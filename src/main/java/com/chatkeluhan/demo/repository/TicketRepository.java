package com.chatkeluhan.demo.repository;

import com.chatkeluhan.demo.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
    // Bot bisa menggunakan ini untuk melacak semua riwayat tiket pelanggan
    List<Ticket> findByCustomer_CustomerId(String customerId);
}