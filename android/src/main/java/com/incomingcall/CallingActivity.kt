package com.incomingcall

import android.Manifest
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.ReactActivity
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import kotlinx.android.synthetic.main.call_fullscreen.*


class CallingActivity : ReactActivity() {

  override fun onStart() {
    super.onStart()
    active = true
  }

  override fun onDestroy() {
    active = false
    super.onDestroy()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val bundle = intent.extras

    if (allPermissionsGranted()) {
      startCamera()
    } else {
      ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true)
      setTurnScreenOn(true)
      //Some devices need the code below to work when the device is locked
      val keyguardManager = getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager
      if (keyguardManager.isDeviceLocked) {
        val keyguardLock = keyguardManager.newKeyguardLock(TAG_KEYGUARD)
        keyguardLock.disableKeyguard()
      }
    }
    window.addFlags(
      WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
    )

    setContentView(R.layout.call_fullscreen)

    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
      View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    val mIntentFilter = IntentFilter();
    mIntentFilter.addAction(Constants.ACTION_END_INCOMING_CALL);
    registerReceiver(mBroadcastReceiver, mIntentFilter)

    acceptButton.setOnClickListener {
      stopService(Intent(this, CallService::class.java))
      val answerIntent = Intent(this, AnswerCallActivity::class.java)
      val component = bundle?.getString("component")
      val accessToken = bundle?.getString("accessToken")
      answerIntent.putExtra("component", component)
      answerIntent.putExtra("accessToken", accessToken)
      startActivity(answerIntent)
      finishAndRemoveTask()
    }

    declineButton.setOnClickListener {
      stopService(Intent(this, CallService::class.java))
      finishAndRemoveTask()
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<String>, grantResults:
    IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_CODE_PERMISSIONS) {
      // If all permissions granted , then start Camera
      if (allPermissionsGranted()) {
        startCamera()
      } else {
        // finishAndRemoveTask()
      }
    }
  }

  private fun startCamera() {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

    cameraProviderFuture.addListener(Runnable {

      val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

      val preview = Preview.Builder()
        .build()
        .also {
          it.setSurfaceProvider(viewFinder.surfaceProvider)
        }

      val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

      try {
        // Unbind use cases before rebinding
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
          this, cameraSelector, preview
        )
      } catch (exc: Exception) {
      }

    }, ContextCompat.getMainExecutor(this))
  }

  private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
    ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
  }

  private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
      if (intent.action == Constants.ACTION_END_INCOMING_CALL) {
        finishAndRemoveTask()
      }
    }
  }

  companion object {
    var active = false
    private const val TAG_KEYGUARD = "Incoming:unLock"
    private const val REQUEST_CODE_PERMISSIONS = 20
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
  }
}
