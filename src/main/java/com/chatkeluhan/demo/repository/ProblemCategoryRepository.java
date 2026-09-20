package com.chatkeluhan.demo.repository;

import com.chatkeluhan.demo.entity.ProblemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemCategoryRepository extends JpaRepository<ProblemCategory, String> {
}
