package com.workbuddy.matrixai.intelligence_service.service;


import com.workbuddy.matrixai.intelligence_service.dto.chat.ChatResponse;

import java.util.List;

public interface ChatService {

    List<ChatResponse> getProjectChatHistory(Long projectId);
}
