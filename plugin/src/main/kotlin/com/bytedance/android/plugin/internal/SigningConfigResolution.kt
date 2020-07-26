package com.bytedance.android.plugin.internal

import com.android.build.gradle.internal.VariantManager
import com.android.build.gradle.internal.scope.VariantScope
import com.bytedance.android.plugin.model.SigningConfig
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File

/**
 * Created by YangJing on 2020/01/06 .
 * Email: yangjing.yeoh@bytedance.com
 */
internal fun getSigningConfig(project: Project, variantScope: VariantScope): SigningConfig {
    val agpVersion = getAGPVersion(project)
    // get signing config
    return when {
        // AGP3.2+: use VariantScope.getVariantConfiguration.getSigningConfig
        agpVersion.startsWith("3.") -> {
            getSigningConfigForAGP3(project, variantScope)
        }
        // AGP4.0+: VariantScope class removed getVariantConfiguration method.
        // VariantManager add getBuildTypes method
        // Use BuildType.getSigningConfig method to get signingConfig
        else -> {
            getSigningConfigForAGP4(agpVersion, project, variantScope)
        }
    }
}

private fun getSigningConfigForAGP3(project: Project, variantScope: VariantScope): SigningConfig {
    val variantData = variantScope.variantData
    val variantConfiguration = variantData::class.java.getMethod("getVariantConfiguration").invoke(variantData)
    val signingConfig = variantConfiguration::class.java.getMethod("getSigningConfig").invoke(variantConfiguration)
    return invokeSigningConfig(signingConfig)
}

private fun getSigningConfigForAGP4(agpVersion: String, project: Project, variantScope: VariantScope): SigningConfig {
    val variantManager = getVariantManager(project)
    var buildTypes = getBuildTypesForAGPBefore4008(variantManager)
    if (buildTypes == null) {
        buildTypes = getBuildTypesForAGP4009(variantManager)
    }
    if (buildTypes == null) {
        throw IllegalStateException("AGP $agpVersion is not supported, please Please ask for an issue or pull request.")
    }
    val flavor = variantScope.variantData.name
    val buildTypeData = buildTypes[flavor] ?: throw GradleException("get buildType failed for $flavor")
    val buildType = buildTypeData::class.java.getMethod("getBuildType").invoke(buildTypeData)
    val signingConfig = buildType::class.java.getMethod("getSigningConfig").invoke(buildType)
            ?: throw GradleException("Cannot find signing config for $flavor")
    return invokeSigningConfig(signingConfig)
}

/**
 * Return SigningConfig.
 * Range: 4.* to 4.0.0-alpha08
 */
private fun getBuildTypesForAGPBefore4008(variantManager: VariantManager): Map<*, *>? {
    return try {
        variantManager::class.java.getMethod("getBuildTypes").invoke(variantManager) as Map<*, *>
    } catch (e: Exception) {
        return null
    }
}

/**
 * Return SigningConfig.
 * Range: 4.0.0-alpha09 and after all.
 */
private fun getBuildTypesForAGP4009(variantManager: VariantManager): Map<*, *>? {
    return try {
        val variantInputModelField = variantManager::class.java.getDeclaredField("variantInputModel")
        variantInputModelField.isAccessible = true
        val variantInputModel = variantInputModelField.get(variantManager)
        val buildTypesField = variantInputModel::class.java.getDeclaredField("buildTypes")
        buildTypesField.isAccessible = true
        return buildTypesField.get(variantInputModel) as Map<*, *>?
    } catch (e: Exception) {
        null
    }
}

private fun invokeSigningConfig(any: Any): SigningConfig {
    val storeFile: File = any::class.java.getMethod("getStoreFile").invoke(any) as File
    val keyAlias: String = any::class.java.getMethod("getKeyAlias").invoke(any) as String
    val keyPassword: String = any::class.java.getMethod("getKeyPassword").invoke(any) as String
    val storePassword: String = any::class.java.getMethod("getStorePassword").invoke(any) as String
    return SigningConfig(
            storeFile, storePassword, keyAlias, keyPassword
    )
}
