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
export interface SYRFHeadingConfig {}

/**
 * The class helps you config params of location request for IOS
 * @property activity The activity type for the location
 * @property distanceFilter The minimum change in distance on which location updates are notified
 * @property desiredAccuracy The accuracy of the location updates
 * @property pauseUpdatesAutomatically The flag for battery optimization location updates
 * @property allowIndicatorInBackground The flag for showing the visual indicator of location usage
 * @property allowUpdatesInBackground The flag for getting location updates while the application is in the background
 */
export interface SYRFHeadingConfigIOS extends SYRFHeadingConfig {
  orientation?: HeadingOrientationTypeIOS | void;
  distanceFilter?: number | void;
}
