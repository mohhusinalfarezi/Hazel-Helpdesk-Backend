package com.chatkeluhan.demo.repository;

import com.chatkeluhan.demo.entity.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Integer> {
    List<TicketMessage> findByTicket_TicketIdOrderBySentAtAsc(String ticketId);
}
