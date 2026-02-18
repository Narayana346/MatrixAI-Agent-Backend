package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.llm.Prompt;
import com.workbuddy.matrix.llm.advisors.FileTreeContextAdvisor;
import com.workbuddy.matrix.llm.tool.CodeGenerationTool;
import com.workbuddy.matrix.security.AuthUtil;
import com.workbuddy.matrix.service.AiGenerationService;
import com.workbuddy.matrix.service.ProjectFileService;
import com.workbuddy.matrix.utility.ContentMatcher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class AiGenerationServiceImp implements AiGenerationService {
    ChatClient chatClient;
    AuthUtil authUtil;
    ProjectFileService projectFileService;
    FileTreeContextAdvisor fileTreeContextAdvisor;

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<String> stremResponse(String userMessage, Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        createChatSessionIfNotExists(userId);

        Map<String,Object> advisorsParams = Map.of(
                "userId",userId,
                "projectId",projectId
        );
        StringBuilder fullResponseBuffer = new StringBuilder();

        CodeGenerationTool codeGenerationTool = new CodeGenerationTool(projectFileService,projectId);

        return chatClient.prompt()
                .system(Prompt.CODE_GENERATION_SYSTEM_PROMPT)
                .user(userMessage)
                .tools(codeGenerationTool)
                .advisors( advisorSpec -> {
                    advisorSpec.params(advisorsParams);
                    advisorSpec.advisors(fileTreeContextAdvisor);
                })
                .stream()
                .chatResponse()
                .doOnNext( chatResponse -> {
                    log.info("Chat response: {}",chatResponse);
                    String content = chatResponse.getResult().getOutput().getText();
                    fullResponseBuffer.append(content);
                })
                .doOnComplete(() ->{
                    // in here implement background thread as flux way ... but u can use Executor framework
                    Schedulers.boundedElastic().schedule(() -> parseAndSaveFile(fullResponseBuffer.toString(),projectId,userId));
                })
                .doOnError(error -> log.error("Error during streaming for project",error))
                .map(chatResponse -> Objects.requireNonNull(
                        chatResponse.getResult().getOutput().getText())
                );
    }

    private void parseAndSaveFile(String fullResponse, Long projectId , Long userId) {
        // for reference
//        String dummyFileContent = """
//                <message>I'm going to read the files and generate the code</message>
//                <file>path="src/App.jsx"
//                import React from 'react';
//                import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
//                import Home from './Home';
//                import About from './About';
//
//                ......
//
//                </file>
//                """;

        Matcher fileMatcher = ContentMatcher.FILE_TAG_PATTERN.matcher(fullResponse);

        while (fileMatcher.find()){
            String filePath = fileMatcher.group(1);
            String fileContent = fileMatcher.group(2).trim();

            projectFileService.saveFile(userId,projectId,filePath,fileContent);

        }

    }

    private void createChatSessionIfNotExists(Long userId) {

    }
}
