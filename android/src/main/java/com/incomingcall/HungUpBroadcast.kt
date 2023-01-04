package com.incomingcall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class HungUpBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (CallingActivity.active) {
            context?.sendBroadcast(Intent("DESTROY_CALL"))
        }

        val stopIntent = Intent(context, CallService::class.java).setAction("STOP_ACTION")
        context?.stopService(stopIntent)
    }
}
