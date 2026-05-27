package com.ninja.nova.services

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log

object PhoneActionService {

    // Bluetooth
    fun setBluetoothOn(context: Context, on: Boolean) {
        try {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bm.adapter
            if (on) adapter?.enable() else adapter?.disable()
        } catch (e: Exception) { Log.e("PhoneAction", "Bluetooth error: ${e.message}") }
    }

    // WiFi
    fun setWifiOn(context: Context, on: Boolean) {
        try {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wm.isWifiEnabled = on
        } catch (e: Exception) { Log.e("PhoneAction", "WiFi error: ${e.message}") }
    }

    // Flashlight
    fun setFlashlight(context: Context, on: Boolean) {
        try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cm.cameraIdList[0]
            cm.setTorchMode(cameraId, on)
        } catch (e: Exception) { Log.e("PhoneAction", "Flashlight error: ${e.message}") }
    }

    // Volume
    fun setVolume(context: Context, level: Int) {
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val vol = (level / 100.0 * max).toInt()
            am.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0)
        } catch (e: Exception) { Log.e("PhoneAction", "Volume error: ${e.message}") }
    }

    // Open any app by package name
    fun openApp(context: Context, packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) { Log.e("PhoneAction", "Open app error: ${e.message}") }
    }

    // Open Spotify with song
    fun openSpotify(context: Context, query: String = "") {
        try {
            val intent = if (query.isNotEmpty()) {
                Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:$query")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                context.packageManager.getLaunchIntentForPackage("com.spotify.music")?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            intent?.let { context.startActivity(it) }
        } catch (e: Exception) {
            // Spotify not installed - open Play Store
            openUrl(context, "https://play.google.com/store/apps/details?id=com.spotify.music")
        }
    }

    // Open YouTube
    fun openYouTube(context: Context, query: String = "") {
        try {
            val url = if (query.isNotEmpty()) "https://www.youtube.com/results?search_query=${Uri.encode(query)}"
                      else "https://www.youtube.com"
            openUrl(context, url)
        } catch (e: Exception) { Log.e("PhoneAction", "YouTube error: ${e.message}") }
    }

    // Make a call
    fun makeCall(context: Context, number: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) { Log.e("PhoneAction", "Call error: ${e.message}") }
    }

    // Send WhatsApp message
    fun sendWhatsApp(context: Context, number: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$number?text=${Uri.encode(message)}")
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openUrl(context, "https://wa.me/$number?text=${Uri.encode(message)}")
        }
    }

    // Open URL in browser
    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) { Log.e("PhoneAction", "URL error: ${e.message}") }
    }

    // Open Settings
    fun openSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) { Log.e("PhoneAction", "Settings error: ${e.message}") }
    }

    // Common app packages
    val APP_PACKAGES = mapOf(
        "spotify" to "com.spotify.music",
        "youtube" to "com.google.android.youtube",
        "whatsapp" to "com.whatsapp",
        "instagram" to "com.instagram.android",
        "telegram" to "org.telegram.messenger",
        "chrome" to "com.android.chrome",
        "gmail" to "com.google.android.gm",
        "maps" to "com.google.android.apps.maps",
        "camera" to "com.android.camera2",
        "calculator" to "com.android.calculator2",
        "calendar" to "com.google.android.calendar",
        "clock" to "com.android.deskclock",
        "contacts" to "com.android.contacts",
        "files" to "com.android.documentsui",
        "play store" to "com.android.vending",
        "settings" to "com.android.settings"
    )
}
