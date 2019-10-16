package com.bytedance.android.aabresguard;

import org.junit.Test;

/**
 * Created by YangJing on 2019/10/16 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class AabResGuardMainTest extends BaseTest {

    @Test
    public void test_help() {
        AabResGuardMain.main(
                new String[]{
                        "help"
                }
        );
    }
}
