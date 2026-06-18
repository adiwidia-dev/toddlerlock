package com.toddlerlock.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AppVersionTest {

  @Test
  fun appVersionMatchesReleaseTarget() {
    assertEquals("1.2.0", BuildConfig.VERSION_NAME)
    assertEquals(3, BuildConfig.VERSION_CODE)
  }
}
