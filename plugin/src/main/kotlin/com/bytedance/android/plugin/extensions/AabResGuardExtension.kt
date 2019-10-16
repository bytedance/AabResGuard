package com.bytedance.android.plugin.extensions

import java.nio.file.Path

/**
 * Created by YangJing on 2019/10/15 .
 * Email: yangjing.yeoh@bytedance.com
 */
open class AabResGuardExtension {
    lateinit var mappingFile: Path
    lateinit var whiteList: Set<String>
    lateinit var obfuscatedBundleFileName: String
    var mergeDuplicatedRes: Boolean = false
    var enableFilterFiles: Boolean = false
    lateinit var filterList: Set<String>

    override fun toString(): String {
        return "AabResGuardExtension\n" +
                "\tmappingFile=$mappingFile\n" +
                "\twhiteList=$whiteList\n" +
                "\tobfuscatedBundleFileName=$obfuscatedBundleFileName\n" +
                "\tmergeDuplicatedRes=$mergeDuplicatedRes\n" +
                "\tenableFilterFiles=$enableFilterFiles\n" +
                "\tfilterList=$filterList"
    }
}