package com.workbuddy.matrix.repository;

import com.workbuddy.matrix.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession,Long> {}
