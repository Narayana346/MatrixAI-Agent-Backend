package com.workbuddy.matrixai.intelligence_service.service.impl;

import com.workbuddy.matrixai.intelligence_service.dto.model.OpenRouterModelInfo;
import com.workbuddy.matrixai.intelligence_service.dto.model.OpenRouterModelsResponse;
import com.workbuddy.matrixai.intelligence_service.service.OpenRouterModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class OpenRouterModelServiceImpl implements OpenRouterModelService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final WebClient webClient;
    private final AtomicReference<List<OpenRouterModelInfo>> cache = new AtomicReference<>();
    private volatile Instant cachedAt = Instant.EPOCH;

    public OpenRouterModelServiceImpl(
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${spring.ai.openai.api-key}") String apiKey) {
        // OpenRouter's /v1/models response (~500 models with descriptions) comfortably
        // exceeds WebClient's default 256KB in-memory buffer limit.
        ExchangeStrategies largeBuffer = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .exchangeStrategies(largeBuffer)
                .build();
    }

    @Override
    public List<OpenRouterModelInfo> listModels() {
        List<OpenRouterModelInfo> cached = cache.get();
        if (cached != null && Duration.between(cachedAt, Instant.now()).compareTo(CACHE_TTL) < 0) {
            return cached;
        }

        List<OpenRouterModelInfo> fetched = fetchModels();
        if (!fetched.isEmpty()) {
            cache.set(fetched);
            cachedAt = Instant.now();
            return fetched;
        }

        // OpenRouter call failed — serve the stale cache if we have one rather than an empty list.
        return cached != null ? cached : Collections.emptyList();
    }

    private List<OpenRouterModelInfo> fetchModels() {
        try {
            OpenRouterModelsResponse response = webClient.get()
                    .uri("/v1/models")
                    .retrieve()
                    .bodyToMono(OpenRouterModelsResponse.class)
                    .block();

            if (response == null || response.data() == null) {
                return Collections.emptyList();
            }

            return response.data().stream()
                    .map(this::toModelInfo)
                    .sorted(Comparator.comparing(OpenRouterModelInfo::free).reversed()
                            .thenComparing(OpenRouterModelInfo::name))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to fetch OpenRouter model catalog: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private OpenRouterModelInfo toModelInfo(OpenRouterModelsResponse.RawModel m) {
        double promptPrice = parsePrice(m.pricing() != null ? m.pricing().prompt() : null);
        double completionPrice = parsePrice(m.pricing() != null ? m.pricing().completion() : null);
        boolean free = promptPrice == 0.0 && completionPrice == 0.0;

        return new OpenRouterModelInfo(
                m.id(),
                m.name() != null ? m.name() : m.id(),
                m.contextLength(),
                free,
                roundToFourDecimals(promptPrice * 1_000_000),
                roundToFourDecimals(completionPrice * 1_000_000)
        );
    }

    private double parsePrice(String raw) {
        if (raw == null || raw.isBlank()) return 0.0;
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double roundToFourDecimals(double value) {
        return Math.round(value * 10_000.0) / 10_000.0;
    }
}
