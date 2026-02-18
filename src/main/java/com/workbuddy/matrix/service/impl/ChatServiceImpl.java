package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.chat.ChatResponse;
import com.workbuddy.matrix.entity.ChatMessage;
import com.workbuddy.matrix.entity.ChatSession;
import com.workbuddy.matrix.entity.ChatSessionId;
import com.workbuddy.matrix.mapper.ChatMapper;
import com.workbuddy.matrix.repository.ChatEventRepository;
import com.workbuddy.matrix.repository.ChatMessageRepository;
import com.workbuddy.matrix.repository.ChatSessionRepository;
import com.workbuddy.matrix.security.AuthUtil;
import com.workbuddy.matrix.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatEventRepository chatEventRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMapper chatMapper;
    private final AuthUtil authUtil;


    @Override
    public List<ChatResponse> getProjectChatHistory(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        ChatSession chatSession = chatSessionRepository.getReferenceById(
                new ChatSessionId(projectId,userId)
        );
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatSession(chatSession);

        return chatMapper.fromListOfChatMessage(chatMessages);
    }
}
