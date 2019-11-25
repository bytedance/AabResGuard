[English](../en/DATA.md) | **[简体中文](DATA.md)**

# 数据收益
**AabResGuard** 于2019年六月研发完成，于2019年七月底在 `Tiktok`、`Vigo` 等多个海外产品上线，
为海外产品提供了资源保护和包大小优化的能力。

目前未收到相关资源方面的问题反馈，由于研发流程的一些原因，**AabResGuard** 在资源混淆的基础上由提供了额外的其他命令的支持，
做到了命令之间独立运行的能力，并且直接提供 `jar` 包，为 `CI` 提供便利支持。
目前在多个产品的收益数据如下所示：（解析 apk 的配置固定）

>由于每个应用对资源的优化程度不同，所以该数据在不同的应用上的优化不同，以实际数据为准。

**AabResGuard-0.1.0**

|产品|运行时间|aab size|apk raw size|apk download size|
|---|-------|--------|-------------|----------------|
|Tiktok/840|75s|-2.9MB|-1.9MB|-0.7MB|
|Vigo/v751|60s|-1.0Mb|-1.4MB|-0.6MB|


**`device-spec` 配置：**
```json
{
  "supportedAbis": ["armeabi-v7a"],
  "supportedLocales": ["zh-CN", "en-US", "ja-JP", "zh-HK", "zh-TW"],
  "screenDensity": 480,
  "sdkVersion": 16
}
```
