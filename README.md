# xf_speech_plugin

flutter 讯飞语音插件，包含语音识别、语音合成

有些不完善的地方，有兴趣可以自己修改哈

### 注意事项：

讯飞是根据不同appId打包不同的SDK的，所以，

1. Android中请替换android->libs中的Msc.jar文件，以及android->src->main->jniLibs里面的文件

2. iOS请替换掉ios->Frameworks里面的文件

3. initWithAppId 中使用对应的appid

参考了 https://pub.dev/packages/xfvoice, 非常感谢

ios 13.3系统真机有问题，所以可以使用低一点的系统版本真机测试
https://github.com/flutter/flutter/issues/49504
https://github.com/flutter/flutter/issues/49504#issuecomment-581554697