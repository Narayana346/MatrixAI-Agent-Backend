package com.workbuddy.matrix.service;


import com.workbuddy.matrix.dto.chat.ChatResponse;

import java.util.List;

public interface ChatService {
    List<ChatResponse> getProjectChatHistory(Long projectId);
}
