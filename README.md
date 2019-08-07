# xf_speech_plugin

flutter 讯飞语音插件，包含语音识别、语音合成

有些不完善的地方，有兴趣可以自己修改哈

### 注意事项：

讯飞是根据不同appId打包不同的SDK的，所以，

Android中请替换android->libs中的Msc.jar文件，以及android->src->main->jniLibs里面的文件

iOS请替换掉ios->Frameworks里面的文件

参考了 https://pub.dev/packages/xfvoice, 非常感谢