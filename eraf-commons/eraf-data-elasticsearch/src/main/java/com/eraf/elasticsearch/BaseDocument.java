package com.eraf.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * Elasticsearch Document 기본 클래스
 *
 * JPA의 BaseEntity와 유사한 역할을 합니다.
 * ID, 생성/수정 시각, 생성자 필드를 자동 관리합니다.
 */
public abstract class BaseDocument {

    @Id
    private String id;

    @CreatedDate
    @Field(type = FieldType.Date, name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field(type = FieldType.Date, name = "updated_at")
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Keyword, name = "created_by")
    private String createdBy;

    @Field(type = FieldType.Keyword, name = "updated_by")
    private String updatedBy;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
