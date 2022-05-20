import SYRFLocation
import SYRFDeviceInfo
import SYRFNavigation
import CoreLocation
import React

let UPDATE_LOCATION_EVENT       = "UPDATE_LOCATION_EVENT"
let FAILED_LOCATION_EVENT       = "FAILED_LOCATION_EVENT"
let CURRENT_LOCATION_EVENT      = "CURRENT_LOCATION_EVENT"
let UPDATE_HEADING_EVENT        = "UPDATE_HEADING_EVENT"
let FAILED_HEADING_EVENT        = "FAILED_HEADING_EVENT"
let UPDATE_NAVIGATION_EVENT     = "UPDATE_NAVIGATION_EVENT"
let FAILED_NAVIGATION_EVENT     = "FAILED_NAVIGATION_EVENT"

@objc(SyrfClient)
class SyrfClient: RCTEventEmitter {
    
    // MARK: - Stored Properties
    private var navigationManager: NavigationManager!
    private var locationManager: LocationManager!
    private var headingManager: HeadingManager!
    private var permissionsManager: PermissionsManager!
    private var deviceInfoManager: DeviceInfoManager!
    
    private var configureNavigationBlock: (() -> ())? = nil
    private var callbackAuthorization: RCTPromiseResolveBlock?
    private var callbackAccuracy: RCTPromiseResolveBlock?
    
    private var hasListeners: Bool = false
    
    // MARK: - Lifecycle Methods
    
    override init() {
        self.navigationManager = NavigationManager()
        self.locationManager = LocationManager()
        self.headingManager = HeadingManager()
        self.permissionsManager = PermissionsManager()
        self.deviceInfoManager = DeviceInfoManager()
        
        super.init()
        
        self.navigationManager.delegate = self
        self.locationManager.delegate = self
        self.headingManager.delegate = self
        self.permissionsManager.delegate = self
    }
    
    @objc
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    override func constantsToExport() -> [AnyHashable : Any]! {
        return [
            UPDATE_LOCATION_EVENT: UPDATE_LOCATION_EVENT,
            CURRENT_LOCATION_EVENT: CURRENT_LOCATION_EVENT,
            FAILED_LOCATION_EVENT: FAILED_LOCATION_EVENT,
            UPDATE_HEADING_EVENT: UPDATE_HEADING_EVENT,
            FAILED_HEADING_EVENT: FAILED_HEADING_EVENT,
            UPDATE_NAVIGATION_EVENT: UPDATE_NAVIGATION_EVENT,
            FAILED_NAVIGATION_EVENT: FAILED_NAVIGATION_EVENT,
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
            CURRENT_LOCATION_EVENT,
            FAILED_LOCATION_EVENT,
            UPDATE_HEADING_EVENT,
            FAILED_HEADING_EVENT,
            UPDATE_NAVIGATION_EVENT,
            FAILED_NAVIGATION_EVENT,
        ];
    }

    // MARK: - Exported Methods

    @objc(enableBatteryMonitoring)
    func enableBatteryMonitoring() {
        self.deviceInfoManager.startDeviceInfoUpdates()
    }

    @objc(disableBatteryMonitoring)
    func disableBatteryMonitoring() {
        self.deviceInfoManager.stopDeviceInfoUpdates()
    }

    @objc(getBatteryLevel:failure:)
    func getBatteryLevel(success: @escaping RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        success(self.deviceInfoManager.getBatteryLevel())
    }

    @objc(getPhoneModel:failure:)
    func getPhoneModel(success: @escaping RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        success(self.deviceInfoManager.getPhoneModel())
    }

    @objc(getOsVersion:failure:)
    func getOsVersion(success: @escaping RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        success(self.deviceInfoManager.getOsVersion())
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
        let options = getLocationManagerConfig(configuration)
        
        guard let options = options else {
            failure("0", "Invalid parameters", nil)
            return
        }
        
        self.locationManager.configure(options)
        success(true)
    }
    
    @objc(configureHeading:success:failure:)
    func configureHeading(configuration: [String: Any], success: RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        let options = getHeadingManagerConfig(configuration)
        
        guard let options = options else {
            failure("0", "Invalid parameters", nil)
            return
        }
        
        self.headingManager.configure(options)
        success(true)
    }
    
    @objc(configureNavigation:success:failure:)
    func configureNavigation(configuration: [String: Any], success: @escaping RCTPromiseResolveBlock, failure: RCTPromiseRejectBlock) {
        let options = getNavigationManagerConfig(configuration)
        
        guard let options = options else {
            failure("0", "Invalid parameters", nil)
            return
        }
        
        self.configureNavigationBlock = {
            self.configureNavigationBlock = nil
            self.navigationManager.configure(options)
            success(true)
        }
        
        let auth = self.permissionsManager.checkAuthorization()
        
        if let location = options.locationConfig,
            (location.authorizationLevel == .always && auth != .authorizedAlways) ||
            (location.authorizationLevel == .whenInUse && auth != .authorizedWhenInUse) {
            self.permissionsManager.requestAuthorization(location.authorizationLevel)
        } else {
            self.configureNavigationBlock?()
        }
    }
    
    @objc(getCurrentNavigation:success:failure:)
    func getCurrentNavigation(options: [String: Any]?, success: @escaping RCTPromiseResolveBlock, failure: @escaping RCTPromiseRejectBlock) {
        self.navigationManager.getCurrentNavigation(options) { (navigation, error) in
            if let navigation = navigation {
                success(self.getNavigationDictionary(navigation))
            } else {
                failure("0", "Failed to get navigation", nil)
            }
        }
    }
    
    @objc(updateNavigationSettings:success:failure:)
    func updateNavigationSettings(options: [String: Any]?, success: @escaping RCTPromiseResolveBlock, failure: @escaping RCTPromiseRejectBlock) {
        self.navigationManager.updateNavigationSettings(options) { (error) in
            if let _ = error {
                failure("0", "Failed to update navigation", nil)
            } else {
                success(true)
            }
        }
    }
    
    @objc(startLocationUpdates)
    func startLocationUpdates() {
        self.locationManager.startLocationUpdates()
    }
    
    @objc(startHeadingUpdates)
    func startHeadingUpdates() {
        self.headingManager.startHeadingUpdates()
    }
    
    @objc(getCurrentLocation)
    func getCurrentLocation() {
        self.locationManager.getCurrentLocation()
    }
    
    @objc(stopLocationUpdates)
    func stopLocationUpdates() {
        self.locationManager.stopLocationUpdates()
    }
    
    @objc(stopHeadingUpdates)
    func stopHeadingUpdates() {
        self.headingManager.stopHeadingUpdates()
    }
    
    func getLocationManagerConfig(_ configuration: Any?) -> LocationManagerConfig? {
        guard let configuration = configuration as? [String: Any],
              let activityType = configuration["activity"] as? String,
              let distanceFilter = configuration["distanceFilter"] as? Double,
              let desiredAccuracy = configuration["desiredAccuracy"] as? String else {
            return nil
        }
        
        let options = LocationManagerConfig()
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
        if let permissions = configuration["permissions"] as? String {
            options.authorizationLevel = permissions.lowercased() == "always" ? .always : .whenInUse
        }
        if let enabled = configuration["enabled"] as? Bool {
            options.enabled = enabled
        }
        
        return options
    }
    
    func getHeadingManagerConfig(_ configuration: Any?) -> HeadingManagerConfig? {
        guard let configuration = configuration as? [String: Any] else {
            return nil
        }
        
        let options = HeadingManagerConfig()
        if let orientation = configuration["orientation"] as? String {
            options.headingOrientation = self.getHeadingOrientation(orientation: orientation)
        }
        if let headingFilter = configuration["headingFilter"] as? Double {
            options.headingFilter = self.getHeadingDistanceFilter(distance: headingFilter)
        }
        if let enabled = configuration["enabled"] as? Bool {
            options.enabled = enabled
        }
        
        return options
    }
    
    func getDeviceInfoManagerConfig(_ configuration: Any?) -> DeviceInfoManagerConfig? {
        guard let configuration = configuration as? [String: Any] else {
            return nil
        }
        
        let options = DeviceInfoManagerConfig()
        
        if let enabled = configuration["enabled"] as? Bool {
            options.enabled = enabled
        }
        
        return options
    }
    
    func getNavigationManagerConfig(_ configuration: Any?) -> NavigationManagerConfig? {
        guard let configuration = configuration as? [String: Any] else {
            return nil
        }
        
        let options = NavigationManagerConfig()
        
        options.locationConfig = getLocationManagerConfig(configuration["location"])
        options.headingConfig = getHeadingManagerConfig(configuration["heading"])
        options.deviceInfoConfig = getDeviceInfoManagerConfig(configuration["deviceInfo"])
        if let val = configuration["throttleForegroundDelay"] as? Double {
            options.throttleForegroundDelay = val
        }
        if let val = configuration["throttleBackgroundDelay"] as? Double {
            options.throttleBackgroundDelay = val
        }
        
        return options
    }
}

// MARK: - Extension for LocationManager Delegate
extension SyrfClient: LocationDelegate {
    
    func locationFailed(_ error: Error) {
        self.sendEvent(eventName: FAILED_LOCATION_EVENT, data: ["error": error.localizedDescription])
    }
    
    func locationUpdated(_ location: SYRFLocation) {
        self.sendEvent(eventName: UPDATE_LOCATION_EVENT, data: self.getLocationDictionary(location))
    }
    
    func currentLocationUpdated(_ location: SYRFLocation) {
        self.sendEvent(eventName: CURRENT_LOCATION_EVENT, data: self.getLocationDictionary(location))
    }
}

// MARK: - Extension for HeadingManager Delegate
extension SyrfClient: HeadingDelegate {
    
    func headingUpdated(_ heading: SYRFHeading) {
        self.sendEvent(eventName: UPDATE_HEADING_EVENT, data: self.getHeadingDictionary(heading))
    }
    
    func headingFailed(_ error: Error) {
        self.sendEvent(eventName: FAILED_HEADING_EVENT, data: ["error": error.localizedDescription])
    }
}

// MARK: - Extension for PermissionsManager Delegate
extension SyrfClient: PermissionsDelegate {
    
    func authorizationUpdated(_ status: PermissionsAuthorization) {
        if let callback = self.callbackAuthorization {
            callback(self.getAuthorizationStatus(status))
            self.callbackAuthorization = nil
        }
        self.configureNavigationBlock?()
    }
    
    func accuracyUpdated(_ status: PermissionsAccuracy) {
        if let callback = self.callbackAccuracy {
            callback(self.getAccuracyStatus(status))
            self.callbackAccuracy = nil
        }
    }
}

// MARK: - Extension for NavigationManager Delegate
extension SyrfClient: NavigationDelegate {
    
    func navigationUpdated(_ navigation: SYRFNavigation) {
        self.sendEvent(eventName: UPDATE_NAVIGATION_EVENT, data: self.getNavigationDictionary(navigation))
    }
    
    func navigationFailed(_ error: Error) {
        self.sendEvent(eventName: FAILED_NAVIGATION_EVENT, data: ["error": error.localizedDescription])
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
    
    func getHeadingDistanceFilter(distance: Double) -> Double {
        return distance == 0 ? kCLHeadingFilterNone : distance
    }
    
    func getHeadingOrientation(orientation: String) -> CLDeviceOrientation {
        if (orientation.lowercased() == "portrait") {
            return .portrait
        }
        if (orientation.lowercased() == "portraitupsidedown") {
            return .portraitUpsideDown
        }
        if (orientation.lowercased() == "landscapeleft") {
            return .landscapeLeft
        }
        if (orientation.lowercased() == "landscaperight") {
            return .landscapeRight
        }
        if (orientation.lowercased() == "faceup") {
            return .faceUp
        }
        if (orientation.lowercased() == "facedown") {
            return .faceDown
        }
        return .portrait
    }
    
    func getLocationDictionary(_ location: SYRFLocation) -> [String: Any] {
        var dictionary = [String: Any]()
        
        dictionary["latitude"] = location.coordinate.latitude
        dictionary["longitude"] = location.coordinate.longitude
        dictionary["instrumentHorizontalAccuracyMeters"] = location.horizontalAccuracy
        dictionary["instrumentVerticalAccuracyMeters"] = location.verticalAccuracy
        dictionary["instrumentCOGTrue"] = location.courseHeading
        dictionary["instrumentCOGTrueAccuracyDegrees"] = location.courseAccuracy
        dictionary["instrumentSOGMetersPerSecond"] = location.speed
        dictionary["instrumentSOGAccuracyMetersPerSecond"] = location.speedAccuracy
        dictionary["timestamp"] = floor(location.timestamp.timeIntervalSince1970 * 1000)
        
        return dictionary
    }
    
    func getHeadingDictionary(_ heading: SYRFHeading) -> [String: Any] {
        var dictionary = [String: Any]()
        var rawDictionary = [String: Any]()
        
        dictionary["headingMagnetic"] = heading.magneticHeading
        dictionary["headingTrue"] = heading.trueHeading
        dictionary["accuracy"] = heading.accuracy
        rawDictionary["x"] = heading.rawData.x
        rawDictionary["y"] = heading.rawData.y
        rawDictionary["z"] = heading.rawData.z
        dictionary["rawData"] = rawDictionary
        dictionary["timestamp"] = floor(heading.timestamp.timeIntervalSince1970 * 1000)
        
        return dictionary
    }
    
    func getDeviceInfoDictionary(_ info: SYRFDeviceInfo) -> [String: Any] {
        var dictionary = [String: Any]()
        
        dictionary["batteryLevel"] = info.batteryLevel
        dictionary["osVersion"] = info.osVersion
        dictionary["deviceModel"] = info.deviceModel
        dictionary["timestamp"] = floor(info.timestamp.timeIntervalSince1970 * 1000)
        
        return dictionary
    }
    
    func getNavigationDictionary(_ navigation: SYRFNavigation) -> [String: Any] {
        var dictionary = [String: Any]()
        
        if let val = navigation.location {
            dictionary["location"] = getLocationDictionary(val)
        }
        if let val = navigation.heading {
            dictionary["heading"] = getHeadingDictionary(val)
        }
        if let val = navigation.deviceInfo {
            dictionary["deviceInfo"] = getDeviceInfoDictionary(val)
        }
        
        return dictionary
    }
}


