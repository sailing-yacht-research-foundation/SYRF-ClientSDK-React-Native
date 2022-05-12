import React, { useState, useEffect, useCallback } from 'react';
import { StyleSheet, View, Text, Platform, CheckBox } from 'react-native';
import SyrfClient, {
  SYRFLocationConfigAndroid,
  UPDATE_LOCATION_EVENT,
  CURRENT_LOCATION_EVENT,
  FAILED_LOCATION_EVENT,
  UPDATE_HEADING_EVENT,
  FAILED_HEADING_EVENT,
  useEventListener,
  SYRFLocation,
  SYRFHeading,
  SYRFPermissionRequestConfig,
  SYRFLocationConfigIOS,
  SYRFLocationAuthorizationRequestIOS,
  LocationActivityTypeIOS,
  LocationAccuracyIOS,
  SYRFHeadingConfigIOS,
  HeadingOrientationTypeIOS,
  SYRFNavigationConfig,
} from 'react-native-syrf-client';
import SimpleButton from './SimpleButton';
import { hasPermissionIOS, timeFormat } from './Utils';

export default function App() {
  const [result, setResult] = useState('');
  const [updating, setUpdating] = useState(false);
  const [updatingHeading, setUpdatingHeading] = useState(false);

  const [enableLocation, setEnableLocation] = useState(false);
  const [enableHeading, setEnableHeading] = useState(false);
  const [enableDeviceInfo, setEnableDeviceInfo] = useState(false);

  useEventListener(UPDATE_LOCATION_EVENT, (location: SYRFLocation) => {
    const format = 'dd/MM/yyyy, hh:mm:ss';
    const time = timeFormat(location.timestamp, format);
    setResult((prev) => {
      return `${time} - Update location (${location.latitude}, ${location.longitude}, ${location.instrumentHorizontalAccuracyMeters}, ${location.instrumentSOGMetersPerSecond}, ${location.instrumentCOGTrue})\n${prev}`;
    });
  });

  useEventListener(CURRENT_LOCATION_EVENT, (location: SYRFLocation) => {
    const format = 'dd/MM/yyyy, hh:mm:ss';
    const time = timeFormat(location.timestamp, format);
    setResult((prev) => {
      return `${prev}\n${time} - Current Location (${location.latitude}, ${location.longitude}, ${location.instrumentHorizontalAccuracyMeters}, ${location.instrumentSOGAccuracyMetersPerSecond}, ${location.instrumentCOGTrue})`;
    });
  });

  useEventListener(UPDATE_HEADING_EVENT, (heading: SYRFHeading) => {
    const format = 'dd/MM/yyyy, hh:mm:ss';
    const time = timeFormat(heading.timestamp, format);
    setResult((prev) => {
      return `${time} - ${JSON.stringify(heading)})\n${prev}`;
    });
  });

  useEventListener(FAILED_LOCATION_EVENT, (error: string) => {
    console.log(error);
    setResult((prev) => `${prev}\nError - error)`);
  });

  useEventListener(FAILED_HEADING_EVENT, (error: string) => {
    console.log(error);
    setResult((prev) => `${prev}\nError - error)`);
  });

  const configureSyrfLocation = useCallback(async () => {
    if (Platform.OS === 'android') {
      configureAndroid();
    } else {
      configureIOS();
    }
  }, []);

  const configureSyrfHeading = useCallback(async () => {
    if (Platform.OS === 'android') {
      configureHeadingAndroid();
    } else {
      configureHeadingIOS();
    }
  }, []);

  const configureAndroid = () => {
    const permissionRequestConfig: SYRFPermissionRequestConfig = {
      title: 'Permission required',
      message: 'Please grant location permission for continue...',
      okButton: 'OK',
      cancelButton: 'Cancel',
    };
    const androidLocationConfig: SYRFLocationConfigAndroid = {
      enabled: true,
      permissionRequestConfig: permissionRequestConfig,
      provider: 'network',
    };

    const config: SYRFNavigationConfig = {
      location: androidLocationConfig,
      heading: { enabled: true },
      deviceInfo: { enabled: true },
      throttleBackgroundDelay: 2000,
      throttleForegroundDelay: 1000,
    };
    SyrfClient.configureNavigation(config);
  };

  const configureIOS = async () => {
    const config: SYRFLocationConfigIOS = {
      activity: LocationActivityTypeIOS.OtherNavigation,
      distanceFilter: 0,
      desiredAccuracy: LocationAccuracyIOS.BestForNavigation,
      pauseUpdatesAutomatically: false,
      allowIndicatorInBackground: true,
      allowUpdatesInBackground: true,
    };

    // Check for permission status
    const permissionStatus = await SyrfClient.checkAuthorizationPermissions();
    if (hasPermissionIOS(permissionStatus)) {
      SyrfClient.configure(config);
      return;
    }

    // Request permission
    const permissionRequestConfig: SYRFLocationAuthorizationRequestIOS = {
      permissions: 'always',
    };
    const requestPermissionStatus =
      await SyrfClient.requestAuthorizationPermissions(permissionRequestConfig);
    setResult(
      (prev) =>
        `${prev}\nAuthorization location request status: ${requestPermissionStatus}`
    );
    if (hasPermissionIOS(requestPermissionStatus)) {
      SyrfClient.configure(config);
    }
  };

  const configureHeadingAndroid = () => {
    SyrfClient.configureHeading();
  };

  const configureHeadingIOS = async () => {
    const config: SYRFHeadingConfigIOS = {
      distanceFilter: 10,
      orientation: HeadingOrientationTypeIOS.Portrait,
    };

    // Check for permission status
    const permissionStatus = await SyrfClient.checkAuthorizationPermissions();
    if (hasPermissionIOS(permissionStatus)) {
      SyrfClient.configureHeading(config);
      return;
    }

    // Request permission
    const permissionRequestConfig: SYRFLocationAuthorizationRequestIOS = {
      permissions: 'always',
    };
    const requestPermissionStatus =
      await SyrfClient.requestAuthorizationPermissions(permissionRequestConfig);
    setResult(
      (prev) =>
        `${prev}\nAuthorization location request status: ${requestPermissionStatus}`
    );
    if (hasPermissionIOS(requestPermissionStatus)) {
      SyrfClient.configureHeading(config);
    }
  };

  useEffect(() => {
    configureSyrfLocation();
    configureSyrfHeading();

    return () => {
      SyrfClient.stopLocationUpdates();
      SyrfClient.stopHeadingUpdates();
    };
  }, [configureSyrfLocation, configureSyrfHeading]);

  const toggleUpdate = () => {
    if (updating) {
      SyrfClient.stopLocationUpdates();
    } else {
      SyrfClient.startLocationUpdates();
    }

    setUpdating((prev) => !prev);
  };

  const getCurrentLocation = () => {
    SyrfClient.getCurrentLocation();
  };

  const onUpdateNavigationClick = () => {
    SyrfClient.updateNavigationSettings({
      enableLocation: enableLocation,
      enableHeading: enableHeading,
      enableDeviceInfo: enableDeviceInfo,
    });
  };

  const toggleHeading = () => {
    if (updatingHeading) {
      SyrfClient.stopHeadingUpdates();
    } else {
      SyrfClient.startHeadingUpdates();
    }

    setUpdatingHeading((prev) => !prev);
  };

  const printPhoneModelInfo = async () => {
    const phoneModel = await SyrfClient.getPhoneModel();
    const osVersion = await SyrfClient.getOsVersion();
    console.log(`Phone model: ${phoneModel}, OS version: ${osVersion}`);
    setResult((prev) => {
      return `${prev}\nPhone model: ${phoneModel}, OS version: ${osVersion}`;
    });
  };

  const printBatteryLevel = async () => {
    const batteryLevel = await SyrfClient.getBatteryLevel();
    setResult((prev) => {
      return `${prev}\nBattery level: ${batteryLevel}`;
    });
  };

  const enableBatteryTracking = async () => {
    SyrfClient.enableBatteryMonitoring();
  };

  const disableBatteryTracking = async () => {
    SyrfClient.disableBatteryMonitoring();
  };

  return (
    <View style={styles.container}>
      <View style={styles.textResultContainer}>
        <Text style={styles.textResult}>{result}</Text>
      </View>
      <View style={styles.buttonContainer}>
        <View style={styles.leftContainer}>
          <View style={styles.checkboxContainer}>
            <CheckBox
              value={enableLocation}
              onValueChange={setEnableLocation}
              style={styles.checkbox}
            />
            <Text style={styles.label}>Location</Text>
          </View>
          <View style={styles.checkboxContainer}>
            <CheckBox
              value={enableHeading}
              onValueChange={setEnableHeading}
              style={styles.checkbox}
            />
            <Text style={styles.label}>Heading</Text>
          </View>
          <View style={styles.checkboxContainer}>
            <CheckBox
              value={enableDeviceInfo}
              onValueChange={setEnableDeviceInfo}
              style={styles.checkbox}
            />
            <Text style={styles.label}>Device Info</Text>
          </View>
          <SimpleButton
            style={styles.button}
            text={'Update navigation'}
            textStyle={styles.buttonText}
            onPress={onUpdateNavigationClick}
          />
        </View>
        <View style={styles.rightContainer}>
          {
            Platform.OS === 'ios' ? <View style={{
              flexDirection: 'row'
            }}>
              <SimpleButton
                style={styles.button}
                text={'Enable battery'}
                textStyle={styles.buttonText}
                onPress={enableBatteryTracking}
              />
              <SimpleButton
                style={styles.button}
                text={'Disable Battery'}
                textStyle={styles.buttonText}
                onPress={disableBatteryTracking}
              />
            </View> : null}
          <SimpleButton
            style={styles.button}
            text={'Print battery level'}
            textStyle={styles.buttonText}
            onPress={printBatteryLevel}
          />
          <SimpleButton
            style={styles.button}
            text={'Print phone model'}
            textStyle={styles.buttonText}
            onPress={printPhoneModelInfo}
          />
          <SimpleButton
            style={styles.button}
            text={updating ? 'Stop Navigation Update' : 'Start Navigation Update'}
            textStyle={styles.buttonText}
            onPress={toggleUpdate}
          />
          <SimpleButton
            style={styles.button}
            text={updatingHeading ? 'Stop Heading Update' : 'Start Heading Update'}
            textStyle={styles.buttonText}
            onPress={toggleHeading}
          />
          <SimpleButton
            style={styles.button}
            text={'Get Current Location'}
            textStyle={styles.buttonText}
            onPress={getCurrentLocation}
          />
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  buttonContainer: {
    flexDirection: 'row',
    marginHorizontal: 10,
  },
  leftContainer: {
    flex: 1,
    marginRight: 5,
  },
  rightContainer: {
    flex: 1,
    marginLeft: 5,
  },
  textResultContainer: {
    flex: 1,
    borderRadius: 2,
    borderColor: 'gray',
    borderWidth: 1,
    margin: 10,
  },
  textResult: {
    flex: 1,
    fontSize: 12,
  },
  button: {
    height: 40,
    marginBottom: 12,
    backgroundColor: 'blue',
  },
  buttonText: {
    color: 'white',
  },
  checkbox: {
    alignSelf: 'center',
  },
  checkboxContainer: {
    flexDirection: 'row',
    marginBottom: 10,
  },
  label: {
    margin: 8,
  },
});
