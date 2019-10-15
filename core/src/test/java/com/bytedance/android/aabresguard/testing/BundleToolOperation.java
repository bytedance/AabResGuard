package com.bytedance.android.aabresguard.testing;

import com.android.tools.build.bundletool.commands.BuildApksCommand;
import com.android.tools.build.bundletool.commands.ExtractApksCommand;
import com.android.tools.build.bundletool.device.AdbServer;
import com.android.tools.build.bundletool.device.DdmlibAdbServer;
import com.android.tools.build.bundletool.flags.FlagParser;
import com.bytedance.android.aabresguard.BaseTest;

import java.io.File;
import java.nio.file.Path;

import static com.bytedance.android.aabresguard.testing.Aapt2Helper.AAPT2_PATH;

/**
 * Created by YangJing on 2019/10/15 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class BundleToolOperation extends BaseTest {

    public static Path buildApkByBundle(Path bundlePath, Path apkDirPath) {
        // build apks
        Path apksPath = new File(apkDirPath.toFile(), "app.apks").toPath();
        Path deviceSpecPath = loadResourceFile("device-spec/armeabi-v7a_sdk16.json").toPath();
        AdbServer adbServer = DdmlibAdbServer.getInstance();
        BuildApksCommand.fromFlags(
                new FlagParser().parse(
                        "--bundle=" + bundlePath.toFile().getAbsolutePath(),
                        "--output=" + apksPath.toFile(),
                        "--device-spec=" + deviceSpecPath.toFile(),
                        "--aapt2=" + AAPT2_PATH
                ),
                adbServer
        ).execute();
        assert apksPath.toFile().exists();
        // extract apks
        ExtractApksCommand.fromFlags(
                new FlagParser().parse(
                        "--apks=" + apksPath.toFile().getAbsolutePath(),
                        "--output-dir=" + apkDirPath.toFile(),
                        "--device-spec=" + deviceSpecPath.toFile()
                )
        ).execute();
        File[] apkList = apkDirPath.toFile().listFiles((file, s) -> s.endsWith(".apk"));
        assert apkList != null;
        assert apkList.length > 0;
        return apkList[0].toPath();
    }
}
