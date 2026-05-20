package com.example

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.Toast
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class TouchBlockService : AccessibilityService() {

    private val windowManager: WindowManager by lazy {
        getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private var overlayView: View? = null

    private var isVolUpPressed = false
    private var isVolDownPressed = false
    private var keyHoldJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        val isServiceRunning = MutableStateFlow(false)
        val isLocked = MutableStateFlow(false)
        
        var instance: TouchBlockService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isServiceRunning.value = true
        
        // Re-apply flags if needed, although configuration xml specifies them
        serviceInfo = serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used
    }

    override fun onInterrupt() {
        // Not used
    }

    fun toggleLock() {
        if (isLocked.value) {
            unlockScreen()
        } else {
            lockScreen()
        }
    }

    private fun lockScreen() {
        if (isLocked.value) return

        Handler(Looper.getMainLooper()).post {
            try {
                if (overlayView == null) {
                    val styledContext = android.view.ContextThemeWrapper(this, com.example.R.style.Theme_MyApplication)
                    val frameLayout = FrameLayout(styledContext).apply {
                        // Block and swallow all touch events on this full-screen window
                        setOnTouchListener { _, event ->
                            true
                        }
                        // Apply a tiny 1% dark tint to intercept touchscreen input buffer efficiently
                        setBackgroundColor(Color.argb(2, 0, 0, 0))
                    }

                    // Create a beautiful, semi-transparent banner explaining the active status
                    val displayMetrics = resources.displayMetrics
                    val dpToPx = { dp: Int -> (dp * displayMetrics.density).toInt() }

                    val textView = android.widget.TextView(styledContext).apply {
                        text = "🔒 ToddlerLock is active\nHold Vol Up + Vol Down for 3s to unlock"
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        textSize = 14f
                        setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
                        
                        // Use programmatical rounded card background
                        background = android.graphics.drawable.GradientDrawable().apply {
                            setColor(Color.parseColor("#E02D302E")) // Charcoal with 88% opacity
                            cornerRadius = dpToPx(16).toFloat()
                        }
                    }

                    val textParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                        topMargin = dpToPx(80) // Graceful space below most front camera notches
                        leftMargin = dpToPx(24)
                        rightMargin = dpToPx(24)
                    }

                    frameLayout.addView(textView, textParams)

                    val params = WindowManager.LayoutParams().apply {
                        width = WindowManager.LayoutParams.MATCH_PARENT
                        height = WindowManager.LayoutParams.MATCH_PARENT
                        type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        format = PixelFormat.TRANSLUCENT
                        gravity = Gravity.CENTER
                    }

                    windowManager.addView(frameLayout, params)
                    overlayView = frameLayout
                }
                isLocked.value = true
                Toast.makeText(applicationContext, "ToddlerLock Active!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Failed to start Lock: Overlay permission may be missing", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun unlockScreen() {
        if (!isLocked.value) return

        Handler(Looper.getMainLooper()).post {
            try {
                overlayView?.let {
                    windowManager.removeViewImmediate(it)
                    overlayView = null
                }
                isLocked.value = false
                Toast.makeText(applicationContext, "Screen Unlocked!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            val isDown = action == KeyEvent.ACTION_DOWN
            
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                isVolUpPressed = isDown
            } else {
                isVolDownPressed = isDown
            }

            if (isVolUpPressed && isVolDownPressed) {
                if (keyHoldJob == null) {
                    // Vol Up + Vol Down are held: wait 3 seconds
                    keyHoldJob = serviceScope.launch {
                        delay(3000)
                        toggleLock()
                        isVolUpPressed = false
                        isVolDownPressed = false
                    }
                }
            } else {
                // Anyone released, cancel countdown
                keyHoldJob?.cancel()
                keyHoldJob = null
            }

            // Consume volume keys iflocked or during interactive toggle countdown
            if (isLocked.value || (isVolUpPressed && isVolDownPressed)) {
                return true
            }
        }

        // Consume other mechanical keys (like Back click) while locked
        if (isLocked.value) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return true
            }
        }

        return super.onKeyEvent(event)
    }

    override fun onDestroy() {
        unlockScreen()
        isServiceRunning.value = false
        instance = null
        keyHoldJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
}
