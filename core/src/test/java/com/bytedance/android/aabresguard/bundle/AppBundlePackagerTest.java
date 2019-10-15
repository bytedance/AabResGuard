package com.bytedance.android.aabresguard.bundle;

import com.android.tools.build.bundletool.model.AppBundle;
import com.bytedance.android.aabresguard.BaseTest;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * Created by YangJing on 2019/10/11 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class AppBundlePackagerTest extends BaseTest {

    @Test
    public void testPackageAppBundle() throws IOException {
        File output = new File(getTempDirPath().toFile(), "package.aab");
        AppBundle appBundle = AppBundle.buildFromZip(new ZipFile(loadResourceFile("demo/demo.aab")));
        AppBundlePackager packager = new AppBundlePackager(appBundle, output.toPath());
        packager.execute();
        assert output.exists();
    }
}
