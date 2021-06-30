import React, { useState, useEffect } from 'react';
import { StyleSheet, View, Text, Platform } from 'react-native';
import SyrfClient, {
  SYRFLocationConfigAndroid,
  LocationAccuracyPriority,
  UPDATE_LOCATION_EVENT,
  useEventListener,
  SYRFLocation,
  SYRFPermissionRequestConfig,
} from 'react-native-syrf-client';
import SimpleButton from './SimpleButton';
import { timeFormat } from './Utils';

export default function App() {
  const [result, setResult] = useState('');

  useEventListener(UPDATE_LOCATION_EVENT, (location: SYRFLocation) => {
    const format = 'dd/MM/yyyy, hh:mm:ss';
    const time = timeFormat(location.time, format);
    console.log(location);
    setResult(
      (prev) => `${prev}\n${time} - (${location.lat}, ${location.lon})`
    );
  });

  useEffect(() => {
    if (Platform.OS === 'android') {
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
    } else {
      SyrfClient.configure();
    }

    return () => {
      SyrfClient.stopLocationUpdates();
    };
  }, []);

  const startUpdate = () => {
    SyrfClient.startLocationUpdates();
  };

  const stopUpdate = () => {
    SyrfClient.stopLocationUpdates();
  };

  return (
    <View style={styles.container}>
      <View style={styles.textResultContainer}>
        <Text style={styles.textResult}>{result}</Text>
      </View>
      <SimpleButton
        style={styles.button}
        text="Start Location Update"
        textStyle={styles.buttonText}
        onPress={startUpdate}
      />
      <SimpleButton
        style={styles.button}
        text="Stop Location Update"
        textStyle={styles.buttonText}
        onPress={stopUpdate}
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
