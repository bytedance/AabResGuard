package com.bytedance.android.aabresguard.issues.i1;

import com.android.tools.build.bundletool.flags.FlagParser;
import com.bytedance.android.aabresguard.BaseTest;
import com.bytedance.android.aabresguard.commands.ObfuscateBundleCommand;
import com.bytedance.android.aabresguard.testing.BundleToolOperation;

import org.dom4j.DocumentException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by YangJing on 2019/10/31 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class Issue1Test extends BaseTest {

    private Path loadIssueResourcePath(String name) {
        return loadResourceFile("issues/i1/" + name).toPath();
    }

    /**
     * 增量混淆后生成的 aab 无法被解析，错误信息：
     * Caused by: java.util.concurrent.ExecutionException: com.android.tools.build.bundletool.model.Aapt2Command$Aapt2Exception:
     * Command '[/var/folders/sc/1qy4dyz527vf0j1qg5h2r0b40000gn/T/2712062020451125499/output/macos/aapt2, convert, --output-format, binary, -o, /var/folders/sc/1qy4dyz527vf0j1qg5h2r0b40000gn/T/5211613263510034859/binary.apk, /var/folders/sc/1qy4dyz527vf0j1qg5h2r0b40000gn/T/5211613263510034859/proto.apk]'
     * didn't terminate successfully (exit code: 1). Check the logs.
     * <p>
     * aapt2 convert --output-format binary -o binary.apk proto.apk
     * proto.apk: error: failed to deserialize resources.pb: duplicate configuration in resource table.
     * proto.apk: error: failed to load APK.
     */
    @Test
    public void test() throws DocumentException, IOException, InterruptedException {
        Path bundlePath = loadIssueResourcePath("raw.aab");
        File outputFile = new File(getTempDirPath().toFile(), "obfuscated.aab");
        ObfuscateBundleCommand.fromFlags(
                new FlagParser().parse(
                        "--bundle=" + bundlePath.toFile().getAbsolutePath(),
                        "--output=" + outputFile.getAbsolutePath(),
                        "--config=" + loadIssueResourcePath("config.xml"),
                        "--merge-duplicated-res=true",
                        "--mapping=" + loadIssueResourcePath("mapping.txt")
                )
        ).execute();
        assert outputFile.exists();

        Path apkPath = BundleToolOperation.buildApkByBundle(outputFile.toPath(), getTempDirPath());
        assert apkPath.toFile().exists();
    }
}
