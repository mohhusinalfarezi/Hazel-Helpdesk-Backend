package com.chatkeluhan.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ASSET")
public class Asset {

    @Id
    @Column(name = "asset_id", length = 20)
    private String assetId;

    @Column(name = "asset_name", length = 100, nullable = false)
    private String assetName;

    @Column(name = "location_coordinate", length = 255)
    private String locationCoordinate;

    public Asset() {}

    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
    public String getLocationCoordinate() { return locationCoordinate; }
    public void setLocationCoordinate(String locationCoordinate) { this.locationCoordinate = locationCoordinate; }
}