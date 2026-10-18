package com.chatkeluhan.demo.repository;

import com.chatkeluhan.demo.entity.Role;
import com.chatkeluhan.demo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByRole(Role role);
}
