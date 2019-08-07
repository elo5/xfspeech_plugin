package com.lilplugins.xf_speech_plugin;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

///** XfSpeechPlugin */
//public class XfSpeechPlugin implements MethodCallHandler {
//  /** Plugin registration. */
//  public static void registerWith(Registrar registrar) {
//    final MethodChannel channel = new MethodChannel(registrar.messenger(), "xf_speech_plugin");
//    channel.setMethodCallHandler(new XfSpeechPlugin());
//  }
//
//  @Override
//  public void onMethodCall(MethodCall call, Result result) {
//    if (call.method.equals("getPlatformVersion")) {
//      result.success("Android " + android.os.Build.VERSION.RELEASE);
//    } else {
//      result.notImplemented();
//    }
//  }
//}



import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class XfSpeechPlugin implements MethodCallHandler{

  private static String TAG = XfSpeechPlugin.class.getSimpleName();
  private static final String CHANNEL = "lilplugins.com/xf_speech_plugin";

//  static Activity activity;
  PluginRegistry.Registrar registrar;
  XfSpeechDelegate delegate;

  XfSpeechPlugin(final PluginRegistry.Registrar registrar, final XfSpeechDelegate delegate) {
    this.registrar = registrar;
    this.delegate = delegate;
  }

  public static void registerWith(Registrar registrar) {
    if (registrar.activity() == null) {
      return;
    }
//    activity = registrar.activity();
    final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL);
    final XfSpeechDelegate delegate = new XfSpeechDelegate(registrar.activity(),channel);
    registrar.addRequestPermissionsResultListener(delegate);
    final XfSpeechPlugin instance = new XfSpeechPlugin(registrar, delegate);
    channel.setMethodCallHandler(instance);
  }

  static final String METHOD_CALL_INITWITHAPPID = "initWithAppId";
  static final String METHOD_CALL_SETPARAMETER = "setParameter";

  static final String METHOD_CALL_STARTLISTENING = "startListening";
//  static final String METHOD_CALL_WRITEAUDIO = "writeAudio";
  static final String METHOD_CALL_STOPLISTENING = "stopListening";
//  static final String METHOD_CALL_ISLISTENING = "isListening";
  static final String METHOD_CALL_CANCELLISTENING = "cancelListening";


  static final String METHOD_CALL_STARTSPEAKING = "startSpeaking";
  static final String METHOD_CALL_PAUSESPEAKING = "pauseSpeaking";
  static final String METHOD_CALL_RESUMESPEAKING = "resumeSpeaking";
  static final String METHOD_CALL_STOPSPEAKING = "stopSpeaking";
  static final String METHOD_CALL_ISSPEAKING = "isSpeaking";


  static final String METHOD_CALL_DISPOSE = "dispose";


  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (registrar.activity() == null) {
      result.error("no_activity", "image_cropper plugin requires a foreground activity.", null);
      return;
    }

    Log.e(TAG, call.method);
//        MethodChannel.Result result = new MethodResultWrapper(rawResult);
    switch (call.method) {
      case METHOD_CALL_INITWITHAPPID:
        delegate.initWithAppId(call, result);  // recongnnizer & synthesizer
        break;
      case METHOD_CALL_SETPARAMETER:  // recongnnizer & synthesizer
        delegate.setParameter(call, result);
        break;
      case METHOD_CALL_DISPOSE: // recongnnizer & synthesizer
        delegate.dispose(call, result);
        break;


      case METHOD_CALL_STARTLISTENING:
        delegate.startListening(call, result);
        break;
      case METHOD_CALL_STOPLISTENING:
        delegate.stopListening(call, result);
        break;
      case METHOD_CALL_CANCELLISTENING:
        delegate.cancelListening(call, result);
        break;

      case METHOD_CALL_STARTSPEAKING:
        delegate.startSpeaking(call, result);
        break;
      case METHOD_CALL_PAUSESPEAKING:
        delegate.pauseSpeaking(call, result);
        break;
      case METHOD_CALL_RESUMESPEAKING:
        delegate.resumeSpeaking(call, result);
        break;
      case METHOD_CALL_STOPSPEAKING:
        delegate.stopSpeaking(call, result);
        break;

      default:
        throw new IllegalArgumentException("Unknown method " + call.method);
    }
  }

//  // MethodChannel.Result wrapper that responds on the platform thread.
//  private static class MethodResultWrapper implements MethodChannel.Result {
//    private MethodChannel.Result methodResult;
//    private Handler handler;
//
//    MethodResultWrapper(MethodChannel.Result result) {
//      methodResult = result;
//      handler = new Handler(Looper.getMainLooper());
//    }
//
//    @Override
//    public void success(final Object result) {
//      handler.post(
//              new Runnable() {
//                @Override
//                public void run() {
//                  methodResult.success(result);
//                }
//              });
//    }
//
//    @Override
//    public void error(
//            final String errorCode, final String errorMessage, final Object errorDetails) {
//      handler.post(
//              new Runnable() {
//                @Override
//                public void run() {
//                  methodResult.error(errorCode, errorMessage, errorDetails);
//                }
//              });
//    }
//
//    @Override
//    public void notImplemented() {
//      handler.post(
//              new Runnable() {
//                @Override
//                public void run() {
//                  methodResult.notImplemented();
//                }
//              });
//    }
//  }
}
