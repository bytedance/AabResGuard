package com.bytedance.android.plugin.model

import java.io.File

/**
 * Created by YangJing on 2020/01/07 .
 * Email: yangjing.yeoh@bytedance.com
 */
data class SigningConfig(
        val storeFile: File?,
        val storePassword: String?,
        val keyAlias: String?,
        val keyPassword: String?
)