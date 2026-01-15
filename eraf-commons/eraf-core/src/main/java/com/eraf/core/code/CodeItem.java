package com.eraf.core.code;

/**
 * 공통코드 아이템
 */
public class CodeItem {

    private String group;
    private String code;
    private String name;
    private String description;
    private int sortOrder;
    private boolean enabled;

    public CodeItem() {
    }

    public CodeItem(String group, String code, String name) {
        this.group = group;
        this.code = code;
        this.name = name;
        this.enabled = true;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
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

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static class Builder {
        private final CodeItem item = new CodeItem();

        public Builder group(String group) {
            item.group = group;
            return this;
        }

        public Builder code(String code) {
            item.code = code;
            return this;
        }

        public Builder name(String name) {
            item.name = name;
            return this;
        }

        public Builder description(String description) {
            item.description = description;
            return this;
        }

        public Builder sortOrder(int sortOrder) {
            item.sortOrder = sortOrder;
            return this;
        }

        public Builder enabled(boolean enabled) {
            item.enabled = enabled;
            return this;
        }

        public CodeItem build() {
            return item;
        }
    }
}
