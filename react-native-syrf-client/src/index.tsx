import { NativeModules } from 'react-native';

type SyrfClientType = {
  multiply(a: number, b: number): Promise<number>;
  requestPermissions(permissions: any): Promise<any>;
  checkPermissions(permissions: any): Promise<any>;
  requestAccuracyPermissions(permissions: any): Promise<any>;
  checkAccuracyPermissions(permissions: any): Promise<any>;
  configure(options: any): Promise<any>;
  addEventListener(event: string): void;
  removeEventListener(event: string): void;
  removeAllListeners(permissions: any): Promise<any>;
  startLocationUpdates(): void;
  stopLocationUpdates(): void;
  startHeadingUpdates(): void;
  stopHeadingUpdates(): void;
};

const { SyrfClient } = NativeModules;

export default SyrfClient as SyrfClientType;
