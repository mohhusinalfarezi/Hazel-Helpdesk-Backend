package com.chatkeluhan.demo.repository;

import com.chatkeluhan.demo.entity.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {
    List<KnowledgeArticle> findAllByOrderByCreatedAtDesc();
    List<KnowledgeArticle> findByCategoryIgnoreCaseOrderByCreatedAtDesc(String category);
}
