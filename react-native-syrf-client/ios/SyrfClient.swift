

@objc(SyrfClient)
class SyrfClient: NSObject {
    
    // MARK: - Stored Properties
    
    // MARK: - Lifecycle Methods
    
    override init() {
        super.init()
        
    }

    // MARK: - Exported Methods
    
    @objc(multiply:withB:withResolver:withRejecter:)
    func multiply(a: Float, b: Float, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) {
        resolve(a*b)
    }
    
    @objc(requestPermissions:success:failure:)
    func requestPermission(permissions: [String: Any], success: RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        // TODO: handling
        success("request permissions")
    }
    
    @objc(checkPermissions:success:failure:)
    func checkPermissions(permissions: [String: Any], success: RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        // TODO: handling
        success("check permissions")
    }
    
    @objc(requestAccuracyPermissions:success:failure:)
    func requestAccuracyPermissions(permissions: [String: Any], success: RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        // TODO: handling
        success("request accuracy")
    }
    
    @objc(checkAccuracyPermissions:success:failure:)
    func checkAccuracyPermissions(permissions: [String: Any], success: RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        // TODO: handling
        success("check accuracy")
    }
    
    @objc(configure:success:failure:)
    func configure(configuration: [String: Any], success: RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        
    }
    
    @objc(addEventListener:)
    func addEventListener(event: String) {
        
    }
    
    @objc(removeListener:)
    func removeEventListener(event: String) {
        
    }
    
    @objc(removeAllListeners:failure:)
    func removeAllListeners(success: RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        
    }
    
    @objc(startLocationUpdates)
    func startLocationUpdates() {
        //TODO: handling
        print("start location updates")
    }
    
    @objc(startHeadingUpdates)
    func startHeadingUpdates() {
        
    }
    
    @objc(stopLocationUpdates)
    func stopLocationUpdates() {
        //TODO: handling
        print("stop location updates")
    }
    
    @objc(stopHeadingUpdates)
    func stopHeadingUpdates() {
        
    }
    
}


