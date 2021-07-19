import SYRFLocation
import CoreLocation
import React

let UPDATE_LOCATION_EVENT   = "UPDATE_LOCATION_EVENT"
let FAILED_LOCATION_EVENT    = "FAILED_LOCATION_EVENT"

@objc(SyrfClient)
class SyrfClient: RCTEventEmitter {
    
    // MARK: - Stored Properties
    
    private var locationManager: LocationManager!
    private var permissionsManager: PermissionsManager!
    
    private var callbackAuthorization: RCTPromiseResolveBlock?
    private var callbackAccuracy: RCTPromiseResolveBlock?
    
    private var hasListeners: Bool = false
    
    // MARK: - Lifecycle Methods
    
    override init() {
        self.locationManager = LocationManager()
        self.permissionsManager = PermissionsManager()
        
        super.init()
        
        self.locationManager.delegate = self
        self.permissionsManager.delegate = self
    }
    
    @objc
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    override func constantsToExport() -> [AnyHashable : Any]! {
        return [
            UPDATE_LOCATION_EVENT: UPDATE_LOCATION_EVENT,
            FAILED_LOCATION_EVENT: FAILED_LOCATION_EVENT
        ];
    }
    
    // MARK: - Event Emitter Methods
    
    override func startObserving() {
        hasListeners = true
    }
    
    override func stopObserving() {
        hasListeners = false
    }
    
    @objc
    func sendEvent(eventName: String, data: [String: Any]) {
        if hasListeners {
            self.sendEvent(withName: eventName, body: data);
        }
    }
    
    @objc
    override func supportedEvents() -> [String]! {
        return [
            UPDATE_LOCATION_EVENT,
            FAILED_LOCATION_EVENT
        ];
    }

    // MARK: - Exported Methods
    
    @objc(multiply:withB:withResolver:withRejecter:)
    func multiply(a: Float, b: Float, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) {
        resolve(a*b)
    }
    
    @objc(requestAuthorizationPermissions:success:failure:)
    func requestAuthorizationPermission(dictionary: [String: Any], success: @escaping RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        if let permissions = dictionary["permissions"] as? String {
            let authorizationLevel: PermissionsType = permissions.lowercased() == "always" ? .always : .whenInUse
            self.permissionsManager.requestAuthorization(authorizationLevel)
            self.callbackAuthorization = success
        } else {
            failure("0", "Invalid parameters", nil)
        }
        
    }
    
    @objc(checkAuthorizationPermissions:failure:)
    func checkAuthorizationPermissions(success: RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        let status = self.permissionsManager.checkAuthorization()
        success(self.getAuthorizationStatus(status))
    }
    
    @objc(requestAccuracyPermissions:success:failure:)
    func requestAccuracyPermissions(dictionary: [String: Any], success: @escaping RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        if let purpose = dictionary["purpose"] as? String {
            self.permissionsManager.requestAccuracy(purpose)
            self.callbackAccuracy = success
        } else {
            failure("0", "Invalid parameters", nil)
        }
    }
    
    @objc(checkAccuracyPermissions:failure:)
    func checkAccuracyPermissions(success: RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        let status = self.permissionsManager.checkAccuracy()
        success(self.getAccuracyStatus(status))
    }
    
    @objc(configure:success:failure:)
    func configure(configuration: [String: Any], success: RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        let options = LocationManagerConfig()
        
        guard let activityType = configuration["activity"] as? String,
              let distanceFilter = configuration["distanceFilter"] as? Double,
              let desiredAccuracy = configuration["desiredAccuracy"] as? String else {
            failure("0", "Invalid parameters", nil)
            return
        }
        
        options.activityType = self.getActivityType(activity: activityType)
        options.distanceFilter = self.getDistanceFilter(distance: distanceFilter)
        options.desiredAccuracy = self.getAccuracyFilter(accuracy: desiredAccuracy)
        if let pauseUpdatesAutomatically = configuration["pauseUpdatesAutomatically"] as? Bool {
            options.pauseUpdatesAutomatically = pauseUpdatesAutomatically
        }
        if let allowIndicatorInBackground = configuration["allowIndicatorInBackground"] as? Bool {
            options.allowIndicatorInBackground = allowIndicatorInBackground
        }
        if let allowUpdatesInBackground = configuration["allowUpdatesInBackground"] as? Bool {
            options.allowUpdatesInBackground = allowUpdatesInBackground
        }
        
        self.locationManager.configure(options)
        success(true)
    }
    
    @objc(startLocationUpdates)
    func startLocationUpdates() {
        self.locationManager.startLocationUpdates()
    }
    
    @objc(startHeadingUpdates)
    func startHeadingUpdates() {
        
    }
    
    @objc(stopLocationUpdates)
    func stopLocationUpdates() {
        self.locationManager.stopLocationUpdates()
    }
    
    @objc(stopHeadingUpdates)
    func stopHeadingUpdates() {
        
    }
    
}

// MARK: - Extension for LocationManager Delegate
extension SyrfClient: LocationDelegate {
    
    func locationFailed(_ error: Error) {
        self.sendEvent(eventName: FAILED_LOCATION_EVENT, data: ["error": error.localizedDescription])
    }
    
    func locationUpdated(_ location: SYRFLocation) {
        print("location updated: \(location)")
        self.sendEvent(eventName: UPDATE_LOCATION_EVENT, data: self.getLocationDictionary(location))
    }
    
    func currentLocationUpdated(_ location: SYRFLocation) {
        print("current location updated: \(location) ")
        self.sendEvent(eventName: UPDATE_LOCATION_EVENT, data: self.getLocationDictionary(location))
    }
}

// MARK: - Extension for PermissionsManager Delegate
extension SyrfClient: PermissionsDelegate {
    
    func authorizationUpdated(_ status: PermissionsAuthorization) {
        if let callback = self.callbackAuthorization {
            callback(self.getAuthorizationStatus(status))
            self.callbackAuthorization = nil
        }
    }
    
    func accuracyUpdated(_ status: PermissionsAccuracy) {
        if let callback = self.callbackAccuracy {
            callback(self.getAccuracyStatus(status))
            self.callbackAccuracy = nil
        }
    }
}

// MARK: - Extension for RN Module
extension SyrfClient {
    
    func getAuthorizationStatus(_ status: PermissionsAuthorization) -> String {
        switch status {
            case .notAvailable:
                return "notAvailable"
            case .notDetermined:
                return "notDetermined"
            case .notAuthorized:
                return "notAuthorized"
            case .authorizedAlways:
                return "authorizedAlways"
            case .authorizedWhenInUse:
                return "authorizedWhenInUse"
        }
    }
    
    func getAccuracyStatus(_ status: PermissionsAccuracy) -> String {
        switch status {
            case .notAvailable:
                return "notAvailable"
            case .full:
                return "full"
            case .reduced:
                return "reduced"
        }
    }
    
    func getActivityType(activity: String) -> CLActivityType {
        if (activity.lowercased() == "airborne") {
            return .airborne
        }
        if (activity.lowercased() == "othernavigation") {
            return .otherNavigation
        }
        if (activity.lowercased() == "automotivenavigation") {
            return .automotiveNavigation
        }
        if (activity.lowercased() == "fitness") {
            return .fitness
        }
        return .other
    }
    
    func getAccuracyFilter(accuracy: String) -> CLLocationAccuracy {
        if (accuracy.lowercased() == "bestfornavigation") {
            return kCLLocationAccuracyBestForNavigation
        }
        if (accuracy.lowercased() == "best") {
            return kCLLocationAccuracyBest
        }
        if (accuracy.lowercased() == "nearesttenmeters") {
            return kCLLocationAccuracyNearestTenMeters
        }
        if (accuracy.lowercased() == "hundredmeters") {
            return kCLLocationAccuracyHundredMeters
        }
        if (accuracy.lowercased() == "threekilometers") {
            return kCLLocationAccuracyThreeKilometers
        }
        return kCLLocationAccuracyKilometer
    }
    
    func getDistanceFilter(distance: Double) -> Double {
        return distance == 0 ? kCLDistanceFilterNone : distance
    }
    
    func getLocationDictionary(_ location: SYRFLocation) -> [String: Any] {
        var dictionary = [String: Any]()
        
        dictionary["latitude"] = location.coordinate.latitude
        dictionary["longitude"] = location.coordinate.longitude
        dictionary["accuracy"] = location.horizontalAccuracy
        dictionary["speed"] = location.speed
        dictionary["heading"] = location.courseHeading
        dictionary["timestamp"] = location.timestamp
        
        return dictionary
    }
}


