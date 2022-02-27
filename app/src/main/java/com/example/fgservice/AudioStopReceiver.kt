package com.example.fgservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AudioStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val targetIntent = Intent(context, ForegroundService::class.java)
        if(context is ForegroundService) {
            Log.d("debug", "cast")
            context.pcm?.stop()
            context.pcm?.flush()
        } else {
            Log.d("debug", "cannot cast")
        }
        context.stopService(targetIntent)
    }
}
