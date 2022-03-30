package com.reactnativesyrfclient

import android.Manifest
import android.app.Activity
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
import com.syrf.location.configs.SYRFLocationConfig
import com.syrf.location.configs.SYRFMagneticConfig
import com.syrf.location.configs.SYRFPermissionRequestConfig
import com.syrf.location.configs.SYRFRotationConfig
import com.syrf.location.data.SYRFLocationData
import com.syrf.location.data.SYRFMagneticSensorData
import com.syrf.location.data.SYRFRotationSensorData
import com.syrf.location.data.SYRFRotationData
import com.syrf.location.interfaces.SYRFLocation
import com.syrf.location.interfaces.SYRFMagneticSensor
import com.syrf.location.interfaces.SYRFRotationSensor
import com.syrf.location.permissions.PermissionsManager
import com.syrf.location.utils.Constants
import com.syrf.location.utils.Constants.EXTRA_LOCATION
import com.syrf.location.utils.Constants.EXTRA_MAGNETIC_SENSOR_DATA
import com.syrf.location.utils.Constants.EXTRA_ROTATION_SENSOR_DATA
import com.syrf.location.utils.MissingLocationException
import com.syrf.time.configs.SYRFTimeConfig
import com.syrf.time.interfaces.SYRFTime
import java.util.*

class SyrfClientModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), PermissionListener {

  companion object {
    const val KEY_UPDATE_INTERVAL = "updateInterval"
    const val KEY_MAX_LOCATION_ACCURACY = "maximumLocationAccuracy"
    const val KEY_PERMISSION_REQUEST_CONFIG = "permissionRequestConfig"
    const val KEY_PERMISSION_REQUEST_TITLE = "title"
    const val KEY_PERMISSION_REQUEST_MESSAGE = "message"
    const val KEY_PERMISSION_REQUEST_OK_BTN = "okButton"
    const val KEY_PERMISSION_REQUEST_CANCEL_BTN = "cancelButton"

    const val UPDATE_LOCATION_EVENT = "UPDATE_LOCATION_EVENT"
    const val CURRENT_LOCATION_EVENT = "CURRENT_LOCATION_EVENT"
    const val UPDATE_HEADING_EVENT = "UPDATE_HEADING_EVENT"
    const val LOCATION_LAT = "latitude"
    const val LOCATION_LON = "longitude"
    const val LOCATION_TIME = "timestamp"
    const val LOCATION_ACCURACY = "accuracy"
    const val LOCATION_SPEED = "speed"
    const val LOCATION_HEADING = "heading"

    const val HEADING_X = "x"
    const val HEADING_Y = "y"
    const val HEADING_Z = "z"
    const val RAW_DATA = "rawData"
    const val HEADING_TIME = "timestamp"

    const val REQUEST_PERMISSION_CODE = 1
  }

  private val locationBroadcastReceiver = LocationBroadcastReceiver()
  private val headingBroadcastReceiver = HeadingBroadcastReceiver()
  private var permissionRequestConfig: SYRFPermissionRequestConfig? = null

  private var waitingForLocationPermission = false
  private var waitingForCurrentLocationPermission = false

  private val rotationMatrix = FloatArray(9)
  private val orientation = FloatArray(3)

  override fun getName(): String {
    return "SyrfClient"
  }

  override fun getConstants(): Map<String, Any> {
    val constants: MutableMap<String, Any> = HashMap()
    constants[UPDATE_LOCATION_EVENT] = UPDATE_LOCATION_EVENT
    constants[CURRENT_LOCATION_EVENT] = CURRENT_LOCATION_EVENT
    constants[UPDATE_HEADING_EVENT] = UPDATE_HEADING_EVENT
    return constants
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ): Boolean {
    if (currentActivity != null) {
      PermissionsManager(currentActivity!!).handleResults(
        permissions,
        successCallback = {
          if (waitingForLocationPermission) {
            SYRFLocation.subscribeToLocationUpdates(currentActivity!!)
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

  @ReactMethod
  fun configure(params: ReadableMap, promise: Promise) {
    if (currentActivity != null) {
      val builder = SYRFLocationConfig.Builder()
      getLongOrNull(params, KEY_UPDATE_INTERVAL)?.let {
        builder.updateInterval(it)
      }
      getIntOrNull(params, KEY_MAX_LOCATION_ACCURACY)?.let {
        builder.maximumLocationAccuracy(it)
      }
      getMapOrNull(params, KEY_PERMISSION_REQUEST_CONFIG)?.let { permissionRequestParams ->
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

        permissionRequestConfig = permissionConfigBuilder.set(currentActivity!!)
      }

      SYRFTime.configure(SYRFTimeConfig.Builder().set(), currentActivity!!)
      SYRFLocation.configure(builder.set(), currentActivity!!)
      SYRFRotationSensor.configure(SYRFRotationConfig.Builder().set(), currentActivity!!)
      promise.resolve(true)
    }
  }

  @ReactMethod
  fun startLocationUpdates() {
    if (currentActivity != null) {
      SYRFLocation.subscribeToLocationUpdates(currentActivity!!) { _, error ->
        if (error != null) {
          if (error is MissingLocationException) {
            waitingForLocationPermission = true
            requestLocationPermission()
          }
          return@subscribeToLocationUpdates
        }
      }
      LocalBroadcastManager.getInstance(currentActivity!!).registerReceiver(
        locationBroadcastReceiver,
        IntentFilter(Constants.ACTION_LOCATION_BROADCAST)
      )
    }
  }

  @ReactMethod
  fun getCurrentLocation() {
    if (currentActivity != null) {
      SYRFLocation.getCurrentPosition(currentActivity!!) { location, error ->
        if (error != null) {
          if (error is MissingLocationException) {
            waitingForCurrentLocationPermission = true
            requestLocationPermission()
          }
          return@getCurrentPosition
        }
        if (location != null) {
          val params = locationToMap(location)
          sendEvent(reactApplicationContext, CURRENT_LOCATION_EVENT, params)
        }
      }
    }
  }

  @ReactMethod
  fun stopLocationUpdates() {
    SYRFLocation.unsubscribeToLocationUpdates()
    currentActivity?.let {
      LocalBroadcastManager.getInstance(it).unregisterReceiver(locationBroadcastReceiver)
    }
  }

  @ReactMethod
  fun onAppMoveToBackground() {
    currentActivity?.let { SYRFLocation.onStop(it) }
  }

  private fun requestLocationPermission() {
    val config = permissionRequestConfig
    if (currentActivity != null) {
      PermissionsManager(currentActivity!!).showPermissionReasonAndRequest(
        config!!,
        onPositionClick = {
          (currentActivity as? PermissionAwareActivity)?.requestPermissions(
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
    if (currentActivity != null) {
      SYRFRotationSensor.subscribeToSensorDataUpdates(currentActivity!!) {}
      LocalBroadcastManager.getInstance(currentActivity!!).registerReceiver(
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


  private fun locationToMap(location: SYRFLocationData): WritableMap {
    val params = Arguments.createMap()
    params.putDouble(LOCATION_LAT, location.latitude)
    params.putDouble(LOCATION_LON, location.longitude)
    params.putDouble(LOCATION_TIME, location.timestamp.toDouble())
    params.putDouble(LOCATION_ACCURACY, location.horizontalAccuracy.toDouble())
    params.putDouble(LOCATION_SPEED, location.speed.toDouble())
    params.putDouble(LOCATION_HEADING, location.trueHeading.toDouble())
    return params;
  }

  private inner class LocationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
      val location = intent.getParcelableExtra<SYRFLocationData>(EXTRA_LOCATION)

      if (location != null) {
        val params = locationToMap(location)
        sendEvent(reactApplicationContext, UPDATE_LOCATION_EVENT, params)
      }
    }
  }

  private inner class HeadingBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      var rotationSensorData: FloatArray? = null
      val data = intent.getParcelableExtra<SYRFRotationSensorData>(EXTRA_ROTATION_SENSOR_DATA)
      rotationSensorData = if (data != null) floatArrayOf(data.x, data.y, data.z, data.s) else {
        null
      }

      if (rotationSensorData != null) {
        val rotationData = calculateOrientations(rotationSensorData)
        if (rotationData != null) {
          val params = headingToMap(rotationData)
          sendEvent(reactApplicationContext, UPDATE_HEADING_EVENT, params)
        }
      }
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
    if (activityDisplay() != null) {
      SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationValues)
      val (matrixColumn, sense) = when (val rotation =
        activityDisplay()?.rotation
      ) {
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

  private fun headingToMap(heading: SYRFRotationData): WritableMap {
    val params = Arguments.createMap()
    val rawParams = Arguments.createMap()

    var h = heading.azimuth.toDouble();
    if (h < 0.0) {
      h += 2 * Math.PI;
    }
    h = h * 180 / Math.PI;

    rawParams.putDouble(HEADING_X, h)
    rawParams.putDouble(HEADING_Y, heading.pitch.toDouble())
    rawParams.putDouble(HEADING_Z, heading.roll.toDouble())
    params.putMap(RAW_DATA, rawParams)
    params.putDouble(HEADING_TIME, heading.timestamp.toDouble())
    return params;
  }

}
