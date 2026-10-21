package com.chatkeluhan.demo.repository;

import com.chatkeluhan.demo.entity.ServiceCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, Long> {
    List<ServiceCatalog> findByActiveTrueOrderByIdAsc();
}
