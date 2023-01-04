package com.incomingcall

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter


class IncomingCallModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun onDisplayIncomingCall() {
    reactApplicationContext.stopService(
      Intent(
        reactApplicationContext,
        CallService::class.java
      )
    )
    reactApplicationContext.startForegroundService(
      Intent(
        reactApplicationContext,
        CallService::class.java
      )
    )
  }

  @ReactMethod
  fun sendEventToJs(eventName: String, params: WritableMap?) {
    reactApplicationContext?.getJSModule(RCTDeviceEventEmitter::class.java)
      ?.emit(eventName, params)
  }

  companion object {
    val NAME = "IncomingCall"
  }

}
