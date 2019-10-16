# AadResGuard
<h1 align="center">
  <img src="" height="200" width="200"/>
  <p align="center" style="font-size: 0.5em">针对 `aab` 文件的资源混淆工具</p>
</h1>

[![License](https://img.shields.io/badge/license-Apache2.0-brightgreen)](../../LICENSE)
[![Bundletool](https://img.shields.io/badge/Bundletool-0.10.0-blue)](https://github.com/google/bundletool)

[English](../../README.md) | **[简体中文](README.md)**

> 本工具由字节跳动抖音 Android 团队提供。

# 特性
- **命令行工具：** 支持命令行一键输入输出。
- **Gradle plugin：** 支持 `gradle plugin`，使用原始打包命令执行混淆。
- **资源去重：** 对重复资源文件进行合并，降低包体积。
- **Dynamic feature：** 支持 `dynamic feature` 的混淆。
- **增量混淆：** 输入 `mapping` 文件，支持增量混淆。
- **白名单：** 白名单中的资源，名称不予混淆。
- **？？？：** 展望未来，会有更多的特性支持，欢迎提交 PR & issue。

# 数据收益
**AabResGuard** 是抖音Android团队完成的资源混淆工具，目前已经在 **Tiktok、Musically、Vigo** 等多个产品上线多月，目前无相关资源问题的反馈。

详细的数据收益信息请移步 **[数据收益](DATA.md)**

# 快速开始
## #Gradle plugin
在 `build.gradle(root project)` 中进行配置
```gradle
classpath "com.bytedance.android:aabresguard-plugin:0.1.0"
```

在 `build.gradle(application)` 中配置
```gradle
apply plugin: "com.bytedance.android.aabResGuard"
aabResGuard {
    mappingFile = file("mapping.txt").toPath() // 用于增量混淆的 mapping 文件
    whiteList = [ // 白名单规则
        "*.R.raw.*",
        "*.R.drawable.icon"
    ]
    obfuscatedBundleFileName = "duplicated-app.aab" // 混淆后的文件名称，必须以 `.aab` 结尾
    mergeDuplicatedRes = true // 是否允许去除重复资源
    enableFilterFiles = true // 是否允许过滤文件
    filterList = [ // 文件过滤规则
        "*/arm64-v8a/*",
        "META-INF/*"
    ]
}
```

`aabResGuard plugin` 侵入了 `bundle` 打包流程，可以直接执行原始打包命令进行混淆。
```cmd
./gradlew clean :app:bundleDebug --stacktrace
```

## #命令行支持
### ##资源去重
根据文件 `md5` 值对重复的文件进行合并，只保留一份，然后重定向原本的资源路径索引表中的值，以达到缩减包体积的目的。
```cmd
aabresguard merge-duplicated-res --bundle=app.aab --output=merged.aab 
--storeFile=debug.store
--storePassword=android
--keyAlias=android
--keyPassword=android
```
签名信息为可选参数，如果不指定签名信息，则会使用机器中 `Android` 默认的签名文件进行签名。

### ##文件过滤
支持指定特定的文件进行过滤，目前只支持 `META-INF/` 和 `lib/` 文件夹下的过滤。
```cmd
aabresguard filter-file --bundle=app.aab --output=filtered.aab --config=config.xml
--storeFile=debug.store
--storePassword=android
--keyAlias=android
--keyPassword=android
```
配置文件 `config.xml`，过滤规则支持`正则表达式`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<resproguard>
    <filter isactive="false">
        <rule value="*/arm64-v8a/*" />
        <rule value="META-INF/*.RSA" />
    </filter>
</resproguard>
```
**适用场景：** 由于业务的需要，部分渠道需要打全量包，但是全量包会包括所有的 `so`，使用该根据可以过滤某一个纬度的 `abi`，并且不会影响 `bundletool` 的解析过程。

### ##资源混淆
对输入的 `aab` 文件进行资源混淆，并输出混淆后的 `aab` 文件，支持 `资源去重` 和 `文件过滤`。
```cmd
aabresguard obfuscate-bundle --bundle=app.aab --output=obfuscated.aab --config=config.xml --mapping=mapping.txt
--merge-duplicated-res=true
--storeFile=debug.store
--storePassword=android
--keyAlias=android
--keyPassword=android
```
配置文件 `config.xml`，白名单支持`正则表达式`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<resproguard>
    <issue id="whitelist" isactive="true">
        <path value="com.ss.android.ugc.aweme.R.raw.*" />
    </issue>
    <filter isactive="false">
        <rule value="*/arm64-v8a/*" />
        <rule value="META-INF/*.RSA" />
    </filter>
</resproguard>
```

## # 输出文件
在打包完成后会输出混淆后的文件和相应的日志文件。
- **resources-mapping.txt：** 资源混淆 mapping，可作为下次混淆输入以达到增量混淆的目的。
- **aab：** 优化后的 aab 文件。
- **-duplicated.txt：** 被去重的文件日志记录。

关于输出日志格式和更多的输出文件信息请移步 **[输出文件](OUTPUT.md)** 。

# [版本日志](CHANGELOG.md)

# 代码贡献
阅读以下内容，了解如何参与改进 **AabResGuard**。
## #贡献指南
查看我们的 **[贡献指南](CONTRIBUTOR.md)** 来了解我们的开发流程。
## #贡献者
* [JingYeoh](https://github.com/JingYeoh)
* [Jun Li]()
* [Zilai Jiang](https://github.com/Zzzia)

