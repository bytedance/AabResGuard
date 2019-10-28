package com.bytedance.android.aabresguard.commands;

import com.android.tools.build.bundletool.flags.Flag;
import com.android.tools.build.bundletool.flags.FlagParser;
import com.bytedance.android.aabresguard.BaseTest;
import com.bytedance.android.aabresguard.testing.BundleToolOperation;
import com.bytedance.android.aabresguard.utils.FileOperation;

import org.dom4j.DocumentException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by YangJing on 2019/10/15 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class ObfuscateBundleCommandTest extends BaseTest {
    @Test
    public void test_noFlag() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> ObfuscateBundleCommand.fromFlags(
                        new FlagParser().parse(
                                ""
                        )
                ).execute());
        assertThat(flagsException)
                .hasMessageThat()
                .matches("Missing the required --bundle flag.");
    }

    @Test
    public void test_no_bundle() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> ObfuscateBundleCommand.fromFlags(
                        new FlagParser().parse(
                                "--output=" + getTempDirFilePath()
                        )
                ).execute());
        assertThat(flagsException)
                .hasMessageThat()
                .matches("Missing the required --bundle flag.");
    }

    @Test
    public void test_no_Config() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> ObfuscateBundleCommand.fromFlags(
                        new FlagParser().parse(
                                "--bundle=" + loadResourcePath("demo/demo.aab")
                        )
                ).execute());
        assertThat(flagsException)
                .hasMessageThat()
                .matches("Missing the required --config flag.");
    }


    @Test
    public void test_wrong_params() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> ObfuscateBundleCommand.fromFlags(
                        new FlagParser().parse(
                                "--abc=a"
                        )
                ).execute());
        assertThat(flagsException)
                .hasMessageThat()
                .matches("Missing the required --bundle flag.");
    }

    @Test
    public void test_DisableSign() throws DocumentException, IOException, InterruptedException {
        File rawAabFile = loadResourceFile("demo/demo.aab");
        File outputFile = new File(getTempDirPath().toFile(), "obfuscated.aab");
        ObfuscateBundleCommand.fromFlags(
                new FlagParser().parse(
                        "--bundle=" + rawAabFile.getAbsolutePath(),
                        "--output=" + outputFile.getAbsolutePath(),
                        "--config=" + loadResourcePath("demo/config.xml"),
                        "--merge-duplicated-res=true",
                        "--mapping=" + loadResourcePath("demo/mapping.txt"),
                        "--disable-sign=true"
                )
        ).execute();
        assert outputFile.exists();
    }

    @Test
    public void testPass() throws IOException, DocumentException, InterruptedException {
        File rawAabFile = loadResourceFile("demo/demo.aab");
        File outputFile = new File(getTempDirPath().toFile(), "obfuscated.aab");
        ObfuscateBundleCommand.fromFlags(
                new FlagParser().parse(
                        "--bundle=" + rawAabFile.getAbsolutePath(),
                        "--output=" + outputFile.getAbsolutePath(),
                        "--config=" + loadResourcePath("demo/config.xml"),
                        "--merge-duplicated-res=true",
                        "--mapping=" + loadResourcePath("demo/mapping.txt")
                )
        ).execute();
        assert outputFile.exists();
        assert FileOperation.getFileSizes(rawAabFile) > FileOperation.getFileSizes(outputFile);

        Path apkPath = BundleToolOperation.buildApkByBundle(outputFile.toPath(), getTempDirPath());
        assert apkPath.toFile().exists();
    }
}
