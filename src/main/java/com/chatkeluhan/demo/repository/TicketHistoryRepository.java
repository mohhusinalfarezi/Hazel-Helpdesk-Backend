package com.chatkeluhan.demo.repository;

import com.chatkeluhan.demo.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Integer> {
}