package com.jawwad.alexachatgptcontroller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var logView: TextView

    private val eventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val event = intent?.getStringExtra(MediaBridgeService.EXTRA_EVENT) ?: return
            appendLog(event)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 24)
        }
        root.addView(TextView(this).apply {
            text = "Alexa Bluetooth Media Diagnostic"
            textSize = 22f
        })
        root.addView(TextView(this).apply {
            text = "Start the virtual media player, then use Alexa Play and Stop."
            textSize = 15f
            setPadding(0, 16, 0, 16)
        })
        root.addView(Button(this).apply {
            text = "Start virtual media player"
            setOnClickListener {
                val i = Intent(this@MainActivity, MediaBridgeService::class.java)
                    .setAction(MediaBridgeService.ACTION_START)
                ContextCompat.startForegroundService(this@MainActivity, i)
                appendLog("Started virtual media session")
            }
        })
        root.addView(Button(this).apply {
            text = "Stop diagnostic session"
            setOnClickListener {
                stopService(Intent(this@MainActivity, MediaBridgeService::class.java))
                appendLog("Stopped diagnostic service")
            }
        })
        root.addView(Button(this).apply {
            text = "Local test: PLAY"
            setOnClickListener { appendLog("LOCAL TEST: PLAY pressed (not a Bluetooth event)") }
        })
        root.addView(Button(this).apply {
            text = "Local test: PAUSE"
            setOnClickListener { appendLog("LOCAL TEST: PAUSE pressed (not a Bluetooth event)") }
        })
        root.addView(Button(this).apply {
            text = "Clear log"
            setOnClickListener { logView.text = "Waiting for Bluetooth media events…" }
        })
        logView = TextView(this).apply {
            textSize = 14f
            text = "Waiting for Bluetooth media events…"
            setPadding(0, 16, 0, 0)
        }
        root.addView(ScrollView(this).apply { addView(logView) },
            LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(MediaBridgeService.ACTION_EVENT)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(eventReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(eventReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        try { unregisterReceiver(eventReceiver) } catch (_: Exception) {}
    }

    private fun appendLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        runOnUiThread { logView.append("\n[$time] $message") }
    }
}
