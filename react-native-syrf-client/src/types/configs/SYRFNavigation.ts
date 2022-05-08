/**
 * Represents a navigation data.
 */

import { SYRFDeviceInfo } from "./SYRFDeviceInfo";
import { SYRFHeading } from "./SYRFHeading";
import { SYRFLocation } from "./SYRFLocation";

export interface SYRFNavigation {
  location?: SYRFLocation;
  heading?: SYRFHeading;
  deviceInfo?: SYRFDeviceInfo;
}
