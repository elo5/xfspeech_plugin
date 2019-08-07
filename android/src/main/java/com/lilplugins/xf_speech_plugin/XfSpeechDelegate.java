package com.lilplugins.xf_speech_plugin;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public class XfSpeechDelegate implements PluginRegistry.RequestPermissionsResultListener {

    static String TAG = XfSpeechDelegate.class.getSimpleName();

    private final PermissionManager permissionManager;
    private final Activity activity;
    private MethodChannel.Result pendingResult;
    private MethodChannel channel;
    private MethodCall methodCall;

    private SpeechRecognizer recognizer;
    private SpeechSynthesizer synthesizer;
    private String filePath;
    private StringBuilder resultBuilder = new StringBuilder();

    @VisibleForTesting
    static final int REQUEST_RECORD_AUDIO_PERMISSION = 1000;

    interface PermissionManager {
        boolean isPermissionGranted(String permissionName);
        void askForPermission(String permissionName, int requestCode);
    }

    @VisibleForTesting
    XfSpeechDelegate(
            final Activity activity,
            final MethodChannel channel,
            final MethodChannel.Result result,
            final MethodCall methodCall,
            final PermissionManager permissionManager
    ){
        this.activity = activity;
        this.pendingResult = result;
        this.methodCall = methodCall;
        this.permissionManager = permissionManager;
        this.channel = channel;
    }

    public XfSpeechDelegate( final Activity activity, final MethodChannel channel){
        this(
                activity,
                channel,
                null,
                null,
                new PermissionManager() {
                    @Override
                    public boolean isPermissionGranted(String permissionName) {
                        return ActivityCompat.checkSelfPermission(activity, permissionName) == PackageManager.PERMISSION_GRANTED;
                    }

                    @Override
                    public void askForPermission(String permissionName, int requestCode) {
                        ActivityCompat.requestPermissions(activity, new String[] {permissionName}, requestCode);
                    }
                });
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                if (permissionGranted) {
                    startListeningMethod();
                }
                break;

            default:
                return false;
        }
        return true;
    }

    public void initWithAppId(MethodCall call, MethodChannel.Result result) {
        Log.e(TAG, call.method);
        methodCall = call;
        pendingResult = result;

        SpeechUtility.createUtility(activity.getApplicationContext(), SpeechConstant.APPID + "=" + call.arguments);
        Setting.setLocationEnable(false);
        recognizer = SpeechRecognizer.createRecognizer(activity.getApplicationContext(), new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    Log.e(TAG, "Failed to init SpeechRecognizer  Code: " + code);
                }else  Log.e(TAG, "Init SpeechRecognizer Success" );
            }
        });

        synthesizer = SpeechSynthesizer.createSynthesizer(activity.getApplicationContext(), new InitListener() {
            @Override
            public void onInit(int i) {
                if (i != ErrorCode.SUCCESS) {
                    Log.e(TAG, "Failed to init SpeechSynthesizer Code: " + i);
                }else  Log.e(TAG, "Init SpeechSynthesizer Success" );
            }
        });

        result.success(null);
    }

    public void setParameter(MethodCall call, MethodChannel.Result result) {
        methodCall = call;
        pendingResult = result;
        Log.e(TAG, "setParameter");
        if (recognizer == null) {
            Log.e(TAG, "recongnizer为null");
        } else {
            try {
                Map<String, String> map = (Map<String, String>) call.arguments;
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    if (entry.getKey().equals(SpeechConstant.ASR_AUDIO_PATH)) {
                        filePath = Environment.getExternalStorageDirectory() + "/msc/" + entry.getValue();
                        recognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, filePath);
                    } else {
                        recognizer.setParameter(entry.getKey(), entry.getValue());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (synthesizer == null) {
            Log.e(TAG, "synthesizer 为 null");
        } else {
            try {
                Map<String, String> map = (Map<String, String>) call.arguments;
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    if (entry.getKey().equals(SpeechConstant.ASR_AUDIO_PATH)) {
//                        filePath = Environment.getExternalStorageDirectory() + "/msc/" + entry.getValue();
//                        recognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, filePath);
                    } else {
                        synthesizer.setParameter(entry.getKey(), entry.getValue());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        result.success(null);
    }

    public void startListening(MethodCall call, MethodChannel.Result result) {
        methodCall = call;
        pendingResult = result;
        if (!permissionManager.isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
            permissionManager.askForPermission(
                    Manifest.permission.RECORD_AUDIO, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }
        startListeningMethod();
    }

    private void startListeningMethod() {
        if (recognizer == null){
            Log.e(TAG, "SpeechRecognizer hasn't been init");
            pendingResult.error("","SpeechRecognizer hasn't been init", "SpeechRecognizer is null");
        }
        else {
            int code = recognizer.startListening(mRecognizerListener);
            if (code != ErrorCode.SUCCESS)  Log.e(TAG, "SpeechRecognizer => startListening error: " + code);
        }
        pendingResult.success(null);
    }

    public void stopListening(MethodCall call, MethodChannel.Result result) {
        methodCall = call;
        pendingResult = result;
        if (recognizer == null){
            Log.e(TAG, "SpeechRecognizer hasn't been init");
            result.error("","SpeechRecognizer hasn't been init", "SpeechRecognizer is null");
        }else{
            recognizer.stopListening();
            result.success(null);
        }
    }

    public void cancelListening(MethodCall call, MethodChannel.Result result) {
        methodCall = call;
        pendingResult = result;
        if (recognizer == null){
            Log.e(TAG, "SpeechRecognizer hasn't been init");
            result.error("","SpeechRecognizer hasn't been init", "SpeechRecognizer is null");
        }else  recognizer.cancel();
        result.success(null);
    }

    public void dispose(MethodCall call, MethodChannel.Result result) {
        methodCall = call;
        pendingResult = result;
        if (recognizer == null){
            Log.e(TAG, "SpeechRecognizer hasn't been init");
            result.error("","SpeechRecognizer hasn't been init", "SpeechRecognizer is null");
        } else {
            if(recognizer.isListening())recognizer.cancel();
            recognizer.destroy();
            recognizer = null;
        }

        if (synthesizer == null){
            Log.e(TAG, "SpeechSynthesizer hasn't been init");
            result.error("","SpeechSynthesizer hasn't been init", "SpeechSynthesizer is null");
        } else {
            if(synthesizer.isSpeaking())synthesizer.stopSpeaking();
            synthesizer.destroy();
            synthesizer = null;
        }

        result.success(null);
    }


    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {
            Log.d(TAG, "onBeginOfSpeech()");
            channel.invokeMethod("onBeginOfSpeech", null);
        }

        @Override
        public void onError(SpeechError error) {
            Log.d(TAG, "onError():" + error.getPlainDescription(true));

            HashMap errorInfo = new HashMap();
            errorInfo.put("code", error.getErrorCode());
            errorInfo.put("desc", error.getErrorDescription());
            ArrayList<Object> arguments = new ArrayList<>();
            arguments.add(errorInfo);
            arguments.add(filePath);
            channel.invokeMethod("onCompleted", arguments);
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech()");
            channel.invokeMethod("onEndOfSpeech", null);
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {

            String result = resultBuilder.append(results.getResultString()).toString();
            Log.d(TAG, "onResult():" + result);

            ArrayList<Object> arguments = new ArrayList<>();
            arguments.add(result);
            arguments.add(isLast);
            channel.invokeMethod("onResults", arguments);

            if (isLast) {
                resultBuilder.delete(0,resultBuilder.length());

                ArrayList<Object> args = new ArrayList<>();
                arguments.add(null);
                arguments.add(filePath);
                channel.invokeMethod("onCompleted", args);
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            channel.invokeMethod("onVolumeChanged", volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };


    public void startSpeaking(MethodCall call, MethodChannel.Result result) {
        methodCall = call;
        pendingResult = result;

        if (synthesizer == null){
            Log.e(TAG, "SpeechSynthesize hasn't been init");
            pendingResult.error("","SpeechSynthesize hasn't been init", "SpeechSynthesize is null");
        }
        else {
            int code = synthesizer.startSpeaking(call.arguments.toString(),mSynthesizerListener);
            if (code != ErrorCode.SUCCESS)  Log.e(TAG, "SpeechSynthesize => startSpeaking error: " + code);
        }
        pendingResult.success(null);
    }

    public void pauseSpeaking(MethodCall call, MethodChannel.Result result) {
        methodCall = call;
        pendingResult = result;

        if (synthesizer == null){
            Log.e(TAG, "SpeechSynthesize hasn't been init");
            pendingResult.error("","SpeechSynthesize hasn't been init", "SpeechSynthesize is null");
        }
        else {
            if(synthesizer.isSpeaking())synthesizer.pauseSpeaking();
        }
        pendingResult.success(null);
    }

    public void resumeSpeaking(MethodCall call, MethodChannel.Result result) {
        methodCall = call;
        pendingResult = result;

        if (synthesizer == null){
            Log.e(TAG, "SpeechSynthesize hasn't been init");
            pendingResult.error("","SpeechSynthesize hasn't been init", "SpeechSynthesize is null");
        }
        else {
            synthesizer.resumeSpeaking();
        }
        pendingResult.success(null);
    }

    public void stopSpeaking(MethodCall call, MethodChannel.Result result) {
        methodCall = call;
        pendingResult = result;

        if (synthesizer == null){
            Log.e(TAG, "SpeechSynthesize hasn't been init");
            pendingResult.error("","SpeechSynthesize hasn't been init", "SpeechSynthesize is null");
        }
        else {
            synthesizer.stopSpeaking();
        }
        pendingResult.success(null);
    }


    private SynthesizerListener mSynthesizerListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            channel.invokeMethod("onSpeakBegin", null);
        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {
            ArrayList<Object> args = new ArrayList<>();
            args.add(i);
            args.add(i1);
            args.add(i2);
            args.add(s);
            channel.invokeMethod("onBufferProgress", args);
        }

        @Override
        public void onSpeakPaused() {
            channel.invokeMethod("onSpeakPaused", null);
        }

        @Override
        public void onSpeakResumed() {
            channel.invokeMethod("onSpeakResumed", null);
        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {
            ArrayList<Object> args = new ArrayList<>();
            args.add(i);
            args.add(i1);
            args.add(i2);
            channel.invokeMethod("onSpeakProgress", args);
        }

        @Override
        public void onCompleted(SpeechError speechError) {
            channel.invokeMethod("onCompleted", null);
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

}