package com.workbuddy.matrix.repository;

import com.workbuddy.matrix.entity.ChatMessage;
import com.workbuddy.matrix.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {

    @Query("""
        SELECT DISTINCT cm FROM ChatMessage cm
        LEFT JOIN FETCH cm.events ev
        WHERE cm.chatSession = :chatSession
        ORDER BY cm.createdAt ASC , ev.sequenceOrder ASC
        """)
    List<ChatMessage> findByChatSession(ChatSession chatSession);
}
