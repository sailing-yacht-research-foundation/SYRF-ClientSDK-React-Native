import { NativeModules } from 'react-native';
import type { 
  SYRFLocationConfig, 
  SYRFLocationAuthorizationRequestIOS,
  SYRFLocationAccuracyRequestIOS,
  LocationAccuracyStatusIOS,
  LocationAuthorizationStatusIOS,
} from './types';

const { SyrfClient } = NativeModules;

type SyrfClientType = {
  multiply(a: number, b: number): Promise<number>;
  // iOS only
  requestAuthorizationPermissions(permissions: SYRFLocationAuthorizationRequestIOS): Promise<LocationAuthorizationStatusIOS>;
  checkAuthorizationPermissions(): Promise<LocationAuthorizationStatusIOS>;
  requestAccuracyPermissions(permissions: SYRFLocationAccuracyRequestIOS): Promise<LocationAccuracyStatusIOS>;
  checkAccuracyPermissions(): Promise<LocationAccuracyStatusIOS>;
  // iOS only
  configure(options?: SYRFLocationConfig): Promise<any>;
  startLocationUpdates(): void;
  stopLocationUpdates(): void;
  startHeadingUpdates(): void;
  stopHeadingUpdates(): void;
};

export const { UPDATE_LOCATION_EVENT, FAILED_LOCATION_EVENT } = SyrfClient.getConstants();

export default SyrfClient as SyrfClientType;

export { useEventListener } from './hooks/useEventListener';

export * from './types';
