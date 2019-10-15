package com.bytedance.android.aabresguard;/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Provides centralized access to testdata files.
 *
 * <p>The {@code fileName} argument always starts with the "testdata/" directory.
 */
@SuppressWarnings("WeakerAccess")
class TestData {

    private static final String PACKAGE = "com/bytedance/android/aabresguard/";

    private TestData() {
    }

    static URL getUrl(String path) {
        URL dirURL = TestData.class.getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            return dirURL;
        }
        if (dirURL == null) {
            String className = TestData.class.getName().replace(".", "/") + ".class";
            dirURL = TestData.class.getClassLoader().getResource(className);
            String classPath = dirURL.getFile();
            classPath = classPath.substring(0, classPath.indexOf("/build/"));
            classPath = "file:" + classPath + "/src/test/resources/" + PACKAGE + path;
            try {
                dirURL = new URL(classPath);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return dirURL;
    }

    static InputStream openStream(String fileName) {
        InputStream is = null;
        try {
            is = new FileInputStream(new File(getUrl(fileName).getFile()));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        checkArgument(is != null, "Testdata file '%s' not found.", fileName);
        return is;
    }

    static Reader openReader(String fileName) {
        return new InputStreamReader(openStream(fileName), UTF_8);
    }

    @SuppressWarnings("UnstableApiUsage")
    static byte[] readBytes(String fileName) {
        try (InputStream inputStream = openStream(fileName)) {
            return ByteStreams.toByteArray(inputStream);
        } catch (IOException e) {
            // Throw an unchecked exception to allow usage in lambda expressions.
            throw new UncheckedIOException(
                    String.format("Failed to read contents of com.bytedance.android.aabresguard file '%s'.", fileName), e);
        }
    }

    static File openFile(String fileName) {
        String filePath = getUrl(fileName).getFile();
        checkArgument(filePath != null, "Testdata file '%s' not found.", fileName);
        return new File(filePath);
    }

    static String resourcePath(String resourceName) {
        return openFile(resourceName).getPath();
    }
}
