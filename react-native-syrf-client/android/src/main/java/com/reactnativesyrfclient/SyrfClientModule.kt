package com.reactnativesyrfclient

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.SensorManager
import android.view.Display
import android.view.Surface
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import com.syrf.device_info.data.SYRFDeviceInfoConfig
import com.syrf.device_info.interfaces.SYRFDeviceInfo
import com.syrf.location.configs.SYRFLocationConfig
import com.syrf.location.configs.SYRFPermissionRequestConfig
import com.syrf.location.configs.SYRFRotationConfig
import com.syrf.location.data.SYRFLocationData
import com.syrf.location.data.SYRFRotationData
import com.syrf.location.data.SYRFRotationSensorData
import com.syrf.location.interfaces.SYRFLocation
import com.syrf.location.interfaces.SYRFRotationSensor
import com.syrf.location.permissions.PermissionsManager
import com.syrf.location.utils.Constants
import com.syrf.location.utils.Constants.EXTRA_LOCATION
import com.syrf.location.utils.Constants.EXTRA_ROTATION_SENSOR_DATA
import com.syrf.location.utils.MissingLocationException
import com.syrf.navigation.data.SYRFNavigationConfig
import com.syrf.navigation.data.SYRFNavigationData
import com.syrf.navigation.data.SYRFToggler
import com.syrf.navigation.interfaces.SYRFNavigation
import com.syrf.time.configs.SYRFTimeConfig
import com.syrf.time.interfaces.SYRFTime

class SyrfClientModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), PermissionListener {

  companion object {
    const val KEY_UPDATE_INTERVAL = "updateInterval"
    const val KEY_MAX_LOCATION_ACCURACY = "maximumLocationAccuracy"
    const val KEY_PERMISSION_REQUEST_CONFIG = "permissionRequestConfig"
    const val KEY_PERMISSION_REQUEST_TITLE = "title"
    const val KEY_PERMISSION_REQUEST_MESSAGE = "message"
    const val KEY_PERMISSION_REQUEST_OK_BTN = "okButton"
    const val KEY_PERMISSION_REQUEST_CANCEL_BTN = "cancelButton"

    const val KEY_ENABLE_LOCATION = "location"
    const val KEY_ENABLE_HEADING = "heading"
    const val KEY_ENABLE_DEVICE_INFO = "deviceInfo"

    const val UPDATE_LOCATION_EVENT = "UPDATE_LOCATION_EVENT"
    const val CURRENT_LOCATION_EVENT = "CURRENT_LOCATION_EVENT"
    const val UPDATE_HEADING_EVENT = "UPDATE_HEADING_EVENT"
    const val UPDATE_NAVIGATION_EVENT = "UPDATE_NAVIGATION_EVENT"

    const val LOCATION_LAT = "latitude"
    const val LOCATION_LON = "longitude"
    const val LOCATION_HORZ_ACCURACY = "instrumentHorizontalAccuracyMeters"
    const val LOCATION_VERT_ACCURACY = "instrumentVerticalAccuracyMeters"
    const val LOCATION_BEARING = "instrumentCOGTrue"
    const val LOCATION_BEARING_ACCURACY = "instrumentCOGTrueAccuracyDegrees"
    const val LOCATION_SPEED = "instrumentSOGMetersPerSecond"
    const val LOCATION_SPEED_ACCURACY = "instrumentSOGAccuracyMetersPerSecond"
    const val LOCATION_TIME = "timestamp"
    const val LOCATION_DESCRIPTION = "instrumentDescription"

    const val BATTERY_LEVEL = "batteryLevel"
    const val OS_VERSION = "osVersion"
    const val DEVICE_MODEL = "deviceModel"

    const val HEADING_X = "x"
    const val HEADING_Y = "y"
    const val HEADING_Z = "z"
    const val RAW_DATA = "rawData"
    const val HEADING_TIME = "timestamp"

    const val REQUEST_PERMISSION_CODE = 1
  }

  private val locationBroadcastReceiver = LocationBroadcastReceiver()
  private val headingBroadcastReceiver = HeadingBroadcastReceiver()
  private val navigationBroadcastReceiver = NavigationBroadcastReceiver()
  private var permissionRequestConfig: SYRFPermissionRequestConfig? = null

  private var waitingForLocationPermission = false
  private var waitingForCurrentLocationPermission = false

  private var usingNavigation = false

  private val rotationMatrix = FloatArray(9)
  private val orientation = FloatArray(3)

  override fun getName(): String = "SyrfClient"

  override fun getConstants(): Map<String, Any> {
    val constants: MutableMap<String, Any> = HashMap()
    constants[UPDATE_LOCATION_EVENT] = UPDATE_LOCATION_EVENT
    constants[CURRENT_LOCATION_EVENT] = CURRENT_LOCATION_EVENT
    constants[UPDATE_HEADING_EVENT] = UPDATE_HEADING_EVENT
    constants[UPDATE_NAVIGATION_EVENT] = UPDATE_NAVIGATION_EVENT
    return constants
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ): Boolean {
    currentActivity?.let { activity ->
      PermissionsManager(activity).handleResults(
        permissions,
        successCallback = {
          if (waitingForLocationPermission) {
            if (usingNavigation) {
              SYRFNavigation.subscribeToNavigationUpdates(activity) { _, error ->
                if (error != null) {
                  if (error is MissingLocationException) {
                    waitingForLocationPermission = true
                    requestLocationPermission()
                  }
                  return@subscribeToNavigationUpdates
                }
              }
            } else {
              SYRFLocation.subscribeToLocationUpdates(activity)
            }
          }
          waitingForLocationPermission = false
          if (waitingForCurrentLocationPermission) {
            getCurrentLocation()
          }
          waitingForCurrentLocationPermission = false
        },
        exceptionCallback = {
          waitingForLocationPermission = false
        })
      return true
    }
    return false
  }

  private fun getSYRFPermissionRequestConfig(permissionRequestParams: ReadableMap): SYRFPermissionRequestConfig? {
    currentActivity?.let { activity ->
      val permissionConfigBuilder = SYRFPermissionRequestConfig.Builder()

      getStringOrNull(permissionRequestParams, KEY_PERMISSION_REQUEST_TITLE)?.let {
        permissionConfigBuilder.title(it)
      }
      getStringOrNull(permissionRequestParams, KEY_PERMISSION_REQUEST_MESSAGE)?.let {
        permissionConfigBuilder.message(it)
      }
      getStringOrNull(permissionRequestParams, KEY_PERMISSION_REQUEST_OK_BTN)?.let {
        permissionConfigBuilder.okButton(it)
      }
      getStringOrNull(permissionRequestParams, KEY_PERMISSION_REQUEST_CANCEL_BTN)?.let {
        permissionConfigBuilder.cancelButton(it)
      }

      return permissionConfigBuilder.set(activity)
    }
    return null
  }

  @ReactMethod
  fun getPhoneModel(promise: Promise) {
    promise.resolve(SYRFDeviceInfo.getPhoneModel())
  }

  @ReactMethod
  fun getOsVersion(promise: Promise) {
    promise.resolve(SYRFDeviceInfo.getOsVersion())
  }

  @ReactMethod
  fun getBatteryLevel(promise: Promise) {
    promise.resolve(SYRFDeviceInfo.getBatteryLevel(reactContext))
  }

  @ReactMethod
  fun configureNavigation(params: ReadableMap, promise: Promise) {
    currentActivity?.let { activity ->
      usingNavigation = true
      val builder = SYRFLocationConfig.Builder()
      getLongOrNull(params, KEY_UPDATE_INTERVAL)?.let {
        builder.updateInterval(it)
      }
      getIntOrNull(params, KEY_MAX_LOCATION_ACCURACY)?.let {
        builder.maximumLocationAccuracy(it)
      }
      getMapOrNull(params, KEY_PERMISSION_REQUEST_CONFIG)?.let { permissionRequestParams ->
        permissionRequestConfig = getSYRFPermissionRequestConfig(permissionRequestParams)
      }

      SYRFNavigation.configure(
        SYRFNavigationConfig(
          locationConfig = builder.set(),
          headingConfig = SYRFRotationConfig.Builder().set(),
          deviceInfoConfig = SYRFDeviceInfoConfig(true),
        ), activity
      )

      SYRFNavigation.subscribeToNavigationUpdates(activity) { _, error ->
        if (error != null) {
          if (error is MissingLocationException) {
            waitingForLocationPermission = true
            requestLocationPermission()
          }
          return@subscribeToNavigationUpdates
        }
      }
      LocalBroadcastManager.getInstance(activity).registerReceiver(
        navigationBroadcastReceiver,
        IntentFilter(Constants.ACTION_NAVIGATION_BROADCAST)
      )

      promise.resolve(true)
    }
  }

  @ReactMethod
  fun configure(params: ReadableMap, promise: Promise) {
    currentActivity?.let { activity ->
      usingNavigation = false
      val builder = SYRFLocationConfig.Builder()
      getLongOrNull(params, KEY_UPDATE_INTERVAL)?.let {
        builder.updateInterval(it)
      }
      getIntOrNull(params, KEY_MAX_LOCATION_ACCURACY)?.let {
        builder.maximumLocationAccuracy(it)
      }
      getMapOrNull(params, KEY_PERMISSION_REQUEST_CONFIG)?.let { permissionRequestParams ->
        permissionRequestConfig = getSYRFPermissionRequestConfig(permissionRequestParams)
      }

      SYRFTime.configure(SYRFTimeConfig.Builder().set(), activity)
      SYRFLocation.configure(builder.set(), activity)
      SYRFRotationSensor.configure(SYRFRotationConfig.Builder().set(), activity)
      promise.resolve(true)
    }
  }

  @ReactMethod
  fun updateNavigationSettings(params: ReadableMap, promise: Promise) {
    currentActivity?.let { activity ->
      val location = getBooleanOrNull(params, KEY_ENABLE_LOCATION)
      val heading = getBooleanOrNull(params, KEY_ENABLE_HEADING)
      val deviceInfo = getBooleanOrNull(params, KEY_ENABLE_DEVICE_INFO)

      val toggler = SYRFToggler(
        location = location,
        heading = heading,
        deviceInfo = deviceInfo
      )
      SYRFNavigation.updateNavigationSettings(toggler, activity) { _, error ->
        if (error != null) {
          if (error is MissingLocationException) {
            waitingForLocationPermission = true
            requestLocationPermission()
          }
        }
      }
    }
  }

  @ReactMethod
  fun startLocationUpdates() {
    currentActivity?.let { activity ->
      SYRFLocation.subscribeToLocationUpdates(activity) { _, error ->
        if (error != null) {
          if (error is MissingLocationException) {
            waitingForLocationPermission = true
            requestLocationPermission()
          }
          return@subscribeToLocationUpdates
        }
      }
      LocalBroadcastManager.getInstance(activity).registerReceiver(
        locationBroadcastReceiver,
        IntentFilter(Constants.ACTION_LOCATION_BROADCAST)
      )
    }
  }

  @ReactMethod
  fun getCurrentLocation() {
    currentActivity?.let { activity ->
      if (usingNavigation) {
        SYRFNavigation.getCurrentPosition(activity) { location, error ->
          if (error != null) {
            if (error is MissingLocationException) {
              waitingForCurrentLocationPermission = true
              requestLocationPermission()
            }
            return@getCurrentPosition
          }
          location?.let {
            sendEvent(reactApplicationContext, CURRENT_LOCATION_EVENT, it.toMap())
          }
        }
      } else {
        SYRFLocation.getCurrentPosition(activity) { location, error ->
          if (error != null) {
            if (error is MissingLocationException) {
              waitingForCurrentLocationPermission = true
              requestLocationPermission()
            }
            return@getCurrentPosition
          }
          location?.let {
            sendEvent(reactApplicationContext, CURRENT_LOCATION_EVENT, it.toMap())
          }
        }
      }
    }
  }

  @ReactMethod
  fun stopLocationUpdates() {
    if (usingNavigation) {
      currentActivity?.let {
        SYRFNavigation.unsubscribeToNavigationUpdates(it)
        LocalBroadcastManager.getInstance(it).unregisterReceiver(navigationBroadcastReceiver)
      }
    } else {
      SYRFLocation.unsubscribeToLocationUpdates()
      currentActivity?.let {
        LocalBroadcastManager.getInstance(it).unregisterReceiver(locationBroadcastReceiver)
      }
    }
  }

  @ReactMethod
  fun onAppMoveToBackground() {
    currentActivity?.let {
      if (usingNavigation) {
        SYRFNavigation.onAppMoveToBackground(it)
      } else {
        SYRFLocation.onStop(it)
      }
    }
  }

  private fun requestLocationPermission() {
    currentActivity?.let { activity ->
      val config =
        permissionRequestConfig ?: SYRFPermissionRequestConfig.getDefault(activity)
      PermissionsManager(activity).showPermissionReasonAndRequest(
        config,
        onPositionClick = {
          (activity as? PermissionAwareActivity)?.requestPermissions(
            arrayOf(
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_PERMISSION_CODE,
            this
          )
        },
        onNegativeClick = {}
      )
    }
  }

  @ReactMethod
  fun startHeadingUpdates() {
    currentActivity?.let { activity ->
      SYRFRotationSensor.subscribeToSensorDataUpdates(activity) {}
      LocalBroadcastManager.getInstance(activity).registerReceiver(
        headingBroadcastReceiver,
        IntentFilter(Constants.ACTION_ROTATION_SENSOR_BROADCAST)
      )
    }
  }

  @ReactMethod
  fun stopHeadingUpdates() {
    SYRFRotationSensor.unsubscribeToSensorDataUpdates()
    currentActivity?.let {
      LocalBroadcastManager.getInstance(it).unregisterReceiver(headingBroadcastReceiver)
    }
  }

  private inner class LocationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      intent.getParcelableExtra<SYRFLocationData>(EXTRA_LOCATION)?.let { location ->
        sendEvent(reactApplicationContext, UPDATE_LOCATION_EVENT, location.toMap())
      }
    }
  }

  private inner class HeadingBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      intent.getParcelableExtra<SYRFRotationSensorData>(EXTRA_ROTATION_SENSOR_DATA)
        ?.let { sensorData ->
          calculateOrientations(
            floatArrayOf(
              sensorData.x,
              sensorData.y,
              sensorData.z,
              sensorData.s
            )
          )?.let { rotationData ->
            sendEvent(reactApplicationContext, UPDATE_HEADING_EVENT, rotationData.toMap())
          }
        }
    }
  }

  private inner class NavigationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val locationMap =
        intent.getParcelableExtra<SYRFNavigationData>(Constants.EXTRA_NAVIGATION)?.location?.toMap()
      val headingMap =
        intent.getParcelableExtra<SYRFNavigationData>(Constants.EXTRA_NAVIGATION)?.sensorData?.let {
          calculateOrientations(floatArrayOf(it.x, it.y, it.z, it.s))?.toMap()
        }
      val deviceInfoMap =
        intent.getParcelableExtra<SYRFNavigationData>(Constants.EXTRA_NAVIGATION)?.deviceInfo?.toMap()

      val params = Arguments.createMap()
      params.putMap("location", locationMap)
      params.putMap("heading", headingMap)
      params.putMap("deviceInfo", deviceInfoMap)

      sendEvent(reactApplicationContext, UPDATE_NAVIGATION_EVENT, params)
    }
  }

  private fun activityDisplay(): Display? {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
      return currentActivity?.display
    }
    @Suppress("DEPRECATION")
    return currentActivity?.windowManager?.defaultDisplay
  }

  private fun calculateOrientations(rotationValues: FloatArray): SYRFRotationData? {
    activityDisplay()?.let { display ->
      SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationValues)
      val (matrixColumn, sense) = when (val rotation = display.rotation) {
        Surface.ROTATION_0 -> Pair(0, 1)
        Surface.ROTATION_90 -> Pair(1, -1)
        Surface.ROTATION_180 -> Pair(0, -1)
        Surface.ROTATION_270 -> Pair(1, 1)
        else -> error("Invalid screen rotation value: $rotation")
      }
      val x = sense * rotationMatrix[matrixColumn]
      val y = sense * rotationMatrix[matrixColumn + 3]
      val azimuth = (-kotlin.math.atan2(y.toDouble(), x.toDouble()))

      SensorManager.getOrientation(rotationMatrix, orientation)

      return SYRFRotationData(
        azimuth = azimuth.toFloat(),
        pitch = orientation[1],
        roll = orientation[2],
        timestamp = System.currentTimeMillis()
      )
    }
    return null
  }
}
