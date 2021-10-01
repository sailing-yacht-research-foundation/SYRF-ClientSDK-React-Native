#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(SyrfClient, RCTEventEmitter<RCTBridgeModule>)

// TODO: remove
RCT_EXTERN_METHOD(multiply:(float)a withB:(float)b
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

#pragma mark - Permissions

RCT_EXTERN_METHOD(requestAuthorizationPermissions:(NSDictionary *)permissions
                 success:(RCTPromiseResolveBlock)success
                 failure:(RCTPromiseRejectBlock)failure)

RCT_EXTERN_METHOD(checkAuthorizationPermissions:(RCTPromiseResolveBlock)success
                 failure:(RCTPromiseRejectBlock)failure)

RCT_EXTERN_METHOD(requestAccuracyPermissions:(NSDictionary *)permissions
                 success:(RCTPromiseResolveBlock)success
                 failute:(RCTPromiseRejectBlock)failure)

RCT_EXTERN_METHOD(checkAccuracyPermissions:(RCTPromiseResolveBlock)success
                 failute:(RCTPromiseRejectBlock)failure)


#pragma mark - Configuration

RCT_EXTERN_METHOD(configure:(NSDictionary *)options
                  success:(RCTPromiseResolveBlock)success
                  failure:(RCTPromiseRejectBlock)failure)

RCT_EXTERN_METHOD(configureHeading:(NSDictionary *)options
                  success:(RCTPromiseResolveBlock)success
                  failure:(RCTPromiseRejectBlock)failure)

#pragma mark - Monitoring

RCT_EXTERN_METHOD(startLocationUpdates)

RCT_EXTERN_METHOD(startHeadingUpdates)

RCT_EXTERN_METHOD(getCurrentLocation)

RCT_EXTERN_METHOD(stopLocationUpdates)

RCT_EXTERN_METHOD(stopHeadingUpdates)

@end
