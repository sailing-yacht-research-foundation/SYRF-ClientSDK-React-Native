import React, { useState, useEffect, useCallback } from 'react';
import { StyleSheet, View, Text, Platform } from 'react-native';
import SyrfClient, {
  SYRFLocationConfigAndroid,
  LocationAccuracyPriority,
  UPDATE_LOCATION_EVENT,
  FAILED_LOCATION_EVENT,
  useEventListener,
  SYRFLocation,
  SYRFPermissionRequestConfig,
  SYRFLocationConfigIOS,
  SYRFLocationAuthorizationRequestIOS,
  LocationActivityTypeIOS,
  LocationAccuracyIOS,
} from 'react-native-syrf-client';
import SimpleButton from './SimpleButton';
import { hasPermissionIOS, timeFormat } from './Utils';

export default function App() {
  const [result, setResult] = useState('');
  const [updating, setUpdating] = useState(false);

  useEventListener(UPDATE_LOCATION_EVENT, (location: SYRFLocation) => {
    const format = 'dd/MM/yyyy, hh:mm:ss';
    const time = timeFormat(location.timestamp, format);
    console.log(location);
    setResult((prev) => {
      return `${prev}\n${time} - (${location.latitude}, ${location.longitude}, ${location.accuracy}, ${location.speed}, ${location.heading})`;
    });
  });

  useEventListener(FAILED_LOCATION_EVENT, (error: string) => {
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

  const configureAndroid = () => {
    const permissionRequestConfig: SYRFPermissionRequestConfig = {
      title: 'Permission required',
      message: 'Please grant location permission for continue...',
      okButton: 'OK',
      cancelButton: 'Cancel',
    };

    const config: SYRFLocationConfigAndroid = {
      updateInterval: 2,
      maximumLocationAccuracy: LocationAccuracyPriority.HighAccuracy,
      permissionRequestConfig: permissionRequestConfig,
    };
    SyrfClient.configure(config);
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

  useEffect(() => {
    configureSyrfLocation();

    return () => {
      SyrfClient.stopLocationUpdates();
    };
  }, [configureSyrfLocation]);

  const toggleUpdate = () => {
    if (updating) {
      SyrfClient.stopLocationUpdates();
    } else {
      SyrfClient.startLocationUpdates();
    }

    setUpdating((prev) => !prev);
  };

  return (
    <View style={styles.container}>
      <View style={styles.textResultContainer}>
        <Text style={styles.textResult}>{result}</Text>
      </View>
      <SimpleButton
        style={styles.button}
        text={updating ? 'Stop Location Update' : 'Start Location Update'}
        textStyle={styles.buttonText}
        onPress={toggleUpdate}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  textResultContainer: {
    flex: 1,
    width: '90%',
    borderRadius: 2,
    borderColor: 'gray',
    borderWidth: 1,
    padding: 8,
    margin: 10,
  },
  textResult: {
    flex: 1,
    borderRadius: 2,
    fontSize: 14,
  },
  button: {
    height: 40,
    marginBottom: 12,
    backgroundColor: 'blue',
  },
  buttonText: {
    color: 'white',
  },
});
