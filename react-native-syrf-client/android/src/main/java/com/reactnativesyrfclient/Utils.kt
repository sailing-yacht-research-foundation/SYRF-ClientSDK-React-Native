package com.reactnativesyrfclient

import androidx.annotation.Nullable
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

fun getStringOrNull(map: ReadableMap?, key: String): String? {
  return if (map?.hasKey(key) == true) map.getString(key) else null
}

fun getStringOrDefault(map: ReadableMap, key: String, default: String? = ""): String? {
  return if (map.hasKey(key)) map.getString(key) else default
}

fun getIntOrNull(map: ReadableMap?, key: String): Int? {
  return if (map?.hasKey(key) == true) map.getInt(key) else null
}

fun getIntOrDefault(map: ReadableMap?, key: String, defaultValue: Int): Int {
  return if (map?.hasKey(key) == true) map.getInt(key) else defaultValue
}

fun getLongOrNull(map: ReadableMap?, key: String): Long? {
  return if (map?.hasKey(key) == true) map.getInt(key).toLong() else null
}

public fun getBooleanOrNull(map: ReadableMap?, key: String): Boolean? {
  return if (map?.hasKey(key) == true) map.getBoolean(key) else null
}

fun getMapOrNull(map: ReadableMap?, key: String): ReadableMap? {
  return if (map?.hasKey(key) == true) map.getMap(key) else null
}

fun getBooleanOrFalse(map: ReadableMap?, key: String): Boolean {
  return if (map?.hasKey(key) == true) map.getBoolean(key) else false
}

fun sendEvent(
  reactContext: ReactContext,
  eventName: String,
  @Nullable params: WritableMap
) {
  reactContext
    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
    .emit(eventName, params)
}
