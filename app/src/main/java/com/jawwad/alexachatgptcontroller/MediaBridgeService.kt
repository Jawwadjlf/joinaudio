package com.jawwad.alexachatgptcontroller

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

class MediaBridgeService : Service() {
    private var mediaSession: MediaSessionCompat? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Ready for Alexa media commands"))
        createMediaSession()
    }

    private fun createMediaSession() {
        val session = MediaSessionCompat(this, "AlexaChatGPTVirtualPlayer")
        session.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        session.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                recordEvent("BLUETOOTH CALLBACK: PLAY")
                setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                updateNotification("Received PLAY from Bluetooth/Alexa")
            }
            override fun onPause() {
                recordEvent("BLUETOOTH CALLBACK: PAUSE")
                setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                updateNotification("Received PAUSE from Bluetooth/Alexa")
            }
            override fun onStop() {
                recordEvent("BLUETOOTH CALLBACK: STOP")
                setPlaybackState(PlaybackStateCompat.STATE_STOPPED)
                updateNotification("Received STOP from Bluetooth/Alexa")
            }
            override fun onSkipToNext() { recordEvent("BLUETOOTH CALLBACK: NEXT") }
            override fun onSkipToPrevious() { recordEvent("BLUETOOTH CALLBACK: PREVIOUS") }
        })
        session.setMetadata(MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Alexa ChatGPT Controller")
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Virtual Media Player")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 60000L)
            .build())
        session.isActive = true
        mediaSession = session
        setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
    }

    private fun setPlaybackState(state: Int) {
        val actions = PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_STOP or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        mediaSession?.setPlaybackState(PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
            .build())
    }

    private fun recordEvent(event: String) {
        sendBroadcast(Intent(ACTION_EVENT).setPackage(packageName)
            .putExtra(EXTRA_EVENT, event))
    }

    private fun updateNotification(message: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(message))
    }

    private fun buildNotification(message: String): Notification {
        val openApp = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0))
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alexa Media Diagnostic")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openApp)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Virtual media session",
                NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (mediaSession == null) createMediaSession()
        return START_STICKY
    }

    override fun onDestroy() {
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.jawwad.alexachatgptcontroller.START"
        const val ACTION_EVENT = "com.jawwad.alexachatgptcontroller.MEDIA_EVENT"
        const val EXTRA_EVENT = "event"
        private const val CHANNEL_ID = "virtual_media_session"
        private const val NOTIFICATION_ID = 4107
    }
}
