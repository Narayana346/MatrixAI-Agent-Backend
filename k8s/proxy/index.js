const http = require('http');
const httpProxy = require('http-proxy');
const Redis = require('ioredis');

// Local fallback, but overridden by K8s env vars
const redisUrl = process.env.REDIS_URL || 'redis://localhost:6379';

const redis = new Redis(redisUrl, {
    maxRetriesPerRequest: null,
    enableReadyCheck: false,
    retryStrategy(times) {
        const delay = Math.min(times * 50, 2000);
        console.log(`Redis connection failed. Retrying in ${delay}ms...`);
        return delay;
    }
});

redis.on('error', (err) => console.error('Redis Client Error:', err.message));
redis.on('connect', () => console.log(`✅ Connected to Redis successfully at ${redisUrl}`));

const proxy = httpProxy.createProxyServer({
    ws: true,
    xfwd: true,
    changeOrigin: true
});

async function getTarget(previewId) {
    try {
        const targetIpOrSvc = await redis.get(`route:${previewId}`);
        return targetIpOrSvc || null;
    } catch (err) {
        console.error('Redis Error:', err);
        return null;
    }
}

// Automatically handles "localhost:5173" (local) or "preview-123-svc...:5173" (K8s)
const getTargetUrl = (target) => {
    return target.includes(':') ? `http://${target}` : `http://${target}:5173`;
};

// Path-based routing: "/previews/<id>/rest/of/path" -> previewId="<id>", rest="/rest/of/path"
const PREVIEW_PATH_RE = /^\/previews\/([^/]+)(\/.*)?$/;

function parsePreviewPath(url) {
    const match = url.match(PREVIEW_PATH_RE);
    if (!match) return null;
    return { previewId: match[1], rest: match[2] || '/' };
}

const server = http.createServer(async (req, res) => {
    const parsed = parsePreviewPath(req.url);

    if (!parsed) {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        return res.end('Unknown route. Expected /previews/<id>/...');
    }

    const { previewId, rest } = parsed;
    const targetIpOrSvc = await getTarget(previewId);

    if (!targetIpOrSvc) {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        return res.end(`Preview not found or spinning up for ${previewId}.`);
    }

    const target = getTargetUrl(targetIpOrSvc);
    req.url = rest;
    console.log(`HTTP: ${previewId} -> ${target}${req.url}`);

    proxy.web(req, res, { target }, (e) => {
        console.error(`Proxy Error (Web): ${previewId} -> ${e.message}`);
        if (!res.headersSent) {
            res.writeHead(502);
            res.end('Preview server unavailable or starting...');
        }
    });
});

server.on('upgrade', async (req, socket, head) => {
    const parsed = parsePreviewPath(req.url);

    if (!parsed) {
        return socket.destroy();
    }

    const { previewId, rest } = parsed;
    const targetIpOrSvc = await getTarget(previewId);

    if (targetIpOrSvc) {
        const target = getTargetUrl(targetIpOrSvc);
        req.url = rest;
        console.log(`WS: ${previewId} -> ${target}`);

        proxy.ws(req, socket, head, { target }, (e) => {
            console.error(`Proxy Error (WS): ${previewId} -> ${e.message}`);
            socket.destroy();
        });
    } else {
        socket.destroy();
    }
});

const PORT = process.env.PORT || 80;
server.listen(PORT, () => console.log(`🚀 Path-based Preview Proxy Active on Port ${PORT}`));
