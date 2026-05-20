package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ToddlerLock", appName)
  }

  @Test
  fun `test TouchBlockService overlay creation`() {
    val controller = Robolectric.buildService(TouchBlockService::class.java)
    val service = controller.create().get()
    assertNotNull(service)
    
    // Attempt toggle locking
    service.toggleLock()
    
    // Cleanup
    service.onDestroy()
  }
}
