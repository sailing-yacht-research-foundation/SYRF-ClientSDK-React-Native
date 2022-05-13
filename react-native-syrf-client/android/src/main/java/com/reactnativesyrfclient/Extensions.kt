package com.reactnativesyrfclient

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.syrf.device_info.data.SYRFDeviceInfoData
import com.syrf.location.data.SYRFLocationData
import com.syrf.location.data.SYRFRotationData

fun SYRFLocationData.toMap(): WritableMap = Arguments.createMap().apply {
  putDouble(SyrfClientModule.LOCATION_LAT, latitude)
  putDouble(SyrfClientModule.LOCATION_LON, longitude)
  putDouble(SyrfClientModule.LOCATION_HORZ_ACCURACY, horizontalAccuracy.toDouble())
  putDouble(SyrfClientModule.LOCATION_VERT_ACCURACY, verticalAccuracy.toDouble())
  putDouble(SyrfClientModule.LOCATION_BEARING, trueHeading.toDouble())
  putDouble(SyrfClientModule.LOCATION_BEARING_ACCURACY, bearingAccuracy.toDouble())
  putDouble(SyrfClientModule.LOCATION_SPEED, speed.toDouble())
  putDouble(SyrfClientModule.LOCATION_SPEED_ACCURACY, speedAccuracy.toDouble())
  putDouble(SyrfClientModule.LOCATION_TIME, timestamp.toDouble())
  putString(SyrfClientModule.LOCATION_DESCRIPTION, provider)
}

fun SYRFRotationData.toMap(): WritableMap {
  val params = Arguments.createMap()
  val rawParams = Arguments.createMap()

  var h = azimuth.toDouble()
  if (h < 0.0) {
    h += 2 * Math.PI
  }
  h = h * 180 / Math.PI

  rawParams.putDouble(SyrfClientModule.HEADING_X, h)
  rawParams.putDouble(SyrfClientModule.HEADING_Y, pitch.toDouble())
  rawParams.putDouble(SyrfClientModule.HEADING_Z, roll.toDouble())
  params.putMap(SyrfClientModule.RAW_DATA, rawParams)
  params.putDouble(SyrfClientModule.HEADING_TIME, timestamp.toDouble())
  return params
}

fun SYRFDeviceInfoData.toMap(): WritableMap = Arguments.createMap().apply {
  putDouble(SyrfClientModule.BATTERY_LEVEL, batteryInfo)
  putString(SyrfClientModule.OS_VERSION, osVersion)
  putString(SyrfClientModule.DEVICE_MODEL, deviceModel)
}
