package com.bytedance.android.aabresguard.model.xml;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jiangzilai on 2019-10-20.
 */
public class StringFilterConfig {
    private boolean isActive;
    private String path = "";
    private Set<String> languageWhiteList = new HashSet<>();


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override public String toString() {
        return "StringFilterConfig{" +
                "isActive=" + isActive +
                ", path='" + path + '\'' +
                ", languageWhiteList=" + languageWhiteList +
                '}';
    }

    public Set<String> getLanguageWhiteList() {
        return languageWhiteList;
    }
}
