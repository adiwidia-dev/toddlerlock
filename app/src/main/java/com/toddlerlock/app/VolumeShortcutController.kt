package com.toddlerlock.app

import android.view.KeyEvent

data class VolumeShortcutDecision(
  val consume: Boolean = false,
  val startHold: Boolean = false,
  val cancelHold: Boolean = false,
)

class VolumeShortcutController {
  private var isVolUpPressed = false
  private var isVolDownPressed = false
  private var isHoldPending = false

  fun onKeyEvent(
    keyCode: Int,
    action: Int,
    shortcutArmed: Boolean,
    overlayLocked: Boolean,
  ): VolumeShortcutDecision {
    if (!shortcutArmed) {
      val shouldCancel = isHoldPending
      reset()
      return VolumeShortcutDecision(cancelHold = shouldCancel)
    }

    if (!isVolumeKey(keyCode)) {
      return VolumeShortcutDecision(consume = overlayLocked && keyCode == KeyEvent.KEYCODE_BACK)
    }

    when (action) {
      KeyEvent.ACTION_DOWN -> setPressed(keyCode, true)
      KeyEvent.ACTION_UP -> setPressed(keyCode, false)
      else -> return VolumeShortcutDecision(consume = overlayLocked)
    }

    val bothPressed = isVolUpPressed && isVolDownPressed
    if (bothPressed && !isHoldPending) {
      isHoldPending = true
      return VolumeShortcutDecision(consume = true, startHold = true)
    }

    if (!bothPressed && isHoldPending) {
      isHoldPending = false
      return VolumeShortcutDecision(consume = overlayLocked, cancelHold = true)
    }

    return VolumeShortcutDecision(consume = bothPressed)
  }

  fun markHoldCompleted() {
    reset()
  }

  fun reset() {
    isVolUpPressed = false
    isVolDownPressed = false
    isHoldPending = false
  }

  private fun isVolumeKey(keyCode: Int): Boolean =
    keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN

  private fun setPressed(keyCode: Int, pressed: Boolean) {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      isVolUpPressed = pressed
    } else {
      isVolDownPressed = pressed
    }
  }
}
