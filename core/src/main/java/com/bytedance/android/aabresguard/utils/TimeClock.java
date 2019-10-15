package com.bytedance.android.aabresguard.utils;

/**
 * Created by YangJing on 2019/04/19 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class TimeClock {

    private long startTime;

    public TimeClock() {
        startTime = System.currentTimeMillis();
    }

    public String getCoast() {
        return (System.currentTimeMillis() - startTime) + "";
    }
}
