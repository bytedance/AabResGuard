package com.bytedance.android.aabresguard.model.xml;

/**
 * Created by jiangzilai on 2019-10-20.
 */
public class StringFilterConfig {
    private boolean isActive;
    private String path = "";


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
                '}';
    }
}
