package com.bytedance.android.aabresguard;


import com.bytedance.android.aabresguard.testing.ProcessThread;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

/**
 * Created by YangJing on 2019/04/14 .
 * Email: yangjing.yeoh@bytedance.com
 */
@RunWith(JUnit4.class)
public class BaseTest {

    private static final boolean DEBUG = true;

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder(); // 注意，临时文件夹在执行完毕之后会被自动删除！

    private Path tmpDir;
    private long startTime;

    protected static File loadResourceFile(String path) {
        return TestData.openFile(path);
    }

    protected static Reader loadResourceReader(String path) {
        return TestData.openReader(path);
    }

    protected static InputStream loadResourceStream(String path) {
        return TestData.openStream(path);
    }

    protected static String loadResourcePath(String path) {
        return TestData.resourcePath(path);
    }

    protected static boolean executeCmd(String cmd, Object... objects) {
        return ProcessThread.execute(cmd, objects);
    }

    protected static void openDir(String dir, Object... objects) {
        if (!DEBUG) {
            return;
        }
        ProcessThread.execute("open " + dir, objects);
    }

    @Before
    public void setUp() throws Exception {
        tmpDir = tmp.getRoot().toPath();
        startTime = System.currentTimeMillis();
    }

    @After
    public void tearDown() {
        System.out.println(System.currentTimeMillis() - startTime);
    }

    /**
     * 返回临时路径
     */
    protected Path getTempDirPath() {
        return tmpDir;
    }

    protected String getTempDirFilePath() {
        return tmpDir.toFile().toString();
    }

    @Test
    public void emptyTest() {
        assert true;
    }
}
