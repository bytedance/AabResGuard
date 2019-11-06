package com.bytedance.android.aabresguard.bundle;

import com.bytedance.android.aabresguard.android.JarSigner;
import com.bytedance.android.aabresguard.utils.TimeClock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Created by YangJing on 2019/10/11 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class AppBundleSigner {

    private Path bundleFile;
    private JarSigner.Signature bundleSignature = JarSigner.Signature.DEBUG_SIGNATURE;

    public AppBundleSigner(Path bundleFile, JarSigner.Signature signature) {
        this.bundleFile = bundleFile;
        this.bundleSignature = signature;
    }

    public AppBundleSigner(Path bundleFile) {
        this.bundleFile = bundleFile;
    }

    public void setBundleSignature(JarSigner.Signature bundleSignature) {
        this.bundleSignature = bundleSignature;
    }

    public void execute() throws IOException, InterruptedException {
        if (bundleSignature == null) {
            return;
        }
        TimeClock timeClock = new TimeClock();
        JarSigner.Signature signature = new JarSigner.Signature(
                bundleSignature.storeFile,
                bundleSignature.storePassword,
                bundleSignature.keyAlias,
                bundleSignature.keyPassword
        );
        new JarSigner().sign(bundleFile.toFile(), signature);
        System.out.println(String.format("[sign] sign done, coast: %s", timeClock.getCoast()));
    }
}
