package com.example.childapp

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.util.Patterns
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat


class ActiveAppWatcher : AccessibilityService() {

    var browserApp = ""
    var browserUrl = ""
    var isForegroundServiceStated = false


    companion object {
        var context: Context? = null
        private const val CHANNEL_ID = "ForegroundServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()
        context = baseContext
    }

    private fun initService() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        filter.addAction(Intent.ACTION_SHUTDOWN)
        filter.addAction(Intent.ACTION_DREAMING_STARTED)
        registerReceiver(EventReceiver(), filter)
        runnable.run()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if (!isForegroundServiceStated) {
            initService()
            isForegroundServiceStated = true
        }
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val runningPackName: String = event.packageName.toString()
                if (runningPackName == "com.android.settings" || runningPackName == "com.miui.securitycenter") {
                    blockSetting()
                } else if (isUserLimited()) {
                    blockUser(runningPackName)
                }
            }
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {

            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {

            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val runningPackName: String = event.packageName.toString()
                val parentNodeInfo = event.source ?: return
                var browserConfig: SupportBrowserConfig? = null
                for (supportedConfig in getSupportedBrowsers()) {
                    if (supportedConfig.packageName == runningPackName) {
                        browserConfig = supportedConfig
                    }
                }
                //this is not supported browser, so exit
                //this is not supported browser, so exit
                if (browserConfig == null) {
                    return
                }

                val capturedUrl: String = captureUrl(parentNodeInfo, browserConfig)
                parentNodeInfo.recycle()

                val eventTime = event.eventTime
                if (!packageName.equals(browserApp)) {
                    if (Patterns.WEB_URL.matcher(capturedUrl).matches()) {
                        browserApp = packageName
                        browserUrl = capturedUrl
                    }
                } else {
                    if (capturedUrl != browserUrl) {
                        if (Patterns.WEB_URL.matcher(capturedUrl).matches()) {
                            browserUrl = capturedUrl
                        }
                    }
                }
            }
        }
    }

    override fun onInterrupt() {

    }

    private fun isUserLimited(): Boolean {
        val pref = context?.getSharedPreferences("ChildApp", 0)
        val currentMinutes = pref?.getLong("onDuration", 0) ?: 0
        val limitedTime = pref?.getInt("limited_time", 10) ?: 0
        return currentMinutes >= limitedTime
    }

    private fun blockSetting() {
        val pref = applicationContext.getSharedPreferences("ChildApp", 0)
        val isSettingBlocked = pref.getBoolean("isSettingBlocked", false)
        if (isSettingBlocked) {
            startActivity(
                Intent(
                    applicationContext,
                    BlockActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private fun blockUser(packageName: String) {
        if (packageName == "com.example.childapp" || packageName == "com.google.android.inputmethod.latin" || isSystemApp(
                packageName
            )
        ) {
            return
        }
        startActivity(
            Intent(
                applicationContext,
                BlockActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val pm: PackageManager = context!!.packageManager
            val list = pm.getInstalledPackages(0)

            for (pi in list) {
                if (pi.packageName == packageName) {
                    val ai = pm.getApplicationInfo(pi.packageName, 0)
                    if (ai.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                        return true
                    }
                }
            }
            false
        } catch (e: PackageManager.NameNotFoundException) {
            true
        }
    }

    private fun captureUrl(info: AccessibilityNodeInfo, config: SupportBrowserConfig): String {
        val nodes = info.findAccessibilityNodeInfosByViewId(config.addressId)
        if (nodes == null || nodes.size <= 0) {
            return ""
        }
        val addressBarNodeInfo = nodes[0]
        var url: String? = null
        if (addressBarNodeInfo.text != null) {
            url = addressBarNodeInfo.text.toString()
        }
        addressBarNodeInfo.recycle()
        return url ?: ""
    }

    private fun getSupportedBrowsers(): List<SupportBrowserConfig> = listOf(
        SupportBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar"),
        SupportBrowserConfig(
            "org.mozilla.firefox",
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view"
        ),
        SupportBrowserConfig("com.opera.browser", "com.opera.browser:id/url_field"),
        SupportBrowserConfig(
            "com.opera.mini.native",
            "com.opera.mini.native:id/url_field"
        ),
        SupportBrowserConfig(
            "com.duckduckgo.mobile.android",
            "com.duckduckgo.mobile.android:id/omnibarTextInput"
        ),
        SupportBrowserConfig("com.microsoft.emmx", "com.microsoft.emmx:id/url_bar")
    )

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun startForegroundService() {
        createNotificationChannel()
        val notificationIntent = Intent(this, PasswordActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("مراقب کودکان!")
            .setContentText("سامانه کنترل کودکان")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(1, notification)
        }
    }
}

data class SupportBrowserConfig(val packageName: String, val addressId: String)

private val handler: Handler = Handler()
var isFirstRun = true

var runnable: Runnable = object : Runnable {
    override fun run() {
        ActiveAppWatcher.context?.let {
            saveCurrentOnTime(it)
        }
        handler.postDelayed(this, 60_000)
    }

    private fun saveCurrentOnTime(context: Context) {
        if (isFirstRun) {
            isFirstRun = false
            return
        }
        val pref = context.getSharedPreferences("ChildApp", 0)
        val currentMinutes = pref.getLong("onDuration", 0)
        val editor = pref.edit()
        editor.putLong("onDuration", currentMinutes + 1)
        editor.apply()
    }
}

class EventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        intent?.action?.let {
            when (it) {
                Intent.ACTION_SCREEN_ON -> {
                    runnable.run()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    isFirstRun = true
                    handler.removeCallbacks(runnable)
                }
                else -> {}
            }

        }
    }
}