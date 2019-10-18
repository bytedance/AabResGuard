package com.bytedance.android.aabresguard.executors;

import com.android.tools.build.bundletool.model.AppBundle;
import com.android.tools.build.bundletool.model.BundleModule;
import com.bytedance.android.aabresguard.BaseTest;
import com.bytedance.android.aabresguard.bundle.AppBundleAnalyzer;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by YangJing on 2019/10/14 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class ResourcesObfuscatorTest extends BaseTest {

    @Test
    public void test() throws IOException {
        Set<String> whiteList = new HashSet<>(
                ImmutableSet.of(
                        "com.bytedance.android.ugc.aweme.R.raw.*",
                        "*.R.drawable.icon",
                        "*.R.anim.ab*"
                )
        );
        Path bundlePath = loadResourceFile("demo/demo.aab").toPath();
        Path outputDir = getTempDirPath();
        AppBundleAnalyzer analyzer = new AppBundleAnalyzer(bundlePath);
        AppBundle appBundle = analyzer.analyze();
        ResourcesObfuscator obfuscator = new ResourcesObfuscator(bundlePath, appBundle, whiteList, outputDir, loadResourceFile("demo/mapping.txt").toPath());
        AppBundle obfuscateAppBundle = obfuscator.obfuscate();
        assert obfuscateAppBundle != null;
        assert obfuscateAppBundle.getModules().size() == appBundle.getModules().size();
        appBundle.getModules().forEach((bundleModuleName, bundleModule) -> {
            BundleModule obfuscatedModule = obfuscateAppBundle.getModule(bundleModuleName);
            assert obfuscatedModule.getEntries().size() == bundleModule.getEntries().size();
        });
    }
}
