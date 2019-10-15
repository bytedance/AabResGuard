package com.bytedance.android.plugin.extensions

import java.nio.file.Path

/**
 * Created by YangJing on 2019/10/15 .
 * Email: yangjing.yeoh@bytedance.com
 */
data class AabResGuardExtension(
        val mappingFile: Path,
        val whiteList: Set<String>,
        val obfuscatedBundleFileName: String,
        val mergeDuplicatedRes: Boolean,
        val enableFilterFiles: Boolean,
        val filterList: Set<String>
)