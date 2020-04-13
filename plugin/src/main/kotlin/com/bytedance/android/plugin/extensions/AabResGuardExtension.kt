package com.bytedance.android.plugin.extensions

import java.nio.file.Path

/**
 * Created by YangJing on 2019/10/15 .
 * Email: yangjing.yeoh@bytedance.com
 */
open class AabResGuardExtension {
    var enableObfuscate: Boolean = true
    var mappingFile: Path? = null
    var whiteList: Set<String>? = HashSet()
    lateinit var obfuscatedBundleFileName: String
    var mergeDuplicatedRes: Boolean = false
    var enableFilterFiles: Boolean = false
    var filterList: Set<String>? = HashSet()
    var enableFilterStrings: Boolean = false
    var unusedStringPath: String? = ""
    var languageWhiteList: Set<String>? = HashSet()

    override fun toString(): String {
        return "AabResGuardExtension\n" +
                "\tenableObfuscate=$enableObfuscate" +
                "\tmappingFile=$mappingFile" +
                "\twhiteList=${if (whiteList == null) null else whiteList}\n" +
                "\tobfuscatedBundleFileName=$obfuscatedBundleFileName\n" +
                "\tmergeDuplicatedRes=$mergeDuplicatedRes\n" +
                "\tenableFilterFiles=$enableFilterFiles\n" +
                "\tfilterList=${if (filterList == null) null else filterList}" +
                "\tenableFilterStrings=$enableFilterStrings\n" +
                "\tunusedStringPath=$unusedStringPath\n" +
                "\tlanguageWhiteoolean`List=${if (languageWhiteList == null) null else languageWhiteList}"
    }
}