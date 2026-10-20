package com.chatkeluhan.demo.repository;

import com.chatkeluhan.demo.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByAssignedEmailIgnoreCase(String assignedEmail);

    Optional<Asset> findByAssetTag(String assetTag);

    Optional<Asset> findByAssetTagIgnoreCase(String assetTag);
}
