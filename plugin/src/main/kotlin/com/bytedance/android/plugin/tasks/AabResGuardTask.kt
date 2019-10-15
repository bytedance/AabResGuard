package com.bytedance.android.plugin.tasks

import com.android.build.gradle.internal.dsl.CoreSigningConfig
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.tasks.BundleTask
import com.bytedance.android.aabresguard.commands.ObfuscateBundleCommand
import com.bytedance.android.plugin.extensions.AabResGuardExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Path

/**
 * Created by YangJing on 2019/10/15 .
 * Email: yangjing.yeoh@bytedance.com
 */
class AabResGuardTask(private val variantScope: VariantScope) : DefaultTask() {

    lateinit var signingConfig: CoreSigningConfig
    var aabResGuard: AabResGuardExtension = project.extensions.getByName("aabResGuard") as AabResGuardExtension
    private val bundlePath: Path
    private val obfuscatedBundlePath: Path

    init {
        description = "Assemble resource proguard for bundle file"
        group = "bundle"
        outputs.upToDateWhen { false }
        val bundlePackageTask: BundleTask = project.tasks.getByName("package${variantScope.variantData.name.capitalize()}Bundle") as BundleTask
        bundlePath = File(bundlePackageTask.bundleLocation, bundlePackageTask.fileName).toPath()
        obfuscatedBundlePath = File(bundlePackageTask.bundleLocation, aabResGuard.obfuscatedBundleFileName).toPath()
    }

    fun getObfuscatedBundlePath(): Path {
        return obfuscatedBundlePath
    }

    @TaskAction
    private fun execute() {
        println(aabResGuard.toString())
        signingConfig = variantScope.variantData.variantConfiguration.signingConfig
        printSignConfiguration()
        val command = ObfuscateBundleCommand.builder()
                .setBundlePath(bundlePath)
                .setOutputPath(obfuscatedBundlePath)
                .setMappingPath(aabResGuard.mappingFile)
                .setMergeDuplicatedResources(aabResGuard.mergeDuplicatedRes)
                .setWhiteList(aabResGuard.whiteList)
                .setFilterFile(aabResGuard.enableFilterFiles)
                .setFileFilterRules(aabResGuard.filterList)

        if (signingConfig.storeFile.exists()) {
            command.setStoreFile(signingConfig.storeFile.toPath())
                    .setKeyAlias(signingConfig.keyAlias)
                    .setKeyPassword(signingConfig.keyPassword)
                    .setStorePassword(signingConfig.storePassword)
        }
        command.build().execute()
    }

    private fun printSignConfiguration() {
        println("-------------- sign configuration --------------")
        println("\tstoreFile : ${signingConfig.storeFile}")
        println("\tkeyPassword : ${encrypt(signingConfig.keyPassword)}")
        println("\talias : ${encrypt(signingConfig.keyAlias)}")
        println("\tstorePassword : ${encrypt(signingConfig.storePassword)}")
        println("-------------- sign configuration --------------")
    }

    private fun encrypt(value: String): String {
        if (value.length > 1) {
            return "${value.substring(0, value.length)}****"
        }
        return "****"
    }
}