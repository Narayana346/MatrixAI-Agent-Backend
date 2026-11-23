package com.workbuddy.matrixai.workspace_service.scheduler;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

// Claiming a runner pod (status: idle -> busy) drops it out of the runner-pool
// Deployment's label selector, so the Deployment just replaces it with a fresh idle
// pod — the claimed pod becomes permanently orphaned and is never reclaimed by
// anything on its own. This reaper deletes an orphaned "busy" pod once its Redis
// route key (the same 6h TTL that KubernetesDeploymentServiceImpl#registerRoute
// already sets, and refreshes on every (re)deploy) has expired, i.e. the preview
// hasn't been (re)deployed/visited-via-redeploy in 6h. Deleting it is safe: it's
// already unmanaged by the Deployment, so this doesn't touch the idle pool at all.
@Component
@RequiredArgsConstructor
@Slf4j
public class PreviewPodReaper {

    private final KubernetesClient client;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.preview.namespace}")
    private String namespace;

    private static final String POOL_LABEL = "status";
    private static final String PROJECT_LABEL = "project-id";
    private static final String BUSY = "busy";

    @Scheduled(initialDelay = 5, fixedDelay = 15, timeUnit = TimeUnit.MINUTES)
    public void reapInactivePreviews() {
        List<Pod> busyPods = client.pods().inNamespace(namespace)
                .withLabel(POOL_LABEL, BUSY)
                .list().getItems();

        for (Pod pod : busyPods) {
            String projectId = pod.getMetadata().getLabels().get(PROJECT_LABEL);
            String podName = pod.getMetadata().getName();
            if (projectId == null) {
                continue;
            }

            String routeKey = "route:project-" + projectId;
            Boolean routeStillActive = redisTemplate.hasKey(routeKey);

            if (routeStillActive == null || !routeStillActive) {
                log.info("Reaping inactive preview pod {} for project {} (route TTL expired)", podName, projectId);
                client.pods().inNamespace(namespace).withName(podName).delete();
            }
        }
    }
}
