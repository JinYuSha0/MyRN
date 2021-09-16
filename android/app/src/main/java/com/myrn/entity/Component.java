package com.myrn.entity;

public class Component {
    public int version;
    public String hash;
    public String commonHash;
    public Boolean isCommon;
    public String componentName;
    public String downloadUrl;
    public Long buildTime;

    public Component(int version, String hash, String commonHash, Boolean isCommon, String componentName, String downloadUrl, Long buildTime) {
        this.version = version;
        this.hash = hash;
        this.commonHash = commonHash;
        this.isCommon = isCommon;
        this.componentName = componentName;
        this.downloadUrl = downloadUrl;
        this.buildTime = buildTime;
    }
}
