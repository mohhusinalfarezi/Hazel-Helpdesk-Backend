package com.chatkeluhan.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "PROBLEM_CATEGORY")
public class ProblemCategory {

    @Id
    @Column(name = "category_id", length = 20)
    private String categoryId;

    @Column(name = "category_name", length = 100, nullable = false)
    private String categoryName;

    @Column(name = "estimated_resolution_hours", nullable = false)
    private Integer estimatedResolutionHours;

    public ProblemCategory() {}

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getEstimatedResolutionHours() { return estimatedResolutionHours; }
    public void setEstimatedResolutionHours(Integer estimatedResolutionHours) { this.estimatedResolutionHours = estimatedResolutionHours; }
}