package com.eraf.mongo.example;

import com.eraf.mongo.domain.BaseIdDocument;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * MongoDB Document 사용 예제
 */
@Document(collection = "users")
public class UserDocument extends BaseIdDocument {

    @Indexed(unique = true)
    @Field("email")
    private String email;

    @Field("name")
    private String name;

    @Field("age")
    private Integer age;

    @Field("active")
    private Boolean active;

    // Constructors
    public UserDocument() {
    }

    public UserDocument(String email, String name) {
        this.email = email;
        this.name = name;
        this.active = true;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
