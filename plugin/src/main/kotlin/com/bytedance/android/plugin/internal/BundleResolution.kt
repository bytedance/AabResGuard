package com.bytedance.android.plugin.internal

import com.android.build.gradle.internal.scope.VariantScope
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path

/**
 * Created by YangJing on 2020/01/07 .
 * Email: yangjing.yeoh@bytedance.com
 */
internal fun getBundleFilePath(project: Project, variantScope: VariantScope): Path {
    val agpVersion = getAGPVersion(project)
    val flavor = variantScope.variantData.name
    return if (agpVersion.startsWith("3.")) {
        getBundleFileForAGP3(project, flavor).toPath()
    } else {
        getBundleFileForAGP4(project, flavor).toPath()
    }
}

fun getBundleFileForAGP3(project: Project, flavor: String): File {
    // AGP-3.2.1: package{}Bundle task is com.android.build.gradle.internal.tasks.BundleTask
    // AGP-3.4.1: package{}Bundle task is com.android.build.gradle.internal.tasks.PackageBundleTask
    val bundleTaskName = "package${flavor.capitalize()}Bundle"
    val bundleTask = project.tasks.getByName(bundleTaskName)
    return File(bundleTask.property("bundleLocation") as File, bundleTask.property("fileName") as String)
}

fun getBundleFileForAGP4(project: Project, flavor: String): File {
    // AGP-4.0.0-alpha07: use FinalizeBundleTask to sign bundle file
    val finalizeBundleTask = project.tasks.getByName("sign${flavor.capitalize()}Bundle")
    // FinalizeBundleTask.finalBundleFile is the final bundle path
    val bundleFile = finalizeBundleTask.property("finalBundleFile")
    val regularFile = bundleFile!!::class.java.getMethod("get").invoke(bundleFile)
    return regularFile::class.java.getMethod("getAsFile").invoke(regularFile) as File
}
