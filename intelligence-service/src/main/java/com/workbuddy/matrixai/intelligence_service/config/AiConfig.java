package com.workbuddy.matrixai.intelligence_service.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
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
}
