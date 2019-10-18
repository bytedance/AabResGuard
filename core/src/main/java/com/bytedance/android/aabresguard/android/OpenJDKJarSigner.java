package com.bytedance.android.aabresguard.android;

import com.bytedance.android.aabresguard.utils.FileUtils;
import com.google.common.base.Joiner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;

/**
 * Created by YangJing on 2019/10/18 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class OpenJDKJarSigner {

    private static String jarSignerExecutable = SdkConstants.currentPlatform() == SdkConstants.PLATFORM_WINDOWS
            ? "jarsigner.exe" : "jarsigner";
    private static Logger logger = Logger.getLogger(OpenJDKJarSigner.class.getName());


    public void sign(File toBeSigned, JarSigner.Signature signature) throws IOException, InterruptedException {
        checkFileExistsAndReadable(toBeSigned.toPath());
        checkFileExistsAndReadable(signature.storeFile);
        File jarSigner = locatedJarSigner();
        List<String> args = new ArrayList<>();
        if (jarSigner != null) {
            args.add(jarSigner.getAbsolutePath());
        } else {
            args.add(jarSignerExecutable);
        }
        args.add("-keystore");
        args.add(signature.storeFile.toFile().getAbsolutePath());

        File keyStorePasswordFile = null;
        File aliasPasswordFile = null;

        // write passwords to a file so it cannot be spied on.
        if (signature.storePassword != null) {
            keyStorePasswordFile = File.createTempFile("store", "prv");
            FileUtils.writeToFile(keyStorePasswordFile, signature.storePassword);
            args.add("-storepass:file");
            args.add(keyStorePasswordFile.getAbsolutePath());
        }

        if (signature.keyPassword != null) {
            aliasPasswordFile = File.createTempFile("alias", "prv");
            FileUtils.writeToFile(aliasPasswordFile, signature.keyPassword);
            args.add("--keypass:file");
            args.add(aliasPasswordFile.getAbsolutePath());
        }

        args.add(toBeSigned.getAbsolutePath());

        if (signature.keyAlias != null) {
            args.add(signature.keyAlias);
        }

        File errorLog = File.createTempFile("error", ".log");
        File outputLog = File.createTempFile("output", ".log");

        logger.fine("Invoking " + Joiner.on(" ").join(args));
        Process process = start(new ProcessBuilder(args).redirectError(errorLog).redirectOutput(outputLog));
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String errors = FileUtils.loadFileWithUnixLineSeparators(errorLog);
            String output = FileUtils.loadFileWithUnixLineSeparators(outputLog);
            throw new RuntimeException(
                    String.format("%s failed with exit code %d: \n %s",
                            jarSignerExecutable, exitCode,
                            errors.trim().isEmpty() ? errors : output
                    )
            );
        }
        if (keyStorePasswordFile != null) {
            keyStorePasswordFile.delete();
        }
        if (aliasPasswordFile != null) {
            aliasPasswordFile.delete();
        }
    }

    private Process start(ProcessBuilder builder) throws IOException {
        return builder.start();
    }

    /**
     * Return the "jarsigner" tool location or null if it cannot be determined.
     */
    private File locatedJarSigner() {
        // Look in the java.home bin folder, on jdk installations or Mac OS X, this is where the
        // javasigner will be located.
        File javaHome = new File(System.getProperty("java.home"));
        File jarSigner = getJarSigner(javaHome);
        if (jarSigner.exists()) {
            return jarSigner;
        } else {
            // if not in java.home bin, it's probable that the java.home points to a JRE
            // installation, we should then look one folder up and in the bin folder.
            jarSigner = getJarSigner(javaHome.getParentFile());
            // if still cant' find it, give up.
            return jarSigner.exists() ? jarSigner : null;
        }
    }

    /**
     * Returns the jarsigner tool location with the bin folder.
     */
    private File getJarSigner(File parentDir) {
        return new File(new File(parentDir, "bin"), jarSignerExecutable);
    }
}
