package com.bytedance.android.plugin.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.scope.VariantScope
import com.bytedance.android.aabresguard.commands.ObfuscateBundleCommand
import com.bytedance.android.plugin.extensions.AabResGuardExtension
import com.bytedance.android.plugin.internal.getBundleFilePath
import com.bytedance.android.plugin.internal.getSigningConfig
import com.bytedance.android.plugin.model.SigningConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Path

/**
 * Created by YangJing on 2019/10/15 .
 * Email: yangjing.yeoh@bytedance.com
 */
open class AabResGuardTask : DefaultTask() {

    private lateinit var variant: ApplicationVariant
    lateinit var signingConfig: SigningConfig
    var aabResGuard: AabResGuardExtension = project.extensions.getByName("aabResGuard") as AabResGuardExtension
    private lateinit var bundlePath: Path
    private lateinit var obfuscatedBundlePath: Path

    init {
        description = "Assemble resource proguard for bundle file"
        group = "bundle"
        outputs.upToDateWhen { false }
    }

    fun setVariantScope(variant:ApplicationVariant) {
        this.variant=variant;
        // init bundleFile, obfuscatedBundlePath must init before task action.
        bundlePath = getBundleFilePath(project, variant)
        obfuscatedBundlePath = File(bundlePath.toFile().parentFile, aabResGuard.obfuscatedBundleFileName).toPath()
    }

    fun getObfuscatedBundlePath(): Path {
        return obfuscatedBundlePath
    }

    @TaskAction
    private fun execute() {
        println(aabResGuard.toString())
        // init signing config
        signingConfig = getSigningConfig(project, variant)
        printSignConfiguration()

        prepareUnusedFile()

        val command = ObfuscateBundleCommand.builder()
                .setEnableObfuscate(aabResGuard.enableObfuscate)
                .setBundlePath(bundlePath)
                .setOutputPath(obfuscatedBundlePath)
                .setMergeDuplicatedResources(aabResGuard.mergeDuplicatedRes)
                .setWhiteList(aabResGuard.whiteList)
                .setFilterFile(aabResGuard.enableFilterFiles)
                .setFileFilterRules(aabResGuard.filterList)
                .setRemoveStr(aabResGuard.enableFilterStrings)
                .setUnusedStrPath(aabResGuard.unusedStringPath)
                .setLanguageWhiteList(aabResGuard.languageWhiteList)
        if (aabResGuard.mappingFile != null) {
            command.setMappingPath(aabResGuard.mappingFile)
        }

        if (signingConfig.storeFile != null && signingConfig.storeFile!!.exists()) {
            command.setStoreFile(signingConfig.storeFile!!.toPath())
                    .setKeyAlias(signingConfig.keyAlias)
                    .setKeyPassword(signingConfig.keyPassword)
                    .setStorePassword(signingConfig.storePassword)
        }
        command.build().execute()
    }

    private fun prepareUnusedFile() {
        val simpleName = variant.name.replace("Release", "")
        val name = simpleName[0].toLowerCase() + simpleName.substring(1)
        val resourcePath = "${project.buildDir}/outputs/mapping/$name/release/unused.txt"
        val usedFile = File(resourcePath)
        if (usedFile.exists()) {
            println("find unused.txt : ${usedFile.absolutePath}")
            if (aabResGuard.enableFilterStrings) {
                if (aabResGuard.unusedStringPath == null || aabResGuard.unusedStringPath!!.isBlank()) {
                    aabResGuard.unusedStringPath = usedFile.absolutePath
                    println("replace unused.txt!")
                }
            }
        } else {
            println("not exists unused.txt : ${usedFile.absolutePath}\n" +
                    "use default path : ${aabResGuard.unusedStringPath}")
        }
    }

    private fun printSignConfiguration() {
        println("-------------- sign configuration --------------")
        println("\tstoreFile : ${signingConfig.storeFile}")
        println("\tkeyPassword : ${encrypt(signingConfig.keyPassword)}")
        println("\talias : ${encrypt(signingConfig.keyAlias)}")
        println("\tstorePassword : ${encrypt(signingConfig.storePassword)}")
        println("-------------- sign configuration --------------")
    }

    private fun encrypt(value: String?): String {
        if (value == null) return "/"
        if (value.length > 2) {
            return "${value.substring(0, value.length / 2)}****"
        }
        return "****"
    }
}