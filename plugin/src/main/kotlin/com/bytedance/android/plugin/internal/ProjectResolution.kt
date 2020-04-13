package com.bytedance.android.plugin.internal

import com.android.build.gradle.internal.VariantManager
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier

/**
 * Created by YangJing on 2020/01/07 .
 * Email: yangjing.yeoh@bytedance.com
 */
internal fun getVariantManager(project: Project): VariantManager {
    val appPlugin: Plugin<Any>? = when {
        // AGP4.0.0-alpha07: move all methods to com.android.internal.application
        project.plugins.hasPlugin("com.android.internal.application") -> {
            project.plugins.getPlugin("com.android.internal.application")
        }
        project.plugins.hasPlugin("com.android.application") -> {
            project.plugins.getPlugin("com.android.application")
        }
        else -> {
            throw GradleException("Unexpected AppPlugin")
        }
    }
    return getVariantManagerFromAppPlugin(appPlugin)
            ?: throw GradleException("get VariantManager failed")
}

private fun getVariantManagerFromAppPlugin(appPlugin: Any?): VariantManager? {
    return if (appPlugin == null) return null else try {
        for (method in appPlugin::class.java.methods) {
            if (method.name == "getVariantManager") {
                return method.invoke(appPlugin) as VariantManager?
            }
        }
        for (method in appPlugin::class.java.declaredMethods) {
            if (method.name == "getVariantManager") {
                return method.invoke(appPlugin) as VariantManager?
            }
        }
        return null
    } catch (e: Exception) {
        null
    }
}
