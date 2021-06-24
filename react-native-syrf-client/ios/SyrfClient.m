#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(SyrfClient, NSObject)

// TODO: remove
RCT_EXTERN_METHOD(multiply:(float)a withB:(float)b
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

#pragma mark - Permissions

RCT_EXTERN_METHOD(requestPermissions:(NSDictionary *)permissions
                 success:(RCTPromiseResolveBlock)success
                 failure:(RCTPromiseRejectBlock)failure)

RCT_EXTERN_METHOD(checkPermissions:(NSDictionary *)permissions
                 success:(RCTPromiseResolveBlock)success
                 failure:(RCTPromiseRejectBlock)failure)

RCT_EXTERN_METHOD(requestAccuracyPermissions:(NSDictionary *)permissions
                 success:(RCTPromiseResolveBlock)success
                 failute:(RCTPromiseRejectBlock)failure)

RCT_EXTERN_METHOD(checkAccuracyPermissions:(NSDictionary *)permissions
                 success:(RCTPromiseResolveBlock)success
                 failute:(RCTPromiseRejectBlock)failure)


#pragma mark - Configuration

RCT_EXTERN_METHOD(configure:(NSDictionary *)options
                  success:(RCTPromiseResolveBlock)success
                  failure:(RCTPromiseRejectBlock)failure)

#pragma mark - Listeners

RCT_EXTERN_METHOD(addEventListener:(NSString *)event)

RCT_EXTERN_METHOD(removeEventListener:(NSString *)event)

RCT_EXTERN_METHOD(removeAllListeners:(RCTResponseSenderBlock)success
                  failure:(RCTResponseSenderBlock)failure)


#pragma mark - Monitoring

RCT_EXTERN_METHOD(startLocationUpdates)

RCT_EXTERN_METHOD(startHeadingUpdates)

RCT_EXTERN_METHOD(stopLocationUpdates)

RCT_EXTERN_METHOD(stopHeadingUpdates)

@end
