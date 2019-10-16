**[English](OUTPUT.md)** | [简体中文](../zh-cn/OUTPUT.md)
# 输出文件

>混淆后的文件输出目录和 bundle 打包后输出的文件目录一致，均在 `build/outputs/bundle/{flavor}/` 目录下。

混淆后的输出文件如下图所示：

![output](../images/output.png)

## resources-mapping
用于记录资源混淆规则的日志文件，示例如下：

```txt
res dir mapping:
	res/color-v21 -> res/c
	res/color-v23 -> res/d
	res/anim -> res/a

res id mapping:
	0x7f0c00ba : com.bytedance.android.app.R.style.RtlUnderlay.Widget.AppCompat.ActionButton.Overflow -> com.bytedance.android.app.R.style.eb
	0x7f040002 : com.bytedance.android.app.R.color.abc_btn_colored_borderless_text_material -> com.bytedance.android.app.R.color.c
	0x7f0c00d5 : com.bytedance.android.app.R.style.TextAppearance.AppCompat.Title -> com.bytedance.android.app.R.style.f2
	0x7f0c0022 : com.bytedance.android.app.R.style.Base.TextAppearance.AppCompat.Small.Inverse -> com.bytedance.android.app.R.style.a8

res entries path mapping:
	0x7f060030 : base/res/drawable-xxhdpi-v4/abc_list_selector_disabled_holo_dark.9.png -> res/h/z.9.png
	0x7f060022 : base/res/drawable-xxxhdpi-v4/abc_ic_star_half_black_16dp.png -> res/k/o.png
```

- **res dir mapping：** 存储资源文件目录的混淆规则。格式：dir -> dir（`res/` 根目录不可以被混淆）
- **res id mapping：** 存储资源名称的混淆规则。格式：resourceId : resourceName -> resourceName（增量混淆时，resourceId 不会被读入）
- **res entries path mapping：** 存储资源文件路径的混淆规则。格式：resourceId : path -> path（增量混淆时，resourceId 不会被读入）

## -duplicated.txt
用于记录被去重的资源文件，示例如下：

```txt
res filter path mapping:
	res/drawable-hdpi-v4/abc_list_divider_mtrl_alpha.9.png -> res/drawable-mdpi-v4/abc_list_divider_mtrl_alpha.9.png (size 167B)
	res/color-v23/abc_tint_spinner.xml -> res/color-v23/abc_tint_edittext.xml (size 942B)
	res/drawable-xhdpi-v4/abc_list_divider_mtrl_alpha.9.png -> res/drawable-mdpi-v4/abc_list_divider_mtrl_alpha.9.png (size 167B)
removed: count(3), totalSize(1.2KB)
```
