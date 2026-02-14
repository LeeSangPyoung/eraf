package com.eraf.mongo.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * ID 필드를 포함한 MongoDB Document 기본 클래스
 */
public abstract class BaseIdDocument extends BaseDocument {

    @Id
    @Field("_id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
