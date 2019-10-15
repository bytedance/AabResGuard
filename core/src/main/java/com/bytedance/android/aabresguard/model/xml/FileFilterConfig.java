package com.bytedance.android.aabresguard.model.xml;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by YangJing on 2019/10/14 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class FileFilterConfig {
    private boolean isActive;
    private Set<String> rules = new HashSet<>();

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Set<String> getRules() {
        return rules;
    }

    public void addRule(String rule) {
        rules.add(rule);
    }
}
