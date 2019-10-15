package com.bytedance.android.aabresguard.bundle;

import com.android.build.gradle.internal.packaging.GradleKeystoreHelper;
import com.android.build.gradle.internal.process.JarSigner;
import com.android.builder.signing.DefaultSigningConfig;
import com.bytedance.android.aabresguard.utils.TimeClock;

import java.nio.file.Path;
import java.util.logging.Logger;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;
import static com.bytedance.android.aabresguard.utils.exception.CommandExceptionPreconditions.checkStringIsEmpty;

/**
 * Created by YangJing on 2019/10/11 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class AppBundleSigner {

    private static final Logger logger = Logger.getLogger(AppBundleSigner.class.getName());

    private Path bundleFile;
    private BundleSignature bundleSignature = BundleSignature.DEBUG_SIGNATURE;

    public AppBundleSigner(Path bundleFile, BundleSignature signature) {
        this.bundleFile = bundleFile;
        this.bundleSignature = signature;
    }

    public AppBundleSigner(Path bundleFile) {
        this.bundleFile = bundleFile;
    }

    public void setBundleSignature(BundleSignature bundleSignature) {
        this.bundleSignature = bundleSignature;
    }

    public void execute() {
        TimeClock timeClock = new TimeClock();
        JarSigner.Signature signature = new JarSigner.Signature(
                bundleSignature.storeFile.toFile(),
                bundleSignature.storePassword,
                bundleSignature.keyAlias,
                bundleSignature.keyPassword
        );
        new JarSigner().sign(bundleFile.toFile(), signature);
        logger.info(String.format("[sign] sign done, coast: %s", timeClock.getCoast()));
    }

    public static class BundleSignature {
        public static final BundleSignature DEBUG_SIGNATURE = new BundleSignature();
        private final Path storeFile;
        private final String storePassword;
        private final String keyAlias;
        private final String keyPassword;

        private BundleSignature() {
            DefaultSigningConfig signingConfig = DefaultSigningConfig.debugSigningConfig(GradleKeystoreHelper.getDefaultDebugKeystoreLocation());
            storeFile = signingConfig.getStoreFile().toPath();
            storePassword = signingConfig.getStorePassword();
            keyAlias = signingConfig.getKeyAlias();
            keyPassword = signingConfig.getKeyPassword();
        }

        public BundleSignature(Path storeFile, String storePassword, String keyAlias, String keyPassword) {
            this.storeFile = storeFile;
            this.storePassword = storePassword;
            this.keyAlias = keyAlias;
            this.keyPassword = keyPassword;
            checkFileExistsAndReadable(storeFile);
            checkStringIsEmpty(storePassword, "storePassword");
            checkStringIsEmpty(keyAlias, "keyAlias");
            checkStringIsEmpty(keyPassword, "keyPassword");
        }
    }
}
