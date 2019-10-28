package com.bytedance.android.aabresguard.commands;

import com.android.tools.build.bundletool.flags.Flag;
import com.android.tools.build.bundletool.flags.FlagParser;
import com.bytedance.android.aabresguard.BaseTest;
import com.bytedance.android.aabresguard.utils.FileOperation;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by YangJing on 2019/10/11 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class DuplicatedResourcesMergerCommandTest extends BaseTest {

    @Test
    public void test_noFlag() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> DuplicatedResourcesMergerCommand.fromFlags(
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
                () -> DuplicatedResourcesMergerCommand.fromFlags(
                        new FlagParser().parse(
                                "--output=" + getTempDirFilePath()
                        )
                ).execute());
        assertThat(flagsException)
                .hasMessageThat()
                .matches("Missing the required --bundle flag.");
    }

    @Test
    public void test_no_Output() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> DuplicatedResourcesMergerCommand.fromFlags(
                        new FlagParser().parse(
                                "--bundle=" + loadResourcePath("demo/demo.aab")
                        )
                ).execute());
        assertThat(flagsException)
                .hasMessageThat()
                .matches("Missing the required --output flag.");
    }

    @Test
    public void test_notExists_bundle() {
        String tempPath = getTempDirFilePath();
        File apkFile = new File(tempPath + "abc.apk");
        IllegalArgumentException flagsException = assertThrows(IllegalArgumentException.class,
                () -> DuplicatedResourcesMergerCommand.fromFlags(
                        new FlagParser().parse(
                                "--bundle=" + apkFile.getAbsolutePath(),
                                "--output=" + getTempDirFilePath()
                        )
                ).execute());
        assertThat(flagsException)
                .hasMessageThat()
                .matches(String.format("File '%s' was not found.", apkFile.getAbsolutePath()));
    }

    @Test
    public void test_wrong_params() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> DuplicatedResourcesMergerCommand.fromFlags(
                        new FlagParser().parse(
                                "--abc=a"
                        )
                ).execute());
        assertThat(flagsException)
                .hasMessageThat()
                .matches("Missing the required --bundle flag.");
    }

    @Test
    public void test_disableSign() throws IOException, InterruptedException {
        File rawAabFile = loadResourceFile("demo/demo.aab");
        File outputFile = new File(getTempDirPath().toFile(), "duplicated.aab");
        DuplicatedResourcesMergerCommand.fromFlags(
                new FlagParser().parse(
                        "--bundle=" + rawAabFile.getAbsolutePath(),
                        "--output=" + outputFile.getAbsolutePath(),
                        "--disable-sign=true"
                )
        ).execute();
        assert outputFile.exists();
    }

    @Test
    public void testMergeDuplicatedRes() throws IOException, InterruptedException {
        File rawAabFile = loadResourceFile("demo/demo.aab");
        File outputFile = new File(getTempDirPath().toFile(), "duplicated.aab");
        DuplicatedResourcesMergerCommand.fromFlags(
                new FlagParser().parse(
                        "--bundle=" + rawAabFile.getAbsolutePath(),
                        "--output=" + outputFile.getAbsolutePath()
                )
        ).execute();
        assert outputFile.exists();
        assert FileOperation.getFileSizes(rawAabFile) > FileOperation.getFileSizes(outputFile);
    }
}
