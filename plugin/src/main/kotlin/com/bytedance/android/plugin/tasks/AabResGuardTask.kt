package com.bytedance.android.plugin.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.scope.VariantScope
import com.bytedance.android.aabresguard.commands.ObfuscateBundleCommand
import com.bytedance.android.plugin.extensions.AabResGuardExtension
import com.bytedance.android.plugin.internal.getBundleFilePath
import com.bytedance.android.plugin.internal.getSigningConfig
import com.bytedance.android.plugin.model.SigningConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File
import java.lang.System.out
import java.nio.file.Path
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.internal.logging.text.StyledTextOutput.Style
import javax.inject.Inject
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

/**
 * Created by YangJing on 2019/10/15 .
 * Email: yangjing.yeoh@bytedance.com
 * Modified 2021/08/11
 */
open class AabResGuardTask @Inject constructor(outputFactory: StyledTextOutputFactory) : DefaultTask() {

    @get:Internal
    private lateinit var variant: ApplicationVariant

    @get:Internal
    lateinit var signingConfig: SigningConfig

    @get:Internal
    var aabResGuard: AabResGuardExtension = project.extensions.getByName("aabResGuard") as AabResGuardExtension

    @get:Internal
    private lateinit var bundlePath: Path

    @get:Internal
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
/*
    @InputFile
    @Optional
    fun getObfuscatedBundlePath(): Path {
        return obfuscatedBundlePath
    }
*/
    private val out = outputFactory.create("AabResGuardTask")

    @TaskAction
    private fun execute() {
        out.style(Style.Info).println(aabResGuard.toString())
        // init signing config
        signingConfig = getSigningConfig(project, variant)
        printSignConfiguration()
        printOutputFileLocation()

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
                    out.style(Style.Error).println("replace unused.txt!")
                }
            }
        } else {
            out.style(Style.Error).println("not exists unused.txt : ${usedFile.absolutePath}\n" +
                    "use default path : ${aabResGuard.unusedStringPath}")
        }
    }

    private fun printSignConfiguration() {
        println("-------------- Sign configuration --------------")
        println("\tStoreFile:\t\t${signingConfig.storeFile}")
        println("\tKeyPassword:\t${encrypt(signingConfig.keyPassword)}")
        println("\tAlias:\t\t\t${encrypt(signingConfig.keyAlias)}")
        println("\tStorePassword:\t${encrypt(signingConfig.storePassword)}")
    }

    private fun printOutputFileLocation() {
        println("-------------- Output configuration --------------")
        println("\tFolder:\t\t${obfuscatedBundlePath.parent}")
        println("\tFile:\t\t${obfuscatedBundlePath.fileName}")
        println("--------------------------------------------------")
    }

    private fun encrypt(value: String?): String {
        if (value == null) return "/"
        if (value.length > 2) {
            return "${value.substring(0, value.length / 2)}****"
        }
        return "****"
    }
}