import { NativeModules } from 'react-native';
import {
  SYRFLocationConfig,
  SYRFLocationAuthorizationRequestIOS,
  SYRFLocationAccuracyRequestIOS,
  LocationAccuracyStatusIOS,
  LocationAuthorizationStatusIOS,
  SYRFHeadingConfig,
} from './types';

const { SyrfClient } = NativeModules;

type SyrfClientType = {
  configure(options?: SYRFLocationConfig): Promise<any>;
  startLocationUpdates(): void;
  stopLocationUpdates(): void;
  getCurrentLocation(): void;
  getBatteryLevel(): Promise<number>;
  getPhoneModel(): Promise<string>;
  getOsVersion(): Promise<string>;

  // Android only
  onAppMoveToBackground(): void;

  // iOS only
  enableBatteryMonitoring(): void;
  disableBatteryMonitoring(): void;

  requestAuthorizationPermissions(
    permissions: SYRFLocationAuthorizationRequestIOS
  ): Promise<LocationAuthorizationStatusIOS>;
  checkAuthorizationPermissions(): Promise<LocationAuthorizationStatusIOS>;
  requestAccuracyPermissions(
    permissions: SYRFLocationAccuracyRequestIOS
  ): Promise<LocationAccuracyStatusIOS>;
  checkAccuracyPermissions(): Promise<LocationAccuracyStatusIOS>;

  configureHeading(options?: SYRFHeadingConfig): Promise<any>;
  startHeadingUpdates(): void;
  stopHeadingUpdates(): void;
};

export const {
  UPDATE_LOCATION_EVENT,
  FAILED_LOCATION_EVENT,
  FAILED_HEADING_EVENT,
} = SyrfClient.getConstants();

export default SyrfClient as SyrfClientType;

export { useEventListener } from './hooks/useEventListener';

export * from './types';
