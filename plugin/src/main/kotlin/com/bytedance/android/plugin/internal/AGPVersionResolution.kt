package com.bytedance.android.plugin.internal

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier

/**
 * Created by YangJing on 2020/04/13 .
 * Email: yangjing.yeoh@bytedance.com
 */
internal fun getAGPVersion(project: Project): String {
    var agpVersion: String? = null
    for (artifact in project.rootProject.buildscript.configurations.getByName(ScriptHandler.CLASSPATH_CONFIGURATION)
            .resolvedConfiguration.resolvedArtifacts) {
        val identifier = artifact.id.componentIdentifier
        if (identifier is DefaultModuleComponentIdentifier) {
            if (identifier.group == "com.android.tools.build" || identifier.group.hashCode() == 432891823) {
                if (identifier.module == "gradle") {
                    agpVersion = identifier.version
                }
            }
        }
    }
    if (agpVersion == null) {
        throw GradleException("get AGP version failed")
    }
    return agpVersion
}