package com.toddlerlock.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class TouchBlockService : AccessibilityService() {

  private val windowManager: WindowManager by lazy {
    getSystemService(Context.WINDOW_SERVICE) as WindowManager
  }
  private val mainHandler = Handler(Looper.getMainLooper())
  private val shortcutController = VolumeShortcutController()
  private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  private var overlayView: View? = null
  private var keyHoldJob: Job? = null

  enum class BankingSafeModeResult {
    AccessibilityServiceDisabled,
    ServiceUnavailable,
  }

  companion object {
    val isServiceRunning = MutableStateFlow(false)
    val isShortcutArmed = MutableStateFlow(false)
    val isLocked = MutableStateFlow(false)

    var instance: TouchBlockService? = null
      private set

    fun setShortcutArmed(enabled: Boolean) {
      isShortcutArmed.value = enabled
      if (!enabled) {
        instance?.disarmAndUnlock()
      }
    }

    fun enterBankingSafeMode(): BankingSafeModeResult {
      isShortcutArmed.value = false
      val service = instance ?: return BankingSafeModeResult.ServiceUnavailable

      service.disableForBankingSafeMode()
      return BankingSafeModeResult.AccessibilityServiceDisabled
    }
  }

  override fun onServiceConnected() {
    super.onServiceConnected()
    instance = this
    isServiceRunning.value = true

    serviceInfo =
      serviceInfo.apply {
        flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
      }
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

  override fun onInterrupt() = Unit

  private fun disarmAndUnlock() {
    keyHoldJob?.cancel()
    keyHoldJob = null
    shortcutController.reset()
    unlockScreen()
  }

  private fun disableForBankingSafeMode() {
    disarmAndUnlock()
    disableSelf()
  }

  private fun toggleLock() {
    if (isLocked.value) {
      unlockScreen()
    } else {
      lockScreen()
    }
  }

  private fun lockScreen() {
    if (isLocked.value) return

    mainHandler.post {
      try {
        if (overlayView == null) {
          val styledContext = android.view.ContextThemeWrapper(this, R.style.Theme_MyApplication)
          val frameLayout =
            FrameLayout(styledContext).apply {
              setOnTouchListener { _, _ -> true }
              setBackgroundColor(Color.argb(2, 0, 0, 0))
            }

          val displayMetrics = resources.displayMetrics
          val dpToPx = { dp: Int -> (dp * displayMetrics.density).toInt() }

          val textView =
            android.widget.TextView(styledContext).apply {
              text = "ToddlerLock active"
              setTextColor(Color.WHITE)
              gravity = Gravity.CENTER
              textSize = 12f
              setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
              background =
                android.graphics.drawable.GradientDrawable().apply {
                  setColor(Color.parseColor("#E02D302E"))
                  cornerRadius = dpToPx(14).toFloat()
                }
            }

          val textParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
              )
              .apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                topMargin = dpToPx(24)
                leftMargin = dpToPx(24)
                rightMargin = dpToPx(24)
              }

          frameLayout.addView(textView, textParams)

          val params =
            WindowManager.LayoutParams().apply {
              width = WindowManager.LayoutParams.MATCH_PARENT
              height = WindowManager.LayoutParams.MATCH_PARENT
              type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
              flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                  WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                  WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
              format = PixelFormat.TRANSLUCENT
              gravity = Gravity.CENTER
            }

          windowManager.addView(frameLayout, params)
          overlayView = frameLayout
        }
        isLocked.value = true
        Toast.makeText(applicationContext, "ToddlerLock active", Toast.LENGTH_SHORT).show()
      } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            applicationContext,
            "Failed to start lock. Check Display over other apps permission.",
            Toast.LENGTH_LONG,
          )
          .show()
      }
    }
  }

  private fun unlockScreen() {
    if (!isLocked.value) return

    mainHandler.post {
      try {
        overlayView?.let {
          windowManager.removeViewImmediate(it)
          overlayView = null
        }
        isLocked.value = false
        Toast.makeText(applicationContext, "ToddlerLock inactive", Toast.LENGTH_SHORT).show()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  override fun onKeyEvent(event: KeyEvent): Boolean {
    val decision =
      shortcutController.onKeyEvent(
        keyCode = event.keyCode,
        action = event.action,
        shortcutArmed = isShortcutArmed.value,
        overlayLocked = isLocked.value,
      )

    if (decision.cancelHold) {
      keyHoldJob?.cancel()
      keyHoldJob = null
    }

    if (decision.startHold && keyHoldJob == null) {
      keyHoldJob =
        serviceScope.launch {
          delay(3000)
          if (isShortcutArmed.value) {
            toggleLock()
          }
          shortcutController.markHoldCompleted()
          keyHoldJob = null
        }
    }

    return if (decision.consume) true else super.onKeyEvent(event)
  }

  override fun onDestroy() {
    disarmAndUnlock()
    isShortcutArmed.value = false
    isServiceRunning.value = false
    instance = null
    serviceScope.cancel()
    super.onDestroy()
  }
}
