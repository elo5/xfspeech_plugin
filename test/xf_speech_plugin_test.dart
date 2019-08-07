import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:xf_speech_plugin/xf_speech_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('xf_speech_plugin');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
//    expect(await XfSpeechPlugin.platformVersion, '42');
  });
}
