/**
 * The class help you config params of navigation request
 * */

import { SYRFDeviceInfoConfig } from './SYRFDeviceInfoConfig';
import { SYRFHeadingConfig } from './SYRFHeadingConfig';
import { SYRFLocationConfig } from './SYRFLocationConfig';

export interface SYRFNavigationConfig {
  location?: SYRFLocationConfig;
  heading?: SYRFHeadingConfig;
  deviceInfo?: SYRFDeviceInfoConfig;
  throttleForegroundDelay?: number;
  throttleBackgroundDelay?: number;
}
