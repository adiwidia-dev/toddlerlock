package com.toddlerlock.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ToddlerLock", appName)
  }

  @Test
  fun `creates TouchBlockService`() {
    val controller = Robolectric.buildService(TouchBlockService::class.java)
    val service = controller.create().get()
    assertNotNull(service)

    service.onDestroy()
  }

  @Test
  fun `banking safe mode disarms shortcut even when service is unavailable`() {
    TouchBlockService.setShortcutArmed(true)

    val result = TouchBlockService.enterBankingSafeMode()

    assertEquals(TouchBlockService.BankingSafeModeResult.ServiceUnavailable, result)
    assertFalse(TouchBlockService.isShortcutArmed.value)
  }
}
