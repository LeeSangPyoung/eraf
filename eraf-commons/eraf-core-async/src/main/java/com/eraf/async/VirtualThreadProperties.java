package com.eraf.async;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Virtual Thread Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.async.virtual-threads")
public class VirtualThreadProperties {

    /**
     * Virtual Thread 활성화 여부 (Java 21+)
     */
    private boolean enabled = true;

    /**
     * Virtual Thread 이름 접두사
     */
    private String namePrefix = "virtual-";

    /**
     * 스택 크기 (bytes, 0이면 기본값)
     */
    private long stackSize = 0;

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public long getStackSize() {
        return stackSize;
    }

    public void setStackSize(long stackSize) {
        this.stackSize = stackSize;
    }
}
