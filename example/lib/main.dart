import 'package:flutter/material.dart';
import 'dart:async';
import 'package:xf_speech_plugin/xf_speech_plugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String iflyResultString = '点击+开始，点击-结束';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    final voice = XfSpeechPlugin.instance;
    voice.initWithAppId(iosAppID: 'your appid', androidAppID: 'your appid');
    final param = new XFVoiceParam();
    param.domain = 'iat';
    param.asr_ptt = '0';
    param.asr_audio_path = 'xme.pcm';
    param.result_type = 'plain';
    param.voice_name = 'vixx';
//    param.voice_name = 'xiaoyan';

    voice.setParameter(param.toMap());
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: GestureDetector(
            child: Text(iflyResultString),
          ),
        ),

        floatingActionButton: Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: <Widget>[
            FloatingActionButton(
              onPressed: onTapDown,
              tooltip: 'Increment',
              child: Icon(Icons.add),
            ),
            FloatingActionButton(
              onPressed: onTapUp,
              tooltip: 'Decrement',
              child: Icon(Icons.remove),
            ),
          ],
        ),
      ),
    );
  }

  onTapDown() {
    print("tap down");

    iflyResultString = '';
    final listen = XfSpeechListener(
        onVolumeChanged: (volume) {
          print('$volume');
        },
        onResults: (String result, isLast) {
          if (result.length > 0) {
            setState(() {
              iflyResultString += result;
              XfSpeechPlugin.instance.startSpeaking(
                  string: "你刚才说了" + iflyResultString);
            });
          }
        },
        onCompleted: (Map<dynamic, dynamic> errInfo, String filePath) {
          setState(() {

          });
        }
    );
    XfSpeechPlugin.instance.startListening(listener: listen);
  }

  onTapUp() {
    print("tap up");
    XfSpeechPlugin.instance.stopListening();
  }
}