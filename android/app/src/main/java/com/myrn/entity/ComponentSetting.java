package com.myrn.entity;

public class ComponentSetting {
    public String hash;
    public String commonHash;
    public String bundleName;
    public String componentName;
    public Long timestamp;

    public ComponentSetting(String hash, String commonHash, String bundleName, String componentName, Long timestamp) {
        this.hash = hash;
        this.commonHash = commonHash;
        this.bundleName = bundleName;
        this.componentName = componentName;
        this.timestamp = timestamp;
    }
}
