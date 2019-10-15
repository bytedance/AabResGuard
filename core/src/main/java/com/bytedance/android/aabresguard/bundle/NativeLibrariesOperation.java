package com.bytedance.android.aabresguard.bundle;

import com.android.bundle.Files;

/**
 * Created by YangJing on 2019/10/14 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class NativeLibrariesOperation {

    public static Files.NativeLibraries removeDirectory(Files.NativeLibraries nativeLibraries, String zipPath) {
        int index = -1;
        for (int i = 0; i < nativeLibraries.getDirectoryList().size(); i++) {
            Files.TargetedNativeDirectory directory = nativeLibraries.getDirectoryList().get(i);
            if (directory.getPath().equals(zipPath)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return nativeLibraries;
        }
        return nativeLibraries.toBuilder().removeDirectory(index).build();
    }
}
