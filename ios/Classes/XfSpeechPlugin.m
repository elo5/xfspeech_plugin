#import "XfSpeechPlugin.h"
#import <iflyMSC/iflyMSC.h>
#import <objc/runtime.h>

static FlutterMethodChannel *_channel = nil;

@interface XfSpeechPlugin () <IFlySpeechRecognizerDelegate,IFlySpeechSynthesizerDelegate>
@property (nonatomic, strong) NSString *resultString;
@end

@implementation XfSpeechPlugin {
//    FlutterMethodChannel * _channel;
}
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"lilplugins.com/xf_speech_plugin"
                                     binaryMessenger:[registrar messenger]];
    XfSpeechPlugin* instance = [[XfSpeechPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
    _channel = channel;
}

static NSString * const _METHOD_INITWITHAPPID = @"initWithAppId";
static NSString * const _METHOD_SETPARAMETER = @"setParameter";
static NSString * const _METHOD_DISPOSE = @"dispose";

/// for SpeechRecogniaer only
static NSString * const _METHOD_STARTLISTENING = @"startListening";
static NSString * const _METHOD_STOPLISTENING = @"stopListening";
static NSString * const _METHOD_CANCELLISTENING = @"cancelListening";

/// for SpeechSynthesizer only
static NSString * const _METHOD_START_SPEAKING = @"startSpeaking";
static NSString * const _METHOD_PAUSE_SPEAKING = @"pauseSpeaking";
static NSString * const _METHOD_RESUME_SPEAKING = @"resumeSpeaking";
static NSString * const _METHOD_STOP_SPEAKING = @"stopSpeaking";
static NSString * const _METHOD_IS_SPEAKING = @"isSpeaking";


- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([_METHOD_INITWITHAPPID isEqualToString:call.method]) {
        [self iflyInit:call.arguments];
        
    } else if ([_METHOD_SETPARAMETER isEqualToString:call.method]) {
        [self setParameter:call.arguments];
    } else if ([_METHOD_STARTLISTENING isEqualToString:call.method]) {
        [self startListening];
    } else if ([_METHOD_STOPLISTENING isEqualToString:call.method]) {
        [self stopListening];
    } else if ([_METHOD_DISPOSE isEqualToString:call.method]) {
        [self cancelListening];
    } else if ([_METHOD_CANCELLISTENING isEqualToString:call.method]) {
        [self cancelListening];
    }
    
    
    else if ([_METHOD_START_SPEAKING isEqualToString:call.method]) {
        [self startSpeaking:call.arguments];
    }
    else if ([_METHOD_PAUSE_SPEAKING isEqualToString:call.method]) {
        [self pauseSpeaking];
    }
    else if ([_METHOD_RESUME_SPEAKING isEqualToString:call.method]) {
        [self resumeSpeaking];
    }
    else if ([_METHOD_STOP_SPEAKING isEqualToString:call.method]) {
        [self stopSpeaking];
    }
    
    
    else {
        result(FlutterMethodNotImplemented);
    }
}

#pragma mark - Bridge Actions

- (void)iflyInit:(NSString *)appId {
    NSString *initString = [[NSString alloc] initWithFormat:@"appid=%@", appId];
    [IFlySpeechUtility createUtility:initString];
    [[IFlySpeechRecognizer sharedInstance] setDelegate:self];
    [[IFlySpeechSynthesizer sharedInstance] setDelegate:self];

}

- (void)setParameter:(NSDictionary *)param {
    [param enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
        [[IFlySpeechRecognizer sharedInstance] setParameter:obj forKey:key];
        [[IFlySpeechSynthesizer sharedInstance] setParameter:obj forKey:key];
    }];
}

- (void)startListening {
    if ([[IFlySpeechRecognizer sharedInstance] isListening]) {
        return;
    }
    self.resultString = nil;
    [[IFlySpeechRecognizer sharedInstance] startListening];
}

- (void)stopListening {
    [[IFlySpeechRecognizer sharedInstance] stopListening];
}

- (void)cancelListening {
    [[IFlySpeechRecognizer sharedInstance] cancel];
}

///
- (void)startSpeaking:(NSString *)string {
    [[IFlySpeechSynthesizer sharedInstance] startSpeaking:string];
}

- (void)pauseSpeaking {
    if ([[IFlySpeechSynthesizer sharedInstance] isSpeaking]) {
        [[IFlySpeechSynthesizer sharedInstance] pauseSpeaking];
    }
}

- (void)resumeSpeaking {
    if ([[IFlySpeechSynthesizer sharedInstance] isSpeaking]) {
    }
    [[IFlySpeechSynthesizer sharedInstance] resumeSpeaking];
}

- (void)stopSpeaking {
    if ([[IFlySpeechSynthesizer sharedInstance] isSpeaking]) {
    }
    [[IFlySpeechSynthesizer sharedInstance] stopSpeaking];
}

#pragma mark - IFlySpeechRecognizerDelegate
- (void)onCompleted:(IFlySpeechError *)errorCode {
    NSDictionary *dic = NSNull.null;
    if (errorCode.errorCode != 0) {
        dic = @{@"code": @(errorCode.errorCode),
                @"type": @(errorCode.errorType),
                @"desc": errorCode.errorDesc
                };
    }

    NSString *path = [[IFlySpeechRecognizer sharedInstance] parameterForKey:@"asr_audio_path"];
    NSArray *cachePaths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory
                                                              , NSUserDomainMask
                                                              , YES);
    NSString *folder = cachePaths.firstObject;
    NSString *filePath = [folder stringByAppendingPathComponent:path];
    [_channel invokeMethod:@"onCompleted" arguments:@[dic, filePath]];
}

- (void)onResults:(NSArray *)results isLast:(BOOL)isLast {
    NSString *res = NSNull.null;
    if (results != nil) {
        NSDictionary *dic = [results firstObject];
        res = dic.allKeys.firstObject;
        self.resultString = res;
    } else {
        if (self.resultString != nil) {
            res = self.resultString;
        }
    }
    [_channel invokeMethod:@"onResults" arguments:@[res, @(isLast)]];
}

- (void)onVolumeChanged:(int)volume {
    [_channel invokeMethod:@"onVolumeChanged" arguments:@(volume)];
}

- (void)onBeginOfSpeech {
    [_channel invokeMethod:@"onBeginOfSpeech" arguments:NULL];
}

- (void)onEndOfSpeech {
    [_channel invokeMethod:@"onEndOfSpeech" arguments:NULL];
}

- (void)onCancel {
    [_channel invokeMethod:@"onCancel" arguments:NULL];
}


#pragma mark - IFlySpeechSynthesizerDelegate
//- (void) onCompleted:(IFlySpeechError*) error{
//
//}

- (void) onSpeakBegin{
    [_channel invokeMethod:@"onSpeakBegin" arguments:NULL];
}

- (void) onBufferProgress:(int) progress message:(NSString *)msg{
     [_channel invokeMethod:@"onBufferProgress" arguments:@[@(progress), @(0), @(0),msg]];
}

- (void) onSpeakProgress:(int) progress beginPos:(int)beginPos endPos:(int)endPos{
     [_channel invokeMethod:@"onSpeakProgress" arguments:@[@(progress), @(beginPos), @(endPos)]];
}

- (void) onSpeakPaused{
     [_channel invokeMethod:@"onSpeakPaused" arguments:NULL];
}

- (void) onSpeakResumed{
     [_channel invokeMethod:@"onSpeakResumed" arguments:NULL];
}

- (void) onSpeakCancel{
     [_channel invokeMethod:@"onSpeakCancel" arguments:NULL];
}

- (void) onEvent:(int)eventType arg0:(int)arg0 arg1:(int)arg1 data:(NSData *)eventData{
    
}

+ (void)testParam {
    NSMutableArray *paramArr = [NSMutableArray arrayWithCapacity:100];
    [paramArr addObject:[IFlySpeechConstant SPEECH_TIMEOUT]];
    [paramArr addObject:[IFlySpeechConstant IFLY_DOMAIN]];
    [paramArr addObject:[IFlySpeechConstant NET_TIMEOUT]];
    [paramArr addObject:[IFlySpeechConstant POWER_CYCLE]];
    [paramArr addObject:[IFlySpeechConstant SAMPLE_RATE]];
    [paramArr addObject:[IFlySpeechConstant ENGINE_TYPE]];
    [paramArr addObject:[IFlySpeechConstant TYPE_LOCAL]];
    [paramArr addObject:[IFlySpeechConstant TYPE_CLOUD]];
    [paramArr addObject:[IFlySpeechConstant TYPE_MIX]];
    [paramArr addObject:[IFlySpeechConstant TYPE_AUTO]];
    [paramArr addObject:[IFlySpeechConstant TEXT_ENCODING]];
    [paramArr addObject:[IFlySpeechConstant RESULT_ENCODING]];
    [paramArr addObject:[IFlySpeechConstant PLAYER_INIT]];
    [paramArr addObject:[IFlySpeechConstant PLAYER_DEACTIVE]];
    [paramArr addObject:[IFlySpeechConstant RECORDER_INIT]];
    [paramArr addObject:[IFlySpeechConstant RECORDER_DEACTIVE]];
    [paramArr addObject:[IFlySpeechConstant SPEED]];
    [paramArr addObject:[IFlySpeechConstant PITCH]];
    [paramArr addObject:[IFlySpeechConstant TTS_AUDIO_PATH]];
    [paramArr addObject:[IFlySpeechConstant VAD_ENABLE]];
    [paramArr addObject:[IFlySpeechConstant VAD_BOS]];
    [paramArr addObject:[IFlySpeechConstant VAD_EOS]];
    [paramArr addObject:[IFlySpeechConstant VOICE_NAME]];
    [paramArr addObject:[IFlySpeechConstant VOICE_ID]];
    [paramArr addObject:[IFlySpeechConstant VOICE_LANG]];
    [paramArr addObject:[IFlySpeechConstant VOLUME]];
    [paramArr addObject:[IFlySpeechConstant TTS_BUFFER_TIME]];
    [paramArr addObject:[IFlySpeechConstant TTS_DATA_NOTIFY]];
    [paramArr addObject:[IFlySpeechConstant NEXT_TEXT]];
    [paramArr addObject:[IFlySpeechConstant MPPLAYINGINFOCENTER]];
    [paramArr addObject:[IFlySpeechConstant AUDIO_SOURCE]];
    [paramArr addObject:[IFlySpeechConstant ASR_AUDIO_PATH]];
    [paramArr addObject:[IFlySpeechConstant ASR_SCH]];
    [paramArr addObject:[IFlySpeechConstant ASR_PTT]];
    [paramArr addObject:[IFlySpeechConstant LOCAL_GRAMMAR]];
    [paramArr addObject:[IFlySpeechConstant CLOUD_GRAMMAR]];
    [paramArr addObject:[IFlySpeechConstant GRAMMAR_TYPE]];
    [paramArr addObject:[IFlySpeechConstant GRAMMAR_CONTENT]];
    [paramArr addObject:[IFlySpeechConstant LEXICON_CONTENT]];
    [paramArr addObject:[IFlySpeechConstant LEXICON_NAME]];
    [paramArr addObject:[IFlySpeechConstant GRAMMAR_LIST]];
    [paramArr addObject:[IFlySpeechConstant NLP_VERSION]];

    NSMutableString *defineString = [NSMutableString stringWithString:@"\n"];
    NSMutableString *toJson = [NSMutableString stringWithString:@""];
    [paramArr enumerateObjectsUsingBlock:^(NSString*  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [defineString appendFormat:@"String %@;\n", obj];
        [toJson appendFormat:@"'%@': %@,\n", obj, obj];
    }];
    NSLog(@"********");
    NSLog(defineString);
    NSLog(toJson);

}

@end

