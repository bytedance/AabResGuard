package com.bytedance.android.plugin.internal

import com.android.build.gradle.api.ApplicationVariant
import com.bytedance.android.plugin.model.SigningConfig
import org.gradle.api.Project

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
