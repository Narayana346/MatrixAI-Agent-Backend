package com.workbuddy.matrixai.workspace_service.service.impl;

import com.workbuddy.matrixai.workspace_service.dto.project.DeployResponse;
import com.workbuddy.matrixai.workspace_service.service.DeploymentService;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class KubernetesDeploymentServiceImpl implements DeploymentService {

    private final KubernetesClient client;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.preview.namespace}")
    private String namespace;

    @Value("${app.preview.domain}")
    private String baseDomain;

    @Value("${app.preview.proxy-port}")
    private String proxyPort;

    private static final String POOL_LABEL = "status";
    private static final String PROJECT_LABEL = "project-id";
    private static final String IDLE = "idle";
    private static final String BUSY = "busy";

    public DeployResponse deploy(Long projectId) {
        // Path-based preview id: proxy routes "/previews/<previewId>/..." to this project's pod
        String previewId = "project-" + projectId;

        // Use default port 80 format logic for clean URLs, or explicit ports for local testing
        String formattedUrl = proxyPort.equals("80")
                ? "http://" + baseDomain + "/previews/" + previewId + "/"
                : "http://" + baseDomain + ":" + proxyPort + "/previews/" + previewId + "/";

        Pod existingPod = findActivePod(projectId);

        if (existingPod != null) {
            log.info("Found existing pod {} for project {}. Resuming...", existingPod.getMetadata().getName(), projectId);
            registerRoute(previewId, existingPod);
            return new DeployResponse(formattedUrl);
        }

        return claimAndStartNewPod(projectId, previewId, formattedUrl);
    }

    private Pod findActivePod(Long projectId) {
        return client.pods().inNamespace(namespace)
                .withLabel(PROJECT_LABEL, projectId.toString())
                .withLabel(POOL_LABEL, BUSY)
                .list().getItems().stream()
                .filter(pod -> pod.getStatus().getPhase().equals("Running"))
                .findFirst()
                .orElse(null);
    }

    private DeployResponse claimAndStartNewPod(Long projectId, String previewId, String formattedUrl) {
        Pod pod = client.pods().inNamespace(namespace)
                .withLabel(POOL_LABEL, IDLE)
                .list().getItems().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No idle runners available. Please scale up the runner-pool."));

        String podName = pod.getMetadata().getName();
        log.info("Claiming pod {} for project {}", podName, projectId);

        client.pods().inNamespace(namespace).withName(podName).edit(p -> {
            p.getMetadata().getLabels().put(POOL_LABEL, BUSY);
            p.getMetadata().getLabels().put(PROJECT_LABEL, projectId.toString());
            return p;
        });

        try {
            String initialSyncCmd = String.format("rm -rf /app/* && mc mirror --overwrite myminio/projects/%d/ /app/", projectId);
            execCommand(podName, "syncer", "sh", "-c", initialSyncCmd);

            String watchCmd = String.format("nohup mc mirror --overwrite --watch myminio/projects/%d/ /app/ > /app/sync.log 2>&1 &", projectId);
            execCommand(podName, "syncer", "sh", "-c", watchCmd);

            // --base must match the proxy's path prefix: the proxy only forwards requests
            // under /previews/<id>/..., but Vite's default config emits root-relative asset/
            // HMR paths (/@vite/client, /src/main.tsx, ...), which the proxy 404s as unknown
            // routes. Telling Vite its own base path makes it prefix everything it emits to
            // match.
            String basePath = "/previews/" + previewId + "/";
            String startCmd = String.format(
                    "npm install && nohup npm run dev -- --host 0.0.0.0 --port 5173 --base %s > /app/dev.log 2>&1 &",
                    basePath);
            execCommand(podName, "runner", "sh", "-c", startCmd);

            // npm install + vite startup can easily take longer than this request should
            // block for in the worst case, so this is a best-effort wait, not a hard gate:
            // if it's not up yet by the deadline we still register the route and return,
            // since the pod's NetworkPolicy only allows the proxy to reach it anyway — a
            // slightly-too-early first load just means one manual refresh, not a failure.
            boolean ready = waitForDevServerReady(podName, basePath);
            if (!ready) {
                log.warn("Dev server for project {} did not become ready within the wait budget; " +
                        "returning preview URL anyway, it may need a manual refresh.", projectId);
            }

            Pod updatedPod = client.pods().inNamespace(namespace).withName(podName).get();
            registerRoute(previewId, updatedPod);

            log.info("Deployment successful: {}", formattedUrl);
            return new DeployResponse(formattedUrl);

        } catch (Exception e) {
            log.error("Deployment failed for project {}. Releasing pod {}.", projectId, podName, e);
            client.pods().inNamespace(namespace).withName(podName).delete();
            throw new RuntimeException("Failed to deploy project " + projectId + ": " + e.getMessage(), e);
        }
    }

    private void registerRoute(String previewId, Pod pod) {
        String podIp = pod.getStatus().getPodIP();
        if (podIp == null) throw new RuntimeException("Pod is running but has no IP!");

        redisTemplate.opsForValue().set("route:" + previewId, podIp + ":5173", 6, TimeUnit.HOURS);
        log.info("Route Registered: {} -> {}", previewId, podIp);
    }

    // Polls via exec (loopback inside the pod) rather than a direct network call, since
    // the strict-preview-sandbox NetworkPolicy only allows ingress to runner pods from the
    // matrix-me-proxy pod — workspace-service itself can't reach podIP:5173 directly.
    private boolean waitForDevServerReady(String podName, String basePath) {
        int maxAttempts = 90; // ~3 minutes at 2s intervals — npm install on this hardware
                              // routinely runs past 60s, confirmed empirically (deploys that
                              // timed out at 60s were serving fine ~15-20s later)
        String checkCmd = String.format(
                "wget -q -T 2 -O /dev/null http://localhost:5173%s && echo READY || echo NOTREADY", basePath);

        for (int i = 0; i < maxAttempts; i++) {
            try {
                String result = execCommandCapture(podName, "runner", "sh", "-c", checkCmd);
                if ("READY".equals(result.trim())) {
                    return true;
                }
            } catch (Exception e) {
                log.debug("Readiness check attempt {} for pod {} failed: {}", i, podName, e.getMessage());
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private String execCommandCapture(String podName, String container, String... command) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CompletableFuture<String> data = new CompletableFuture<>();
        try (ExecWatch ignored = client.pods().inNamespace(namespace).withName(podName)
                .inContainer(container)
                .writingOutput(out)
                .writingError(new ByteArrayOutputStream())
                .usingListener(new ExecListener() {
                    @Override
                    public void onClose(int code, String reason) {
                        data.complete("Done");
                    }
                })
                .exec(command)) {

            data.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Readiness exec failed", e);
        }
        return out.toString();
    }

    private void execCommand(String podName, String container, String... command) {
        log.debug("Exec in {}:{} -> {}", podName, container, String.join(" ", command));

        CompletableFuture<String> data = new CompletableFuture<>();
        try (ExecWatch ignored = client.pods().inNamespace(namespace).withName(podName)
                .inContainer(container)
                .writingOutput(new ByteArrayOutputStream())
                .writingError(new ByteArrayOutputStream())
                .usingListener(new ExecListener() {
                    @Override
                    public void onClose(int code, String reason) {
                        data.complete("Done");
                    }
                })
                .exec(command)) {

            if (command[command.length - 1].trim().endsWith("&")) {
                Thread.sleep(500);
            } else {
                data.get(30, TimeUnit.SECONDS);
            }

        } catch (Exception e) {
            log.error("Exec failed", e);
            throw new RuntimeException("Pod Execution Failed", e);
        }
    }


}
