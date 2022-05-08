export enum HeadingOrientationTypeIOS {
  Portrait = 'portrait',
  PortraitUpsideDown = 'portraitUpsideDown',
  LandscapeLeft = 'landscapeLeft',
  landscapeRight = 'landscapeRight',
  faceUp = 'faceUp',
  faceDown = 'faceDown',
  Airborne = 'airborne',
}

/**
 * The class help you config params of heading request
 * */
export interface SYRFHeadingConfig {
  enabled?: boolean;
  headingFilter?: number | void;
}

/**
 * The class helps you config params of location request for Android
 */
export interface SYRFHeadingConfigAndroid extends SYRFHeadingConfig {
}

/**
 * The class helps you config params of location request for IOS
 * @property orientation The orientation
 */
export interface SYRFHeadingConfigIOS extends SYRFHeadingConfig {
  orientation?: HeadingOrientationTypeIOS | void;
}
