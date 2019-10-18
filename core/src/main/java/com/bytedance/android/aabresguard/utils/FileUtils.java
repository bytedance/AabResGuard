package com.bytedance.android.aabresguard.utils;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;

/**
 * Created by YangJing on 2019/10/18 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class FileUtils {
    private static final Joiner UNIX_NEW_LINE_JOINER = Joiner.on('\n');

    /**
     * Loads a text file forcing the line separator to be of Unix style '\n' rather than being
     * Windows style '\r\n'.
     */
    public static String loadFileWithUnixLineSeparators(File file) throws IOException {
        checkFileExistsAndReadable(file.toPath());
        return UNIX_NEW_LINE_JOINER.join(Files.readLines(file, Charsets.UTF_8));
    }

    /**
     * Creates a new text file or replaces content of an existing file.
     *
     * @param file    the file to write to
     * @param content the new content of the file
     */
    public static void writeToFile(File file, String content) throws IOException {
        Files.createParentDirs(file);
        Files.asCharSink(file, StandardCharsets.UTF_8).write(content);
    }
}
