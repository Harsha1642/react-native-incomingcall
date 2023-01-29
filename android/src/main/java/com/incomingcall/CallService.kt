package com.incomingcall

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.*
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import okhttp3.internal.notify


class CallService : Service() {
  private var ringtone: Ringtone? = null

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  @RequiresApi(Build.VERSION_CODES.S)
  override fun onDestroy() {
    super.onDestroy()
    removeNotification()
    stopRingtone()
    stopVibration()
    cancelTimer()
  }


  @RequiresApi(Build.VERSION_CODES.S)
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

    val bundle = intent?.extras
    val timeout = bundle?.getLong("timeout") ?: Constants.TIME_OUT

    val notification: Notification = buildNotification(intent)
    startForeground(Constants.FOREGROUND_SERVICE_ID, notification)
    playRingtone()
    startVibration()
    startTimer(timeout)

    return START_NOT_STICKY
  }

  private fun buildNotification(intent: Intent?): Notification {

    val bundle = intent?.extras
    val channelName = bundle?.getString("channelName") ?: Constants.INCOMING_CALL
    val channelId = bundle?.getString("channelId") ?: Constants.INCOMING_CALL
    val component = bundle?.getString("component")
    val accessToken = bundle?.getString("accessToken")

    val customView = RemoteViews(packageName, R.layout.call_notification)

    val notificationIntent = Intent(this, CallingActivity::class.java)
    val hungupIntent = Intent(this, HungUpBroadcast::class.java)
    var answerIntent = Intent(this, AnswerCallActivity::class.java)

    notificationIntent.putExtra("component", component)
    notificationIntent.putExtra("accessToken", accessToken)

    answerIntent.putExtra("component", component)
    answerIntent.putExtra("accessToken", accessToken)

    val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, flag)
    val hungupPendingIntent = PendingIntent.getBroadcast(this, 0, hungupIntent, flag)
    val answerPendingIntent = PendingIntent.getActivity(this, 0, answerIntent, flag)

    customView.setOnClickPendingIntent(R.id.btnAnswer, answerPendingIntent)
    customView.setOnClickPendingIntent(R.id.btnDecline, hungupPendingIntent)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val notificationChannel = NotificationChannel(
        channelId,
        channelName, NotificationManager.IMPORTANCE_HIGH
      )
      notificationChannel.setSound(null, null)
      notificationChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

      notificationManager.createNotificationChannel(notificationChannel)
    }
    val notification = NotificationCompat.Builder(this, channelId)
    notification.setContentTitle(Constants.INCOMING_CALL)
    notification.setTicker(Constants.INCOMING_CALL)
    notification.setContentText(Constants.INCOMING_CALL)
    notification.setSmallIcon(R.drawable.incoming_video_call)
    notification.setCategory(NotificationCompat.CATEGORY_CALL)
    notification.setOngoing(true)
    notification.setFullScreenIntent(pendingIntent, true)
    notification.setStyle(NotificationCompat.DecoratedCustomViewStyle())
    notification.setCustomContentView(customView)
    notification.setCustomBigContentView(customView)

    return notification.build()
  }

  private fun removeNotification() {
    val notificationManager =
      getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(Constants.FOREGROUND_SERVICE_ID)
  }

  private fun startTimer(timeout: Long) {
    runnable = Runnable {
      run {
        stopSelf()
        if (CallingActivity.active) {
          sendBroadcast(Intent(Constants.ACTION_END_INCOMING_CALL))
        }
      }
    }
    handler = Handler(Looper.getMainLooper())
    handler!!.postDelayed(runnable!!, timeout)
  }

  private fun cancelTimer() {
    handler!!.removeCallbacks(runnable!!)
  }

  private fun playRingtone() {
    ringtone = RingtoneManager.getRingtone(
      this,
      RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    )
    ringtone?.play()
  }

  private fun stopRingtone() {
    ringtone?.stop()
  }

  @RequiresApi(Build.VERSION_CODES.S)
  private fun startVibration() {
    val vibratePattern = longArrayOf(0, 1000, 1000)

    vibrator = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    vibrator!!.defaultVibrator.run {
      vibrate(VibrationEffect.createWaveform(vibratePattern, 0))
    }
  }

  @RequiresApi(Build.VERSION_CODES.S)
  private fun stopVibration() {
    vibrator?.cancel()
  }

  companion object {
    var handler: Handler? = null
    var runnable: Runnable? = null
    var vibrator: VibratorManager? = null
  }
}
