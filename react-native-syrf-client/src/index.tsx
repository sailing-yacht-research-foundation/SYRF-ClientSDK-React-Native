import { NativeModules } from 'react-native';
import type { SYRFLocationConfig } from './types';

const { SyrfClient } = NativeModules;

type SyrfClientType = {
  multiply(a: number, b: number): Promise<number>;
  requestPermissions(permissions: any): Promise<any>;
  checkPermissions(permissions: any): Promise<any>;
  requestAccuracyPermissions(permissions: any): Promise<any>;
  checkAccuracyPermissions(permissions: any): Promise<any>;
  configure(options?: SYRFLocationConfig): Promise<any>;
  addEventListener(event: string): void;
  removeEventListener(event: string): void;
  removeAllListeners(permissions: any): Promise<any>;
  startLocationUpdates(): void;
  stopLocationUpdates(): void;
  startHeadingUpdates(): void;
  stopHeadingUpdates(): void;
};

export const { UPDATE_LOCATION_EVENT } = SyrfClient.getConstants();

export default SyrfClient as SyrfClientType;

export { useEventListener } from './hooks/useEventListener';

export * from './types';
