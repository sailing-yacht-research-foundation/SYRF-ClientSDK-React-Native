export enum LocationAccuracyAndroid {
  HighAccuracy = 100,
  BalancedPowerAccuracy = 102,
  LowPower = 104,
  NoPower = 105,
}

export enum LocationAccuracyIOS {
  BestForNavigation = 'bestForNavigation',
  Best = 'best',
  NearestTenMeters = 'nearestTenMeters',
  HundredMeters = 'hundredMeters',
  ThreeKilometers = 'threeKilometers',
}

export enum LocationActivityTypeIOS {
  Other = 'other',
  AutomativeNavigation = 'automotiveNavigation',
  Fitness = 'fitness',
  OtherNavigation = 'otherNavigation',
  Airborne = 'airborne',
}

export enum LocationAuthorizationStatusIOS {
  NotAvailable = 'notAvailable',
  NotDetermined = 'notDetermined',
  NotAuthorized = 'notAuthorized',
  AuthorizedAlways = 'authorizedAlways',
  AuthorizedWhenInUse = 'authorizedWhenInUse',
}

export enum LocationAccuracyStatusIOS {
  NotAvailable = 'notAvailable',
  Full = 'full',
  Reduced = 'reduced',
}

/**
 * The class help you config params for permission's reason and request dialog
 * @property title The title of permission request dialog
 * @property message The message of permission request dialog
 * @property okButton The title of positive button in permission request dialog
 * @property cancelButton The title of negative button in  permission request dialog
 */
export interface SYRFPermissionRequestConfigAndroid {
  title?: string;
  message?: string;
  okButton?: string;
  cancelButton?: string;
}

/**
 * The class help you config params for permissions iOS
 * @property permissions The level of permissions requested
 */
export interface SYRFPermissionRequestConfigIOS {
  permissions: 'whenInUse' | 'always';
}

/**
 * The class help you config params of location request
 * */
export interface SYRFLocationConfig {
  enabled?: boolean;
}

/**
 * The class help you config params of location request for Android
 * @property updateInterval The interval for active location updates, in milliseconds
 * @property desiredAccuracy The priority of the request
 * @property permissionRequestConfig The config for request permission dialog
 */
export interface SYRFLocationConfigAndroid extends SYRFLocationConfig {
  desiredAccuracy?: LocationAccuracyAndroid;
  permissionRequestConfig?: SYRFPermissionRequestConfigAndroid;
  provider?: 'gps' | 'network';
  updateInterval?: number;
}

/**
 * The class helps you config params of location request for IOS
 * @property activity The activity type for the location
 * @property distanceFilter The minimum change in distance on which location updates are notified
 * @property desiredAccuracy The accuracy of the location updates
 * @property pauseUpdatesAutomatically The flag for battery optimization location updates
 * @property allowIndicatorInBackground The flag for showing the visual indicator of location usage
 * @property allowUpdatesInBackground The flag for getting location updates while the application is in the background
 */
export interface SYRFLocationConfigIOS extends SYRFLocationConfig {
  desiredAccuracy: LocationAccuracyIOS;
  permissionRequestConfig?: SYRFPermissionRequestConfigIOS;

  activity: LocationActivityTypeIOS;
  distanceFilter: number;
  pauseUpdatesAutomatically?: boolean | void;
  allowIndicatorInBackground?: boolean | void;
  allowUpdatesInBackground?: boolean | void;
}

/**
 * The class helps you to set up authorization permissions requests on IOS
 * @property permissions The level of permissions requested
 */
export interface SYRFLocationAuthorizationRequestIOS {
  permissions: 'whenInUse' | 'always';
}

/**
 * The class helps you to set up full accuracty permissions requests on IOS
 * @property purpose The description of the reason for full accuracy permissions
 */
export interface SYRFLocationAccuracyRequestIOS {
  purpose: string;
}
