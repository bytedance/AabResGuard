[English](../en/COMMAND.md) | **[简体中文](COMMAND.md)**

# 命令行支持

> **AabResGuard** 提供了 jar 包，可以直接通过命令行来运行资源混淆。

## 资源去重
根据文件 `md5` 值对重复的文件进行合并，只保留一份，然后重定向原本的资源路径索引表中的值，以达到缩减包体积的目的。
```cmd
aabresguard merge-duplicated-res --bundle=app.aab --output=merged.aab 
--storeFile=debug.store
--storePassword=android
--keyAlias=android
--keyPassword=android
```
签名信息为可选参数，如果不指定签名信息，则会使用机器中 `Android` 默认的签名文件进行签名。

## 文件过滤
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

## 资源混淆
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

## 文案过滤
指定一个按行分割的字符串列表文件，过滤掉string资源类型中name匹配的文案及翻译
```cmd
aabresguard filter-string --bundle=app.aab --output=filtered.aab --config=config.xml
--storeFile=debug.store
--storePassword=android
--keyAlias=android
--keyPassword=android
```
配置文件 `config.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<resproguard>
    <filter-str isactive="false">
        <!--remove strings in file-->
        <path value="unused.txt" />
        <!--keep strings by language such as en, en-xx, and remove others-->
        <language value="en" />
        <language value="zh" />
    </filter-str>
</resproguard>
```


## 参数说明
参数的说明请执行以下命令来进行查看：

```cmd
aabresguard help
```