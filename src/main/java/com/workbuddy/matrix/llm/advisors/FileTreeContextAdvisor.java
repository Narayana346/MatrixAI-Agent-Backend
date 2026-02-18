package com.workbuddy.matrix.llm.advisors;

import com.workbuddy.matrix.dto.project.FileNode;
import com.workbuddy.matrix.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileTreeContextAdvisor implements StreamAdvisor {
    private final ProjectFileService projectFileService;

    @Override
    public @NonNull Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        Map<String,Object> context = chatClientRequest.context();
        Long projectId = Long.parseLong(context.getOrDefault("projectId",0).toString());
        ChatClientRequest augmentedChatClientRequest = augmentedChatClientRequestWithFileTree(chatClientRequest,projectId);
        return streamAdvisorChain.nextStream(augmentedChatClientRequest);
    }

    @Override
    public @NonNull String getName() {
        return "FileTreeContextAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private ChatClientRequest augmentedChatClientRequestWithFileTree(
            ChatClientRequest chatClientRequest, Long projectId) {

        List<Message> incomingMessages = chatClientRequest.prompt().getInstructions();

        Message systemMessage = incomingMessages.stream()
                .filter(message -> message.getMessageType() == MessageType.SYSTEM)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("System message not found"));

        List<Message> userMessages = incomingMessages.stream()
                .filter(message -> message.getMessageType() != MessageType.SYSTEM)
                .toList();

        List<FileNode> fileTree = projectFileService.getFileTree(projectId);
        String fileTreeContext = "\n ---- FILE_TREE ----\n" +fileTree.toString();

        List<Message> allMessages = new ArrayList<>();

        if(systemMessage != null){
            allMessages.add(systemMessage);
        }
        allMessages.add(new SystemMessage(fileTreeContext));
        allMessages.addAll(userMessages);

        return chatClientRequest
                .mutate()
                .prompt(new Prompt(allMessages,chatClientRequest.prompt().getOptions()))
                .build();
    }
}
