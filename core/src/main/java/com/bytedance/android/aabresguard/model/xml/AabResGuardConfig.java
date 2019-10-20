package com.bytedance.android.aabresguard.model.xml;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by YangJing on 2019/10/14 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class AabResGuardConfig {
    private FileFilterConfig fileFilter;
    private StringFilterConfig stringFilterConfig;
    private boolean useWhiteList;
    private Set<String> whiteList = new HashSet<>();


    public FileFilterConfig getFileFilter() {
        return fileFilter;
    }

    public void setFileFilter(FileFilterConfig fileFilter) {
        this.fileFilter = fileFilter;
    }

    public boolean isUseWhiteList() {
        return useWhiteList;
    }

    public void setUseWhiteList(boolean useWhiteList) {
        this.useWhiteList = useWhiteList;
    }

    public Set<String> getWhiteList() {
        return whiteList;
    }

    public void addWhiteList(String whiteRule) {
        this.whiteList.add(whiteRule);
    }

    public StringFilterConfig getStringFilterConfig() {
        return stringFilterConfig;
    }

    public void setStringFilterConfig(StringFilterConfig stringFilterConfig) {
        this.stringFilterConfig = stringFilterConfig;
    }
}
