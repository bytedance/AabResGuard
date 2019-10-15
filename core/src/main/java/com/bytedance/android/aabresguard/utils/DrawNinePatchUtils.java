package com.bytedance.android.aabresguard.utils;

import com.android.tools.build.bundletool.model.BundleModule;
import com.android.tools.build.bundletool.model.ZipPath;


/**
 * .9 文件的帮助类
 * <p>
 * Created by YangJing on 2019/04/19 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class DrawNinePatchUtils {

    public static final String RESOURCE_SUFFIX_9_PATCH = ".9";
    public static final String[] RESOURCE_TYPE_IMG = new String[]{
            "drawable",
            "mipmap"
    };

    /**
     * 是否是 .9 的图片资源
     *
     * @param typeName   资源类型名称，如: drawable
     * @param simpleName 资源名称（不包含文件后缀），如 a.9 aa
     * @return 是否是 .9 的图片资源
     */
    public static boolean is9PatchResource(String typeName, String simpleName) {
        if (!simpleName.endsWith(RESOURCE_SUFFIX_9_PATCH)) {
            return false;
        }
        for (String type : RESOURCE_TYPE_IMG) {
            if (typeName.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean is9PatchResource(ZipPath zipPath) {
        if (!zipPath.startsWith(BundleModule.RESOURCES_DIRECTORY)) {
            return false;
        }
        return zipPath.toString().endsWith(RESOURCE_SUFFIX_9_PATCH);
    }

    /**
     * 获取 .9 资源的名称
     *
     * @param simpleName 资源名称（不包含文件后缀），如 a.9 aa
     * @return 如：a.9 => a
     */
    public static String get9PatchSimpleName(String simpleName) {
        return simpleName.substring(0, simpleName.length() - RESOURCE_SUFFIX_9_PATCH.length());
    }
}
