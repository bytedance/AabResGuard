package com.bytedance.android.aabresguard.bundle;

import com.android.tools.build.bundletool.io.AppBundleSerializer;
import com.android.tools.build.bundletool.model.AppBundle;
import com.bytedance.android.aabresguard.utils.TimeClock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileDoesNotExist;

/**
 * Created by YangJing on 2019/10/11 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class AppBundlePackager {
    private static final Logger logger = Logger.getLogger(AppBundlePackager.class.getName());

    private final Path output;
    private final AppBundle appBundle;

    public AppBundlePackager(AppBundle appBundle, Path output) {
        this.output = output;
        this.appBundle = appBundle;
        checkFileDoesNotExist(output);
    }

    public void execute() throws IOException {
        TimeClock timeClock = new TimeClock();
        AppBundleSerializer appBundleSerializer = new AppBundleSerializer();
        appBundleSerializer.writeToDisk(appBundle, output);

        System.out.println(String.format("package bundle done, coast: %s", timeClock.getCoast()));
    }
}
