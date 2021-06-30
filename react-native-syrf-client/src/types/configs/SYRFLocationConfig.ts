export enum LocationAccuracyPriority {
  HighAccuracy = 100,
  BalancedPowerAccuracy = 102,
  LowPower = 104,
  NoPower = 105,
}

/**
 * The class help you config params for permission's reason and request dialog
 * @property title The title of permission request dialog
 * @property message The message of permission request dialog
 * @property okButton The title of positive button in permission request dialog
 * @property cancelButton The title of negative button in  permission request dialog
 */
export interface SYRFPermissionRequestConfig {
  title?: string;
  message?: string;
  okButton?: string;
  cancelButton?: string;
}

/**
 * The class help you config params of location request
 * */
export interface SYRFLocationConfig {}

/**
 * The class help you config params of location request for Android
 * @property updateInterval The interval for active location updates, in milliseconds
 * @property maximumLocationAccuracy The priority of the request
 * @property permissionRequestConfig The config for request permission dialog
 */
export interface SYRFLocationConfigAndroid extends SYRFLocationConfig {
  updateInterval: number;
  maximumLocationAccuracy?: LocationAccuracyPriority;
  permissionRequestConfig?: SYRFPermissionRequestConfig;
}

/**
 * The class help you config params of location request for IOS
 */
export interface SYRFLocationConfigIOS extends SYRFLocationConfig {}
