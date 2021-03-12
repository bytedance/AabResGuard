package com.bytedance.android.plugin.internal

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.VariantManager
import com.bytedance.android.plugin.model.SigningConfig
import org.gradle.api.Project
import java.io.File

/**
 * Created by YangJing on 2020/01/06 .
 * Email: yangjing.yeoh@bytedance.com
 */
internal fun getSigningConfig(project: Project, variant: ApplicationVariant): SigningConfig {
    val agpVersion = getAGPVersion(project)
    // get signing config
    return when {
        // AGP3.2+: use VariantScope.getVariantConfiguration.getSigningConfig
        agpVersion.startsWith("3.") -> {
            getSigningConfigForAGP3(project, variant)
        }
        // AGP4.0+: VariantScope class removed getVariantConfiguration method.
        // VariantManager add getBuildTypes method
        // Use BuildType.getSigningConfig method to get signingConfig
        else -> {
            getSigningConfigForAGP4(agpVersion, project, variant)
        }
    }
}

private fun getSigningConfigForAGP3(project: Project, variant: ApplicationVariant): SigningConfig {
    return getSigningConfigByAppVariant(variant)
}

private fun getSigningConfigForAGP4(agpVersion: String, project: Project, variant: ApplicationVariant): SigningConfig {
    return getSigningConfigByAppVariant(variant)
}

private fun getSigningConfigByAppVariant(variant: ApplicationVariant): SigningConfig {
    return SigningConfig(variant.signingConfig.storeFile, variant.signingConfig.storePassword, variant.signingConfig.keyAlias, variant.signingConfig.keyPassword)
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
        val buildTypesField = variantInputModel::class.java.getField("buildTypes")
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
