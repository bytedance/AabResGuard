package com.bytedance.android.plugin.internal

import com.android.build.gradle.internal.scope.VariantScope
import com.bytedance.android.plugin.model.SigningConfig
import org.gradle.api.Project
import java.io.File

/**
 * Created by YangJing on 2020/01/06 .
 * Email: yangjing.yeoh@bytedance.com
 */
internal fun getSigningConfig(project: Project, variantScope: VariantScope): SigningConfig {
    val agpVersion = getAGPVersion(project)
    // get signing config
    val variantData = variantScope.variantData
    val variantDslInfoMethodName = if (agpVersion.startsWith("3.")) {
        // AGP3.2+: use VariantScope.getVariantConfiguration.getSigningConfig
        "getVariantConfiguration"
    } else{
        // AGP4.0+: use VariantScope.getVariantDslInfo.getSigningConfig
        "getVariantDslInfo"
    }
    val variantDslInfo = variantData::class.java.getMethod(variantDslInfoMethodName).invoke(variantData)
    val signingConfig = variantDslInfo::class.java.getMethod("getSigningConfig").invoke(variantDslInfo)
    return invokeSigningConfig(signingConfig)
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
