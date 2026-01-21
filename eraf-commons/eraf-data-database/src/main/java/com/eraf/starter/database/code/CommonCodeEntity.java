package com.eraf.starter.database.code;

import com.eraf.starter.database.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 공통코드 JPA 엔티티
 */
@Entity
@Table(name = "common_code", indexes = {
        @Index(name = "idx_common_code_group", columnList = "code_group"),
        @Index(name = "idx_common_code_group_code", columnList = "code_group, code", unique = true)
})
public class CommonCodeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_group", nullable = false, length = 50)
    private String codeGroup;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "extra_value1", length = 500)
    private String extraValue1;

    @Column(name = "extra_value2", length = 500)
    private String extraValue2;

    @Column(name = "extra_value3", length = 500)
    private String extraValue3;

    public CommonCodeEntity() {
    }

    public CommonCodeEntity(String codeGroup, String code, String name) {
        this.codeGroup = codeGroup;
        this.code = code;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeGroup() {
        return codeGroup;
    }

    public void setCodeGroup(String codeGroup) {
        this.codeGroup = codeGroup;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getExtraValue1() {
        return extraValue1;
    }

    public void setExtraValue1(String extraValue1) {
        this.extraValue1 = extraValue1;
    }

    public String getExtraValue2() {
        return extraValue2;
    }

    public void setExtraValue2(String extraValue2) {
        this.extraValue2 = extraValue2;
    }

    public String getExtraValue3() {
        return extraValue3;
    }

    public void setExtraValue3(String extraValue3) {
        this.extraValue3 = extraValue3;
    }
}
