package com.bytedance.android.aabresguard.executors;

import com.android.tools.build.bundletool.model.AppBundle;
import com.bytedance.android.aabresguard.BaseTest;
import com.bytedance.android.aabresguard.bundle.AppBundleAnalyzer;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;

/**
 * Created by jiangzilai on 2019-10-20.
 */
public class BundleStringFilterTest extends BaseTest {

    @Test
    public void test() throws IOException {
        Path bundlePath = loadResourceFile("demo/demo.aab").toPath();
        AppBundleAnalyzer analyzer = new AppBundleAnalyzer(bundlePath);
        AppBundle appBundle = analyzer.analyze();
        BundleStringFilter filter = new BundleStringFilter(loadResourceFile("demo/demo.aab").toPath(), appBundle,
                loadResourceFile("demo/unused.txt").toPath().toString(), new HashSet<>());
        AppBundle filteredAppBundle = filter.filter();
        assert filteredAppBundle != null;
    }
}
