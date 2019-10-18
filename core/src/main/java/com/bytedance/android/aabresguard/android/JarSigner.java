package com.bytedance.android.aabresguard.android;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;
import static com.bytedance.android.aabresguard.utils.exception.CommandExceptionPreconditions.checkStringIsEmpty;

/**
 * Created by YangJing on 2019/10/18 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class JarSigner {

    public void sign(File toBeSigned, Signature signature) throws IOException, InterruptedException {
        new OpenJDKJarSigner().sign(toBeSigned, signature);
    }

    public static class Signature {
        public static final Signature DEBUG_SIGNATURE = AndroidDebugKeyStoreHelper.debugSigningConfig();
        public final Path storeFile;
        public final String storePassword;
        public final String keyAlias;
        public final String keyPassword;


        public Signature(Path storeFile, String storePassword, String keyAlias, String keyPassword) {
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
