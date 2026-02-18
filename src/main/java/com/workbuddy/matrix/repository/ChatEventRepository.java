package com.workbuddy.matrix.repository;

import com.workbuddy.matrix.entity.ChatEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatEventRepository extends JpaRepository<ChatEvent,Long> {
}
