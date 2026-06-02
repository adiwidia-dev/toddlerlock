package com.toddlerlock.app

import android.view.KeyEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VolumeShortcutControllerTest {

  @Test
  fun disabledShortcutIgnoresVolumeCombo() {
    val controller = VolumeShortcutController()

    val upDecision =
      controller.onKeyEvent(
        keyCode = KeyEvent.KEYCODE_VOLUME_UP,
        action = KeyEvent.ACTION_DOWN,
        shortcutArmed = false,
        overlayLocked = false,
      )
    val downDecision =
      controller.onKeyEvent(
        keyCode = KeyEvent.KEYCODE_VOLUME_DOWN,
        action = KeyEvent.ACTION_DOWN,
        shortcutArmed = false,
        overlayLocked = false,
      )

    assertFalse(upDecision.consume)
    assertFalse(upDecision.startHold)
    assertFalse(downDecision.consume)
    assertFalse(downDecision.startHold)
  }

  @Test
  fun armedShortcutStartsHoldWhenBothVolumeKeysArePressed() {
    val controller = VolumeShortcutController()

    controller.onKeyEvent(
      keyCode = KeyEvent.KEYCODE_VOLUME_UP,
      action = KeyEvent.ACTION_DOWN,
      shortcutArmed = true,
      overlayLocked = false,
    )
    val decision =
      controller.onKeyEvent(
        keyCode = KeyEvent.KEYCODE_VOLUME_DOWN,
        action = KeyEvent.ACTION_DOWN,
        shortcutArmed = true,
        overlayLocked = false,
      )

    assertTrue(decision.consume)
    assertTrue(decision.startHold)
  }

  @Test
  fun releasingEitherVolumeKeyCancelsPendingHold() {
    val controller = VolumeShortcutController()

    controller.onKeyEvent(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.ACTION_DOWN, true, false)
    controller.onKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.ACTION_DOWN, true, false)
    val decision =
      controller.onKeyEvent(
        keyCode = KeyEvent.KEYCODE_VOLUME_UP,
        action = KeyEvent.ACTION_UP,
        shortcutArmed = true,
        overlayLocked = false,
      )

    assertTrue(decision.cancelHold)
  }

  @Test
  fun lockedOverlayConsumesBackAndVolumeKeys() {
    val controller = VolumeShortcutController()

    val backDecision =
      controller.onKeyEvent(
        keyCode = KeyEvent.KEYCODE_BACK,
        action = KeyEvent.ACTION_DOWN,
        shortcutArmed = true,
        overlayLocked = true,
      )
    val volumeDecision =
      controller.onKeyEvent(
        keyCode = KeyEvent.KEYCODE_VOLUME_UP,
        action = KeyEvent.ACTION_DOWN,
        shortcutArmed = true,
        overlayLocked = true,
      )

    assertTrue(backDecision.consume)
    assertTrue(volumeDecision.consume)
  }
}
