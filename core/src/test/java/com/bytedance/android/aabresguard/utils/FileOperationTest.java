package com.bytedance.android.aabresguard.utils;


import com.bytedance.android.aabresguard.BaseTest;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by YangJing on 2019/04/10 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class FileOperationTest extends BaseTest {

    @Test
    public void testUnZip() throws IOException {
        File aabFile = loadResourceFile("demo/demo.aab");
        Path unzipDirPath = getTempDirPath();
        Path targetDir = new File(getTempDirPath().toFile(), "/aab").toPath();
        FileOperation.uncompress(aabFile.toPath(), targetDir);
        System.out.println("testUnZip method coast:");
        FileOperation.uncompress(aabFile.toPath(), unzipDirPath);
    }

    @Test
    public void testDrawNinePatchName() {
        assertEquals(FileOperation.getParentFromZipFilePath("res/a/a.9.png"), "res/a");
        assertEquals(FileOperation.getNameFromZipFilePath("res/a/a.9.png"), "a.9.png");
        assertEquals(FileOperation.getFilePrefixByFileName("a.9.png"), "a");
    }
}
