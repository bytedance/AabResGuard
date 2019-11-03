package com.bytedance.android.aabresguard.bundle;

import com.android.tools.build.bundletool.model.BundleModule;
import com.android.tools.build.bundletool.model.ModuleEntry;
import com.android.tools.build.bundletool.model.ResourceTableEntry;
import com.android.tools.build.bundletool.model.ZipPath;
import com.android.tools.build.bundletool.model.utils.files.BufferedIo;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.bytedance.android.aabresguard.utils.FileOperation.getZipPathFileSize;

/**
 * Created by YangJing on 2019/10/14 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class AppBundleUtils {

    public static long getZipEntrySize(ZipFile bundleZipFile, ModuleEntry entry, BundleModule bundleModule) {
        String path = String.format("%s/%s", bundleModule.getName().getName(), entry.getPath().toString());
        ZipEntry bundleConfigEntry = bundleZipFile.getEntry(path);
        return getZipPathFileSize(bundleZipFile, bundleConfigEntry);
    }

    public static long getZipEntrySize(ZipFile bundleZipFile, ZipPath zipPath) {
        String path = zipPath.toString();
        ZipEntry bundleConfigEntry = bundleZipFile.getEntry(path);
        return getZipPathFileSize(bundleZipFile, bundleConfigEntry);
    }


    public static String getEntryMd5(ZipFile bundleZipFile, ModuleEntry entry, BundleModule bundleModule) {
        String path = String.format("%s/%s", bundleModule.getName().getName(), entry.getPath().toString());
        ZipEntry bundleConfigEntry = bundleZipFile.getEntry(path);
        try {
            InputStream is = BufferedIo.inputStream(bundleZipFile, bundleConfigEntry);
            String md5 = bytesToHexString(DigestUtils.md5(is));
            is.close();
            return md5;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readByte(ZipFile bundleZipFile, ModuleEntry entry, BundleModule bundleModule) throws IOException {
        String path = String.format("%s/%s", bundleModule.getName().getName(), entry.getPath().toString());
        ZipEntry bundleConfigEntry = bundleZipFile.getEntry(path);
        InputStream is = BufferedIo.inputStream(bundleZipFile, bundleConfigEntry);
        byte[] bytes = IOUtils.toByteArray(is);
        is.close();
        return bytes;
    }

    public static String bytesToHexString(byte[] src) {
        if (src.length <= 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(src.length);
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String getEntryNameByResourceName(String resourceName) {
        int index = resourceName.indexOf(".R.");
        String value = resourceName.substring(index + 3);
        String[] values = value.replace(".", "/").split("/");
        if (values.length != 2) {
            throw new RuntimeException("Invalid resource format, it should be package.type.entry, yours: " + resourceName);
        }
        return values[values.length - 1];
    }

    public static String getTypeNameByResourceName(String resourceName) {
        int index = resourceName.indexOf(".R.");
        String value = resourceName.substring(index + 3);
        String[] values = value.replace(".", "/").split("/");
        if (values.length != 2) {
            throw new RuntimeException("Invalid resource format, it should be package.type.entry, yours: " + resourceName);
        }
        return values[0];
    }

    public static String getResourceFullName(ResourceTableEntry entry) {
        return getResourceFullName(entry.getPackage().getPackageName(), entry.getType().getName(), entry.getEntry().getName());
    }

    public static String getResourceFullName(String packageName, String typeName, String entryName) {
        return String.format("%s.R.%s.%s", packageName, typeName, entryName);
    }
}
