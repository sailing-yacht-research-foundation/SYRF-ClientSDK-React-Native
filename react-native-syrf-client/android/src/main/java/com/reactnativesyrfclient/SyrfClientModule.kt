package com.reactnativesyrfclient

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.SensorManager
import android.net.Uri
import android.provider.Settings
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
    const val KEY_PROVIDER = "provider"
    const val KEY_MAX_LOCATION_ACCURACY = "desiredAccuracy"

    const val KEY_PERMISSION_REQUEST_CONFIG = "permissionRequestConfig"
    const val KEY_PERMISSION_REQUEST_TITLE = "title"
    const val KEY_PERMISSION_REQUEST_MESSAGE = "message"
    const val KEY_PERMISSION_REQUEST_OK_BTN = "okButton"
    const val KEY_PERMISSION_REQUEST_CANCEL_BTN = "cancelButton"

    const val KEY_LOCATION = "location"
    const val KEY_HEADING = "heading"
    const val KEY_DEVICE_INFO = "deviceInfo"

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
    const val REQUEST_PERMISSION_USING_NAVIGATION_CODE = 2
    const val REQUEST_PERMISSION_LOCATION_UPDATE_CODE = 3
    const val REQUEST_PERMISSION_CURRENT_LOCATION_CODE = 4

    const val THROTTLE_FOREGROUND_DELAY = "throttleForegroundDelay"
    const val THROTTLE_BACKGROUND_DELAY = "throttleBackgroundDelay"
  }

  private val locationBroadcastReceiver = LocationBroadcastReceiver()
  private val headingBroadcastReceiver = HeadingBroadcastReceiver()
  private val navigationBroadcastReceiver = NavigationBroadcastReceiver()
  private var permissionRequestConfig: SYRFPermissionRequestConfig? = null

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

  private val mActivityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(
      activity: Activity,
      requestCode: Int,
      resultCode: Int,
      intent: Intent?
    ) {
      if (requestCode == REQUEST_PERMISSION_USING_NAVIGATION_CODE) {
        SYRFNavigation.subscribeToNavigationUpdates(activity) { _, error ->
          if (error != null) {
            if (error is MissingLocationException) {
              requestLocationPermission(requestCode)
            }
            return@subscribeToNavigationUpdates
          }
        }
      }
      if (requestCode == REQUEST_PERMISSION_LOCATION_UPDATE_CODE) {
        SYRFLocation.subscribeToLocationUpdates(activity) { _, error ->
          if (error != null) {
            if (error is MissingLocationException) {
              requestLocationPermission(requestCode)
            }
            return@subscribeToLocationUpdates
          }
        }
      }
      if (requestCode == REQUEST_PERMISSION_CURRENT_LOCATION_CODE) {
        getCurrentLocation()
      }
    }
  }

  init {
    reactContext.addActivityEventListener(mActivityEventListener)
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
          if (requestCode == REQUEST_PERMISSION_USING_NAVIGATION_CODE) {
            SYRFNavigation.subscribeToNavigationUpdates(activity) { _, error ->
              if (error != null) {
                if (error is MissingLocationException) {
                  requestLocationPermission(requestCode)
                }
                return@subscribeToNavigationUpdates
              }
            }
          }
          if (requestCode == REQUEST_PERMISSION_LOCATION_UPDATE_CODE) {
            SYRFLocation.subscribeToLocationUpdates(activity) { _, error ->
              if (error != null) {
                if (error is MissingLocationException) {
                  requestLocationPermission(requestCode)
                }
                return@subscribeToLocationUpdates
              }
            }
          }
          if (requestCode == REQUEST_PERMISSION_CURRENT_LOCATION_CODE) {
            getCurrentLocation()
          }
        },
        showRequestPermissionRationale = {
          if (it == Manifest.permission.ACCESS_FINE_LOCATION) {
            currentActivity?.let {
              val alertDialogBuilder = AlertDialog.Builder(it)
              alertDialogBuilder.setTitle("Permission")
              alertDialogBuilder.setMessage("Please go to Permissions -> Location to enable \"Use precise location\".")
              alertDialogBuilder.setCancelable(false)
              alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                try {
                  it.startActivityForResult(
                    Intent().apply {
                      action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                      data = Uri.fromParts("package", it.packageName, null)
                    },
                    requestCode
                  )
                } catch (e: Exception) {
                }
                dialog.dismiss()
              }
              alertDialogBuilder.show()
            }
          }
        },
        exceptionCallback = {},
      )
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

      val locationParams = getMapOrNull(params, KEY_LOCATION)

      getLongOrNull(locationParams, KEY_UPDATE_INTERVAL)?.let {
        builder.updateInterval(it)
      }
      getIntOrNull(locationParams, KEY_MAX_LOCATION_ACCURACY)?.let {
        builder.maximumLocationAccuracy(it)
      }
      getMapOrNull(locationParams, KEY_PERMISSION_REQUEST_CONFIG)?.let { permissionRequestParams ->
        permissionRequestConfig = getSYRFPermissionRequestConfig(permissionRequestParams)
      }

      getStringOrDefault(locationParams, KEY_PROVIDER, "gps")?.let {
        builder.provider(it)
      }

      val navigationConfig = SYRFNavigationConfig(
        locationConfig = builder.set(),
        headingConfig = SYRFRotationConfig.Builder().set(),
        deviceInfoConfig = SYRFDeviceInfoConfig(true),
        throttleForegroundDelay = getIntOrDefault(params, THROTTLE_FOREGROUND_DELAY, 1000),
        throttleBackgroundDelay = getIntOrDefault(params, THROTTLE_BACKGROUND_DELAY, 2000),
      )

      SYRFNavigation.configure(navigationConfig, activity)

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
      val location = getBooleanOrNull(params, KEY_LOCATION)
      val heading = getBooleanOrNull(params, KEY_HEADING)
      val deviceInfo = getBooleanOrNull(params, KEY_DEVICE_INFO)

      val toggler = SYRFToggler(
        location = location,
        heading = heading,
        deviceInfo = deviceInfo
      )
      SYRFNavigation.updateNavigationSettings(toggler, activity) { _, error ->
        if (error != null) {
          if (error is MissingLocationException) {
            requestLocationPermission(REQUEST_PERMISSION_USING_NAVIGATION_CODE)
          }
        }
      }

      if (location == true || heading == true) {
        LocalBroadcastManager.getInstance(activity).registerReceiver(
          navigationBroadcastReceiver,
          IntentFilter(Constants.ACTION_NAVIGATION_BROADCAST)
        )
      } else {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(navigationBroadcastReceiver)
      }
    }
  }

  @ReactMethod
  fun getCurrentNavigation(params: ReadableMap, promise: Promise) {
    currentActivity?.let { activity ->
      val location = getBooleanOrNull(params, KEY_LOCATION)
      val heading = getBooleanOrNull(params, KEY_HEADING)
      val deviceInfo = getBooleanOrNull(params, KEY_DEVICE_INFO)

      val toggler = SYRFToggler(
        location = location,
        heading = heading,
        deviceInfo = deviceInfo
      )
      SYRFNavigation.getCurrentNavigation(toggler, activity) { navigationData, error ->
        if (error != null) {
          if (error is MissingLocationException) {
            requestLocationPermission(REQUEST_PERMISSION_CURRENT_LOCATION_CODE)
          }
        } else {
          navigationData?.let {
            val locationMap = navigationData.location?.toMap()
            val headingMap =
              navigationData.sensorData?.let {
                calculateOrientations(floatArrayOf(it.x, it.y, it.z, it.s))?.toMap()
              }
            val deviceInfoMap = navigationData.deviceInfo?.toMap()

            val navigationDataParams = Arguments.createMap()
            navigationDataParams.putMap("location", locationMap)
            navigationDataParams.putMap("heading", headingMap)
            navigationDataParams.putMap("deviceInfo", deviceInfoMap)
            sendEvent(reactApplicationContext, UPDATE_NAVIGATION_EVENT, navigationDataParams)
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
            requestLocationPermission(REQUEST_PERMISSION_LOCATION_UPDATE_CODE)
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
      SYRFLocation.getCurrentPosition(activity) { location, error ->
        if (error != null) {
          if (error is MissingLocationException) {
            requestLocationPermission(REQUEST_PERMISSION_CURRENT_LOCATION_CODE)
          }
          return@getCurrentPosition
        }
        location?.let {
          sendEvent(reactApplicationContext, CURRENT_LOCATION_EVENT, it.toMap())
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

  @ReactMethod
  fun onAppMoveToForeground() {
    currentActivity?.let {
      if (usingNavigation) {
        SYRFNavigation.onAppMoveToForeground(it)
      }
    }
  }

  private fun requestLocationPermission(requestCode: Int) {
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
            requestCode,
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
