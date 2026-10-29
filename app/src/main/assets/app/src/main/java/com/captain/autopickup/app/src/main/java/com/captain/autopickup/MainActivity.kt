package com.captain.autopickup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.addJavascriptInterface(AndroidBridge(this), "AndroidBridge")
        webView.loadUrl("file:///android_asset/captain_auto_pickup.html")
        setContentView(webView)
    }

    class AndroidBridge(private val context: Context) {
        @JavascriptInterface
        fun saveFilterSettings(maxPickup: Float, minDrop: Float, maxDrop: Float, isOnDuty: Boolean) {
            val prefs = context.getSharedPreferences("CaptainPrefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putFloat("maxPickup", maxPickup)
                .putFloat("minDrop", minDrop)
                .putFloat("maxDrop", maxDrop)
                .putBoolean("isOnDuty", isOnDuty)
                .apply()
        }

        @JavascriptInterface
        fun openAccessibilitySettings() {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        @JavascriptInterface
        fun isAccessibilityEnabled(): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabledServices.contains(context.packageName)
        }
    }
}
