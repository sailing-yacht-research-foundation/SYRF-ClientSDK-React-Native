
#import "RNSyrfClient.h"
@import SYRFLocation;

static NSString *const EVENT_LOCATION_UPDATED           = @"locationUpdated";
static NSString *const EVENT_LOCATION_FAILED            = @"locationFailed";
static NSString *const EVENT_CURRENT_LOCATION_UPDATED   = @"currentLocationUpdated";
static NSString *const EVENT_CURRENT_LOCATION_FAILED    = @"currentLocationFailed";
static NSString *const EVENT_HEADING_UPDATED            = @"headingUpdated";
static NSString *const EVENT_HEADING_FAILED             = @"headingFailed";
static NSString *const EVENT_PERMISSIONS_CHANGED        = @"permissionsChanged";
static NSString *const EVENT_ACCURACY_CHANGED           = @"permissionsChanged";

@implementation RNSyrfClient

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
    
}

#pragma mark - Lifecycle

- (NSArray<NSString *> *)supportedEvents
{
    return @[
        EVENT_LOCATION_UPDATED,
        EVENT_LOCATION_FAILED,
        EVENT_CURRENT_LOCATION_UPDATED,
        EVENT_CURRENT_LOCATION_FAILED,
        EVENT_HEADING_UPDATED,
        EVENT_HEADING_FAILED,
        EVENT_PERMISSIONS_CHANGED,
        EVENT_ACCURACY_CHANGED
    ];
}

RCT_EXPORT_MODULE()

#pragma mark - RN Exports

#pragma mark - Permissions

RCT_EXPORT_METHOD(requestPermissions:(NSDictionary *)permissions
                 success:(RCTPromiseResolveBlock)success
                 failure:(RCTPromiseRejectBlock)failure)
{
    // TODO: add location manager
    success(@(YES));
}

RCT_EXPORT_METHOD(checkPermissions:(NSDictionary *)permissions
                 success:(RCTPromiseResolveBlock)success
                 failute:(RCTPromiseRejectBlock)failure)
{
    // TODO: add location manager
    success(@(YES));
}

// ios specific
RCT_EXPORT_METHOD(requestAccuracyPermissions:(NSDictionary *)permissions
                 success:(RCTPromiseResolveBlock)success
                 failute:(RCTPromiseRejectBlock)failure)
{
    success(@(YES));
}

#pragma mark - Configuration

RCT_EXPORT_METHOD(configure:(NSDictionary *) options success:(RCTPromiseResolveBlock)success failure:(RCTPromiseRejectBlock)failure)
{
    // ios specific parameters
    // activityType
    // distanceFilter
    // desiredAccuracy
    // pauseUpdatesAutomatically
    // allowUpdatesInBackground
    // allowIndicatorInBackground
    success(@(YES));
}

#pragma mark - Listeners

RCT_EXPORT_METHOD(addEventListener:(NSString *)event)
{
    // TODO: add events
}

RCT_EXPORT_METHOD(removeListener:(NSString *)event)
{
    // TODO: remove events
}

RCT_EXPORT_METHOD(removeAllListeners:(RCTResponseSenderBlock)success failure:(RCTResponseSenderBlock)failure)
{
    // TODO: remove all events
}

#pragma mark - Monitoring

RCT_EXPORT_METHOD(startLocationUpdates)
{
    // TODO: add location manager
}

RCT_EXPORT_METHOD(startHeadingUpdates)
{
    // TODO: add location manager
}

RCT_EXPORT_METHOD(stopLocationUpdates)
{
    // TODO: add location manager
}

RCT_EXPORT_METHOD(stopHeadingUpdates)
{
    // TODO: add location manager
}

@end
  
