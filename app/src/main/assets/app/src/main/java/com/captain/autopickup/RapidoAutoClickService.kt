package com.captain.autopickup

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.regex.Pattern

class RapidoAutoClickService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val prefs = getSharedPreferences("CaptainPrefs", Context.MODE_PRIVATE)
        val isOnDuty = prefs.getBoolean("isOnDuty", false)
        if (!isOnDuty) return

        val maxPickup = prefs.getFloat("maxPickup", 2.0f)
        val minDrop = prefs.getFloat("minDrop", 2.0f)
        val maxDrop = prefs.getFloat("maxDrop", 15.0f)

        val rootNode = rootInActiveWindow ?: return
        val kmsFound = ArrayList<Float>()
        extractKilometers(rootNode, kmsFound)

        if (kmsFound.size >= 2) {
            val pickupKm = kmsFound[0]
            val dropKm = kmsFound[1]

            if (pickupKm <= maxPickup && dropKm in minDrop..maxDrop) {
                clickAcceptButton(rootNode)
            }
        }
    }

    private fun extractKilometers(node: AccessibilityNodeInfo?, list: ArrayList<Float>) {
        if (node == null) return
        val text = node.text?.toString() ?: ""
        val matcher = Pattern.compile("(\\d+(\\.\\d+)?)\\s*(km|KM|Km)").matcher(text)
        while (matcher.find()) {
            matcher.group(1)?.toFloatOrNull()?.let { list.add(it) }
        }
        for (i in 0 until node.childCount) {
            extractKilometers(node.getChild(i), list)
        }
    }

    private fun clickAcceptButton(root: AccessibilityNodeInfo) {
        val targets = listOf("Accept", "ACCEPT", "Accept Ride", "ACCEPT RIDE")
        for (word in targets) {
            val nodes = root.findAccessibilityNodeInfosByText(word)
            for (n in nodes) {
                if (n.isClickable) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return
                } else if (n.parent != null && n.parent.isClickable) {
                    n.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return
                }
            }
        }
    }

    override fun onInterrupt() {}
}
