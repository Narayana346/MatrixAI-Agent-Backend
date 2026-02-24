package com.workbuddy.matrix.llm;
import com.workbuddy.matrix.entity.ChatEvent;
import com.workbuddy.matrix.entity.ChatMessage;
import com.workbuddy.matrix.enums.ChatEventType;
import com.workbuddy.matrix.utility.ContentMatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;


@Component
@Slf4j
public class LLMResponseParser {
    public  List<ChatEvent> parseChatEvents(String fullResponse,ChatMessage chatMessage){
        List<ChatEvent> events = new ArrayList<>();
        int sequenceOrder = 1;

        Matcher matcher = ContentMatcher.GENERIC_TAG_PATTERN.matcher(fullResponse);
        while (matcher.find()){
            String tagName = matcher.group(2).toLowerCase();
            String attributes = matcher.group(3);
            String content = matcher.group(4);

            //Extract attributes map
            Map<String,String> attrMap = extractAttributes(attributes);
            ChatEvent.ChatEventBuilder chatEventBuilder = ChatEvent.builder()
                    .chatMessage(chatMessage)
                    .content(content)
                    .sequenceOrder(sequenceOrder++);

            switch (tagName){
                case "message" -> chatEventBuilder.eventType(ChatEventType.MESSAGE);
                case "file" -> {
                    chatEventBuilder.eventType(ChatEventType.FILE_EDIT);
                    chatEventBuilder.filePath(attrMap.get("path")); // required for files
                }
                case "tool" -> {
                    chatEventBuilder.eventType(ChatEventType.TOOL_LOG);
                    chatEventBuilder.metadata(attrMap.get("args")); // required for tools
                }
                default -> throw new IllegalArgumentException("Invalid tag name: " + tagName);
            }

            events.add(chatEventBuilder.build());

        }
        return events;
    }
    private static Map<String, String> extractAttributes(String attributeString) {
        Map<String, String> attributes = new HashMap<>();
        if (attributeString == null) return attributes;

        Matcher matcher = ContentMatcher.ATTRIBUTE_PATTERN.matcher(attributeString);
        while (matcher.find()) {
            attributes.put(matcher.group(1), matcher.group(2));
        }
        return attributes;
    }
}
