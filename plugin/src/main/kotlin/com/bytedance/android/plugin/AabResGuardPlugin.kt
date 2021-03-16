package com.bytedance.android.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.bytedance.android.plugin.extensions.AabResGuardExtension
import com.bytedance.android.plugin.tasks.AabResGuardTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Created by YangJing on 2019/10/15 .
 * Email: yangjing.yeoh@bytedance.com
 */
class AabResGuardPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        checkApplicationPlugin(project)
        project.extensions.create("aabResGuard", AabResGuardExtension::class.java)

        val android = project.extensions.getByName("android") as AppExtension
        project.afterEvaluate {
            android.applicationVariants.all { variant ->
                createAabResGuardTask(project, variant)
            }
        }
    }

    private fun createAabResGuardTask(project: Project, variant: ApplicationVariant) {
        val variantName = variant.name.capitalize()
        val bundleTaskName = "bundle$variantName"
        if (project.tasks.findByName(bundleTaskName) == null) {
            return
        }
        val aabResGuardTaskName = "aabresguard$variantName"
        val aabResGuardTask: AabResGuardTask
        aabResGuardTask = if (project.tasks.findByName(aabResGuardTaskName) == null) {
            project.tasks.create(aabResGuardTaskName, AabResGuardTask::class.java)
        } else {
            project.tasks.getByName(aabResGuardTaskName) as AabResGuardTask
        }
        aabResGuardTask.setVariantScope(variant)

        val bundleTask: Task = project.tasks.getByName(bundleTaskName)
        val bundlePackageTask: Task = project.tasks.getByName("package${variantName}Bundle")
        bundleTask.dependsOn(aabResGuardTask)
        aabResGuardTask.dependsOn(bundlePackageTask)
        // AGP-4.0.0-alpha07: use FinalizeBundleTask to sign bundle file
        // FinalizeBundleTask is executed after PackageBundleTask
        val finalizeBundleTaskName = "sign${variantName}Bundle"
        if (project.tasks.findByName(finalizeBundleTaskName) != null) {
            aabResGuardTask.dependsOn(project.tasks.getByName(finalizeBundleTaskName))
        }
    }

    private fun checkApplicationPlugin(project: Project) {
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw  GradleException("Android Application plugin required")
        }
    }
}
