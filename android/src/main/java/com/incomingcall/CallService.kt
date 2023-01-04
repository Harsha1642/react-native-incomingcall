package com.incomingcall

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat


class CallService : Service() {
    private var ringtone: Ringtone? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1000)
        ringtone?.stop()
        handler!!.removeCallbacks(runnable!!)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        ringtone = RingtoneManager.getRingtone(
            this,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        )

        val customView = RemoteViews(packageName, R.layout.call_notification)

        val notificationIntent = Intent(this, CallingActivity::class.java)
        val hungupIntent = Intent(this, HungUpBroadcast::class.java)
        var answerIntent = Intent(this, AnswerCallActivity::class.java)

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
                "IncomingCall",
                "IncomingCall", NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.setSound(null, null)
            notificationChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

            notificationManager.createNotificationChannel(notificationChannel)
            val notification = NotificationCompat.Builder(this, "IncomingCall")
            notification.setContentTitle("Harsha")
            notification.setTicker("Call_STATUS")
            notification.setContentText("IncomingCall")
            notification.setSmallIcon(R.drawable.incoming_video_call)
            // notification.setSilent(true)
            notification.setCategory(NotificationCompat.CATEGORY_CALL)
            notification.setOngoing(true)
            notification.setFullScreenIntent(pendingIntent, true)
            notification.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            notification.setCustomContentView(customView)
            notification.setCustomBigContentView(customView)
            // notificationManager.notify(1000, notification.build())
            startForeground(1000, notification.build())
            ringtone?.play()
            runnable = Runnable {
                run {
                    println("Harsha: Handler Closed")
                    stopSelf()
                    if (CallingActivity.active) {
                        sendBroadcast(Intent("DESTROY_CALL"))
                    }
                }
            }
            handler = Handler(Looper.getMainLooper())
            handler!!.postDelayed(runnable!!, 30000)
        }
        return START_NOT_STICKY

    }

    companion object {
        var handler: Handler? = null
        var runnable: Runnable? = null
    }
}
