**[English](COMMAND.md)** | [简体中文](../zh-cn/COMMAND.md)

# Command line

> **AabResGuard** provides a jar file that can run resource obfuscation by command line.

## Merge duplicated resources
The duplicate files will be merged according to the file `md5` value, only one file will be retained, and then the values in the original resource path index table will be redirected to reduce the volume of the package.
```cmd
aabresguard merge-duplicated-res --bundle=app.aab --output=merged.aab 
--storeFile=debug.store
--storePassword=android
--keyAlias=android
--keyPassword=android
```
The signature information is optional. If you do not specify the signature information, it will be signed using the `Android` default signature file on the PC.

## File filtering
Support for specifying specific files for filtering. Currently only filtering under the `META-INF/` and `lib/` folders is supported.
```cmd
aabresguard filter-file --bundle=app.aab --output=filtered.aab --config=config.xml
--storeFile=debug.store
--storePassword=android
--keyAlias=android
--keyPassword=android
```

Configuration file `config.xml`, filtering rules support `regular expressions`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<resproguard>
    <filter isactive="false">
        <rule value="*/arm64-v8a/*" />
        <rule value="META-INF/*.RSA" />
    </filter>
</resproguard>
```
**Applicable scenarios:** Due to the needs of the business, some channels need to make a full package, but the full package will include all `so` files, `files filter` can be used to filter the `abi` of a certain latitude and will not affect `bundletool` process.

## Resources obfuscation
Resource aliasing of the input `aab` file, and outputting the obfuscated `aab` file, supporting `Merge duplicated resources` and `file filtering`.
```cmd
aabresguard obfuscate-bundle --bundle=app.aab --output=obfuscated.aab --config=config.xml --mapping=mapping.txt
--merge-duplicated-res=true
--storeFile=debug.store
--storePassword=android
--keyAlias=android
--keyPassword=android
```

Configuration file `config.xml`, whitelist support `regular expressions`
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

## String filtering
Specify a line-by-line split string list file to filter out value and translations if name is matched in the string resource type
```cmd
aabresguard filter-string --bundle=app.aab --output=filtered.aab --config=config.xml
--storeFile=debug.store
--storePassword=android
--keyAlias=android
--keyPassword=android
```
Configuration file `config.xml`
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


## #Parameter Description
For the description of the parameters, please execute the following command:

```cmd
aabresguard help
```