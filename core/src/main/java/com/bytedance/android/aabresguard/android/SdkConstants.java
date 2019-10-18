package com.bytedance.android.aabresguard.android;

/**
 * Created by YangJing on 2019/10/18 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class SdkConstants {
    public static final int PLATFORM_UNKNOWN = 0;
    public static final int PLATFORM_LINUX = 1;
    public static final int PLATFORM_WINDOWS = 2;
    public static final int PLATFORM_DARWIN = 3;

    public static int currentPlatform() {
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        if (os.startsWith("Mac OS")) { //$NON-NLS-1$
            return PLATFORM_DARWIN;
        } else if (os.startsWith("Windows")) { //$NON-NLS-1$
            return PLATFORM_WINDOWS;
        } else if (os.startsWith("Linux")) { //$NON-NLS-1$
            return PLATFORM_LINUX;
        }

        return PLATFORM_UNKNOWN;
    }
}
