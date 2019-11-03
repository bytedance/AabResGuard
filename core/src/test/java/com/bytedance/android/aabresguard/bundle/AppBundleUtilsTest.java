package com.bytedance.android.aabresguard.bundle;

import com.bytedance.android.aabresguard.BaseTest;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by YangJing on 2019/11/03 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class AppBundleUtilsTest extends BaseTest {

    @Test
    public void test_getEntryNameByResourceName() {
        assertEquals(AppBundleUtils.getEntryNameByResourceName("a.b.c.R.drawable.a"), "a");
    }

    @Test
    public void test_getTypeNameByResourceName() {
        assertEquals(AppBundleUtils.getTypeNameByResourceName("a.b.c.R.drawable.a"), "drawable");
    }
}
