package com.bytedance.android.plugin.internal

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.scope.VariantScope
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path

/**
 * Created by YangJing on 2020/01/07 .
 * Email: yangjing.yeoh@bytedance.com
 */
internal fun getBundleFilePath(project: Project, variant:ApplicationVariant): Path {
    val agpVersion = getAGPVersion(project)
    val flavor = variant.name
    return when {
        // AGP3.2.0 - 3.2.1: packageBundle task class is com.android.build.gradle.internal.tasks.BundleTask
        // AGP3.3.0 - 3.3.2: packageBundle task class is com.android.build.gradle.internal.tasks.PackageBundleTask
        agpVersion.startsWith("3.2") || agpVersion.startsWith("3.3") -> {
            getBundleFileForAGP32To33(project, flavor).toPath()
        }
        // AGP3.4.0+: use FinalizeBundleTask sign bundle file
        // packageBundle task bundleLocation is intermediates dir
        // The finalize bundle file path: FinalizeBundleTask.finalBundleLocation
        agpVersion.startsWith("3.4") || agpVersion.startsWith("3.5") -> {
            getBundleFileForAGP34To35(project, flavor).toPath()
        }
        // AGP4.0+: removed finalBundleLocation field, and finalBundleFile is public field
        else -> {
            getBundleFileForAGP40After(project, flavor).toPath()
        }
    }
}

fun getBundleFileForAGP32To33(project: Project, flavor: String): File {
    val bundleTaskName = "package${flavor.capitalize()}Bundle"
    val bundleTask = project.tasks.getByName(bundleTaskName)
    return File(bundleTask.property("bundleLocation") as File, bundleTask.property("fileName") as String)
}

fun getBundleFileForAGP34To35(project: Project, flavor: String): File {
    // use FinalizeBundleTask to sign bundle file
    val finalizeBundleTask = project.tasks.getByName("sign${flavor.capitalize()}Bundle")
    // FinalizeBundleTask.finalBundleFile is the final bundle path
    val location = finalizeBundleTask.property("finalBundleLocation") as File
    return File(location, finalizeBundleTask.property("finalBundleFileName") as String)
}

fun getBundleFileForAGP40After(project: Project, flavor: String): File {
    // use FinalizeBundleTask to sign bundle file
    val finalizeBundleTask = project.tasks.getByName("sign${flavor.capitalize()}Bundle")
    // FinalizeBundleTask.finalBundleFile is the final bundle path
    val bundleFile = finalizeBundleTask.property("finalBundleFile")
    val regularFile = bundleFile!!::class.java.getMethod("get").invoke(bundleFile)
    return regularFile::class.java.getMethod("getAsFile").invoke(regularFile) as File
}
