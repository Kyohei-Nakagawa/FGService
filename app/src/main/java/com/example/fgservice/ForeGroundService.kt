package com.example.fgservice


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*


class ForegroundService : Service() {
    companion object {
        const val NOTIFICATION_ID = 10
        const val CHANNEL_ID = "primary_notification_channel"
    }

    var pcm: AudioTrack? = null

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        var istream = resources.openRawResource(R.raw.aoi)
        val bufSize: Int = istream.available()
        var rawData = ByteArray(bufSize)
        try {
            var readBytes: String = String.format(Locale.US, "read bytes = %d", istream.read(rawData))
            /*Log.d("debug", "size=" + bufSize.toString())
            for(i in 0 until bufSize step 4096) {
                Log.d("debug", "rawData[" + i + "]=" + rawData[i].toString())
            }*/
            for(i in 0 until bufSize step 1) {
                rawData[i] = (rawData[i] * 8).toByte()
            }

            val sendIntent = Intent(this, AudioStopReceiver::class.java).apply {
                action = Intent.ACTION_SEND
            }
            val sendPendingIntent = PendingIntent.getBroadcast(this, 0, sendIntent, 0)

            createNotificationChannel()
            val notification = NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("タイトル")
                .setContentText("内容")
                .addAction(R.drawable.ic_launcher_foreground, "停止する", sendPendingIntent)
                .build()
            Log.d("debug", "makeNotification")
            Thread(
                Runnable {
                    (0..15).map {
                        Thread.sleep(1000)
                    }
                    stopForeground(Service.STOP_FOREGROUND_DETACH)
            }).start()

            startForeground(NOTIFICATION_ID, notification)
            Log.d("debug", "startForeground")

            pcm = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(44100)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            pcm?.setPlaybackPositionUpdateListener(
                object : AudioTrack.OnPlaybackPositionUpdateListener {
                    override fun onPeriodicNotification(track: AudioTrack){
                    }
                    override fun onMarkerReached(track: AudioTrack) {
                        //再生完了
                        Log.d("debug", "audio stop")
                        stopForeground(Service.STOP_FOREGROUND_REMOVE)
                    }
                }
            )
            pcm?.setNotificationMarkerPosition(bufSize / 2)
            pcm?.write(rawData, 0, bufSize, AudioTrack.WRITE_NON_BLOCKING)
            pcm?.play()
        } catch (fne: FileNotFoundException) {
            fne.printStackTrace()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        } finally {
            try {
                if(istream != null) {
                    istream.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return START_NOT_STICKY
        }
    }

    override fun onDestroy() {
        Log.d("debug", "stop service")
        pcm?.stop()
        pcm?.flush()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            "MyApp notification",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description = "AppApp Tests"
        Log.d("debug", "createNotificationChannel()")

        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(
            notificationChannel)
    }
}
