**[English](DATA.md)** | [简体中文](../zh-cn/DATA.md)

# Data of size savings
**AabResGuard** was developed in June 2019 and launched at the end of July 2019 in several overseas products such as `Tiktok`, `Vigo`.
Provides resource protection and package size optimization capabilities for overseas products.

At present, no feedback has been received on related resources. Due to some reasons for the R&D process, **AabResGuard** is supported by additional commands based on resource obfuscation.
It has the ability to run by command line, and provides the `jar` package directly to provide convenient support for `CI`.
The current data of size savings for multiple products is below: 

>Since each application has different levels of optimization for resources, the optimization of the data in different applications is different, and the actual data is subject to change.

**AabResGuard-0.1.0**

|App|coast time|aab size|apk raw size|apk download size|
|---|-------|--------|-------------|----------------|
|Tiktok/840|75s|-2.9MB|-1.9MB|-0.7MB|
|Vigo/v751|60s|-1.0Mb|-1.4MB|-0.6MB|


**`device-spec` Configuration:**
```json
{
  "supportedAbis": ["armeabi-v7a"],
  "supportedLocales": ["zh-CN", "en-US", "ja-JP", "zh-HK", "zh-TW"],
  "screenDensity": 480,
  "sdkVersion": 16
}
```
