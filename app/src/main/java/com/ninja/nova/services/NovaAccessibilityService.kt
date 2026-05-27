package com.ninja.nova.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.*

class NovaAccessibilityService : AccessibilityService() {

    companion object {
        var instance: NovaAccessibilityService? = null
        var isRunning = false
        const val TAG = "NovaAccessibility"

        // Commands from outside
        var pendingCommand: String? = null
        var pendingParams: String? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isRunning = true
        Log.d(TAG, "Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        isRunning = false
        instance = null
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        instance = null
    }

    // Open Chrome and search
    fun searchOnChrome(query: String) {
        CoroutineScope(Dispatchers.Main).launch {
            // Open Chrome
            val intent = packageManager.getLaunchIntentForPackage("com.android.chrome")
                ?: packageManager.getLaunchIntentForPackage("org.mozilla.firefox")
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent?.let { startActivity(it) }
            delay(2000)

            // Find address bar and type
            val root = rootInActiveWindow ?: return@launch
            val addressBar = findNodeByHint(root, "Search or type URL", "Address bar", "Search")
            addressBar?.let {
                it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                delay(500)
                val args = Bundle()
                args.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "https://www.google.com/search?q=${query.replace(" ", "+")}")
                it.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                delay(300)
                // Press Enter
                performGlobalAction(GLOBAL_ACTION_BACK)
                delay(300)
                it.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            }
        }
    }

    // Find node by content description or text
    private fun findNodeByHint(root: AccessibilityNodeInfo, vararg hints: String): AccessibilityNodeInfo? {
        for (hint in hints) {
            val nodes = root.findAccessibilityNodeInfosByText(hint)
            if (nodes.isNotEmpty()) return nodes[0]
        }
        return null
    }

    // Click at coordinates
    fun clickAt(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        dispatchGesture(gesture, null, null)
    }

    // Scroll down
    fun scrollDown() {
        val path = Path().apply {
            moveTo(500f, 1000f)
            lineTo(500f, 300f)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()
        dispatchGesture(gesture, null, null)
    }

    // Get all text from current screen
    fun getScreenText(): String {
        val root = rootInActiveWindow ?: return ""
        val sb = StringBuilder()
        extractText(root, sb)
        return sb.toString()
    }

    private fun extractText(node: AccessibilityNodeInfo, sb: StringBuilder) {
        if (!node.text.isNullOrEmpty()) sb.append(node.text).append(" ")
        if (!node.contentDescription.isNullOrEmpty()) sb.append(node.contentDescription).append(" ")
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { extractText(it, sb) }
        }
    }
}
