package com.workbuddy.matrixai.intelligence_service.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(LocalModelProperties.class)
public class AiConfig {

    @Bean
    @Primary
    public ChatClient chatClient(ChatClient.Builder builder) {
        return  builder
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    // Low-latency free-tier fallback for when the primary configured model is rate-limited
    // or otherwise unavailable (OpenRouter free models commonly 429 under global load).
    // Reuses the primary OpenAiChatModel's api/base-url via mutate(), only swapping the model.
    @Bean
    @Qualifier("fallbackChatClient")
    public ChatClient fallbackChatClient(OpenAiChatModel openAiChatModel,
                                          @Value("${ai.chat.fallback.model:nvidia/nemotron-3.5-lightning:free}") String fallbackModel) {
        OpenAiChatModel fallback = openAiChatModel.mutate()
                .defaultOptions(OpenAiChatOptions.builder().model(fallbackModel).build())
                .build();

        return ChatClient.builder(fallback)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    // Self-hosted Qwen Coder model (Docker Model Runner's OpenAI-compatible API — see
    // k8s/stateful/qwen-coder-model-runner.yaml). Unlike the fallback client above, this
    // talks to a completely different host than the OpenRouter-backed primary client, so
    // it needs its own OpenAiApi/OpenAiChatModel rather than a mutate() of the primary one.
    @Bean
    @Qualifier("localChatClient")
    public ChatClient localChatClient(LocalModelProperties localModelProperties) {
        OpenAiApi localApi = OpenAiApi.builder()
                .baseUrl(localModelProperties.getBaseUrl())
                .apiKey(localModelProperties.getApiKey())
                .completionsPath(localModelProperties.getCompletionsPath())
                .build();

        OpenAiChatModel localModel = OpenAiChatModel.builder()
                .openAiApi(localApi)
                .defaultOptions(OpenAiChatOptions.builder().model(localModelProperties.getModelId()).build())
                .build();

        return ChatClient.builder(localModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
