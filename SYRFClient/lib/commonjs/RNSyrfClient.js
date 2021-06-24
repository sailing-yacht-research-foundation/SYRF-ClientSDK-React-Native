"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.stopLocationUpdates = exports.startLocationUpdates = void 0;

var _reactNative = require("react-native");

const {
  RNSyrfClient
} = _reactNative.NativeModules;

const startLocationUpdates = () => {
  if (RNSyrfClient == undefined) {
    console.log('undefined');
    return;
  }

  console.log('hello');
  RNSyrfClient.startLocationUpdates();
};

exports.startLocationUpdates = startLocationUpdates;

const stopLocationUpdates = () => {
  if (RNSyrfClient == undefined) {
    console.log('undefined');
    return;
  }

  RNSyrfClient.stopLocationUpdates();
};

exports.stopLocationUpdates = stopLocationUpdates;
//# sourceMappingURL=RNSyrfClient.js.map