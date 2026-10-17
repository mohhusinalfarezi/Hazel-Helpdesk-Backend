package com.chatkeluhan.demo.repository;

import com.chatkeluhan.demo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Mengambil semua notifikasi milik user, diurutkan dari yang terbaru
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email);

    // Mengambil notifikasi yang belum dibaca milik user
    List<Notification> findByRecipientEmailAndIsReadFalse(String email);
}
