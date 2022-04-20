/**
 * Represents a location data.
 */
export interface SYRFLocation {
  latitude: number;
  longitude: number;
  instrumentHorizontalAccuracyMeters: number;
  instrumentVerticalAccuracyMeters: number;
  instrumentCOGTrue: number;
  instrumentCOGTrueAccuracyDegrees: number;
  instrumentSOGMetersPerSecond: number;
  instrumentSOGAccuracyMetersPerSecond: number;
  timestamp: number;
  batteryLevel: number;
  provider: string | undefined;
}
