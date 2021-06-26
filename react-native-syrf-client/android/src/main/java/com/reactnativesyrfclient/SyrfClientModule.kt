package com.reactnativesyrfclient

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.PermissionListener
import com.syrf.location.configs.SYRFLocationConfig
import com.syrf.location.configs.SYRFPermissionRequestConfig
import com.syrf.location.interfaces.SYRFLocation
import com.syrf.location.utils.Constants
import com.syrf.location.utils.Constants.EXTRA_LOCATION
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
    const val LOCATION_LAT = "lat"
    const val LOCATION_LON = "lon"
    const val LOCATION_TIME = "time"
  }

  private val locationBroadcastReceiver = LocationBroadcastReceiver()

  override fun getName(): String {
    return "SyrfClient"
  }

  override fun getConstants(): Map<String, Any> {
    val constants: MutableMap<String, Any> = HashMap()
    constants[UPDATE_LOCATION_EVENT] = UPDATE_LOCATION_EVENT
    return constants
  }

  // Todo: update code for using [PermissionAwareActivity].requestPermissions
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ): Boolean {
    currentActivity?.let { activity ->
      SYRFLocation.onRequestPermissionsResult(requestCode, permissions, grantResults, activity)
    }
    return true
  }

  @ReactMethod
  fun configure(params: ReadableMap, promise: Promise) {
    val activity = currentActivity ?: run {
      promise.resolve(false)
      return
    }

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

      val permissionRequestConfig = permissionConfigBuilder.set(activity)
      builder.permissionRequestConfig(permissionRequestConfig)
    }

    SYRFTime.configure(SYRFTimeConfig.Builder().set(), activity)
    SYRFLocation.configure(builder.set(), activity)
    promise.resolve(true)
  }

  @ReactMethod
  fun startLocationUpdates() {
    val activity = currentActivity ?: return

    LocalBroadcastManager.getInstance(activity).registerReceiver(
      locationBroadcastReceiver,
      IntentFilter(Constants.ACTION_LOCATION_BROADCAST)
    )

    SYRFLocation.subscribeToLocationUpdates(activity)
  }

  @ReactMethod
  fun stopLocationUpdates() {
    val activity = currentActivity ?: return

    SYRFLocation.unsubscribeToLocationUpdates()

    LocalBroadcastManager.getInstance(activity).unregisterReceiver(
      locationBroadcastReceiver
    )
  }

  private inner class LocationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
      val location = intent.getParcelableExtra<Location>(EXTRA_LOCATION)

      if (location != null) {
        val params = Arguments.createMap()
        params.putDouble(LOCATION_LAT, location.latitude)
        params.putDouble(LOCATION_LON, location.longitude)
        params.putDouble(LOCATION_TIME, SYRFTime.getCurrentTimeMS().toDouble())
        sendEvent(reactApplicationContext, UPDATE_LOCATION_EVENT, params)
      }
    }
  }
}
