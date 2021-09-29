import { NativeModules } from 'react-native';
import {
  SYRFLocationConfig,
  SYRFLocationAuthorizationRequestIOS,
  SYRFLocationAccuracyRequestIOS,
  LocationAccuracyStatusIOS,
  LocationAuthorizationStatusIOS,
} from './types';

const { SyrfClient } = NativeModules;

type SyrfClientType = {
  configure(options?: SYRFLocationConfig): Promise<any>;
  startLocationUpdates(): void;
  stopLocationUpdates(): void;
  getCurrentLocation(): void;

  // iOS only
  requestAuthorizationPermissions(
    permissions: SYRFLocationAuthorizationRequestIOS
  ): Promise<LocationAuthorizationStatusIOS>;
  checkAuthorizationPermissions(): Promise<LocationAuthorizationStatusIOS>;
  requestAccuracyPermissions(
    permissions: SYRFLocationAccuracyRequestIOS
  ): Promise<LocationAccuracyStatusIOS>;
  checkAccuracyPermissions(): Promise<LocationAccuracyStatusIOS>;
  startHeadingUpdates(): void;
  stopHeadingUpdates(): void;
};

export const {
  UPDATE_LOCATION_EVENT,
  CURRENT_LOCATION_EVENT,
  FAILED_LOCATION_EVENT,
} = SyrfClient.getConstants();

export default SyrfClient as SyrfClientType;

export { useEventListener } from './hooks/useEventListener';

export * from './types';
