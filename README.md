# AabResGuard
<h1 align="center">
  <img src="wiki/images/logo.png" height="220" width="460"/>
  <p align="center" style="font-size: 0.3em">The tool of obfuscated aab resources</p>
</h1>

[ ![Download](https://api.bintray.com/packages/yeoh/maven/aabresguard-core/images/download.svg) ](https://bintray.com/yeoh/maven/aabresguard-plugin/)
[![License](https://img.shields.io/badge/license-Apache2.0-brightgreen)](LICENSE)
[![Bundletool](https://img.shields.io/badge/Dependency-Bundletool/0.10.0-blue)](https://github.com/google/bundletool)

**[English](README.md)** | [简体中文](wiki/zh-cn/README.md)

> Powered by bytedance douyin android team.

## Features
> The tool of obfuscated aab resources.

- **Merge duplicated resources:** Consolidate duplicate resource files to reduce package size.
- **Filter bundle files:** Support for filtering files in the `bundle` package. Currently only supports filtering in the `MATE-INFO/` and `lib/` paths.
- **White list:** The resources in the whitelist are not to be obfuscated.
- **Incremental obfuscation:** Input the `mapping` file to support incremental obfuscation.
- **Remove string:** Input the unused file splits by lines to support remove strings.
- **???:** Looking ahead, there will be more feature support, welcome to submit PR & issue.

## [Data of size savings](wiki/en/DATA.md)
**AabResGuard** is a resource obfuscation tool powered by the douyin Android team. It has been launched at the end of July 2018 in several overseas products, such as **Tiktok, Vigo**, etc. 
There is no feedback on related resource issues. 
For more data details, please go to **[Data of size savings](wiki/en/DATA.md)**.

## Quick start
- **Command tool:** Support command line.
- **Gradle plugin:** Support for `gradle plugin`, using the original packaging command to execute obfuscation.

### Gradle plugin
Configured in `build.gradle(root project)`
```gradle
buildscript {
  repositories {
    mavenCentral()
    jcenter()
    google()
   }
  dependencies {
    classpath "com.bytedance.android:aabresguard-plugin:0.1.0"
  }
}
```

Configured in `build.gradle(application)`
```gradle
apply plugin: "com.bytedance.android.aabResGuard"
aabResGuard {
    mappingFile = file("mapping.txt").toPath() // Mapping file used for incremental obfuscation
    whiteList = [ // White list rules
        "*.R.raw.*",
        "*.R.drawable.icon"
    ]
    obfuscatedBundleFileName = "duplicated-app.aab" // Obfuscated file name, must end with '.aab'
    mergeDuplicatedRes = true // Whether to allow the merge of duplicate resources
    enableFilterFiles = true // Whether to allow filter files
    filterList = [ // file filter rules
        "*/arm64-v8a/*",
        "META-INF/*"
    ]
    
    enableFilterStrings = false // switch of filter strings
    unusedStringPath = file("unused.txt").toPath() // strings will be filtered in this file
    languageWhiteList = ["en", "zh"] // keep en,en-xx,zh,zh-xx etc. remove others.
}
```

The `aabResGuard plugin` intrudes the `bundle` packaging process and can be obfuscated by executing the original packaging commands.
```cmd
./gradlew clean :app:bundleDebug --stacktrace
```

Get the obfuscated `bundle` file path by `gradle` .
```groovy
def aabResGuardPlugin = project.tasks.getByName("aabresguard${VARIANT_NAME}")
Path bundlePath = aabResGuardPlugin.getObfuscatedBundlePath()
```

### [Whitelist](wiki/en/WHITELIST.md)
The resources that can not be confused. Welcome PR your configs which is not included in [WHITELIST](wiki/en/WHITELIST.md)

### [Command line](wiki/en/COMMAND.md)
**AabResGuard** provides a `jar` file that can be executed directly from the command line. More details, please go to **[Command Line](wiki/en/COMMAND.md)**.

### [Output](wiki/en/OUTPUT.md)
After the packaging is completed, the obfuscated file and the log files will be output. More details, please go to **[Output File](wiki/en/OUTPUT.md)**.
- **resources-mapping.txt:** Resource obfuscation mapping, which can be used as the next obfuscation input to achieve incremental obfuscate.
- **aab:** Optimized aab file.
- **-duplicated.txt:** duplicated file logging.

## [Change log](wiki/en/CHANGELOG.md)
Version change log. More details, please go to **[Change Log](wiki/en/CHANGELOG.md)** .

## [Contribute](wiki/en/CONTRIBUTOR.md)
Read the details to learn how to participate in the improvement **AabResGuard**.

### Contributor
* [JingYeoh](https://github.com/JingYeoh)
* [Jun Li]()
* [Zilai Jiang](https://github.com/Zzzia)
* [Zhiqian Yang](https://github.com/yangzhiqian)
* [Xiaoshuang Bai (Designer)](https://www.behance.net/shawnpai)

## Thanks
* [AndResGuard](https://github.com/shwenzhang/AndResGuard/)
* [BundleTool](https://github.com/google/bundletool)
