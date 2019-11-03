package com.bytedance.android.aabresguard.android;

import java.io.File;

/**
 * Created by YangJing on 2019/10/17 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class AndroidDebugKeyStoreHelper {

    public static final String DEFAULT_PASSWORD = "android";
    public static final String DEFAULT_ALIAS = "AndroidDebugKey";

    public static JarSigner.Signature debugSigningConfig() {
        String debugKeystoreLocation = defaultDebugKeystoreLocation();
        if (debugKeystoreLocation == null || !new File(debugKeystoreLocation).exists()) {
            return null;
        }
        return new JarSigner.Signature(
                new File(debugKeystoreLocation).toPath(),
                DEFAULT_PASSWORD,
                DEFAULT_ALIAS,
                DEFAULT_PASSWORD
        );
    }

    /**
     * Returns the location of the default debug keystore.
     *
     * @return The location of the default debug keystore
     */
    private static String defaultDebugKeystoreLocation() {
        //this is guaranteed to either return a non null value (terminated with a platform
        // specific separator), or throw.
        String folder = null;
        try {
            folder = AndroidLocation.getFolder();
        } catch (AndroidLocation.AndroidLocationException e) {
            e.printStackTrace();
            return null;
        }
        return folder + "debug.keystore";
    }
}
