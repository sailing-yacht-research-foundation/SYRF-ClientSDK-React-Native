import {NativeModules} from 'react-native';

const {RNSyrfClient} = NativeModules;

export const startLocationUpdates = () => {
  if (RNSyrfClient == undefined) {
    console.log('undefined');
    return;
  }

  console.log('hello');
  RNSyrfClient.startLocationUpdates();
};

export const stopLocationUpdates = () => {
  if (RNSyrfClient == undefined) {
    console.log('undefined');
    return;
  }

  RNSyrfClient.stopLocationUpdates();
};
