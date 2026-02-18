package com.workbuddy.matrix.mapper;

import com.workbuddy.matrix.dto.chat.ChatResponse;
import com.workbuddy.matrix.entity.ChatMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    List<ChatResponse> fromListOfChatMessage(List<ChatMessage> chatMessageList);
}
