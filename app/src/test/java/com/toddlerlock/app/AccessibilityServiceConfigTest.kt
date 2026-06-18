package com.toddlerlock.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityServiceConfigTest {

  @Test
  fun accessibilityConfigOnlyRequestsKeyFilteringScope() {
    val config = File("src/main/res/xml/accessibility_service_config.xml").readText()

    assertTrue(config.contains("flagRequestFilterKeyEvents"))
    assertTrue(config.contains("canRequestFilterKeyEvents=\"true\""))
    assertFalse(config.contains("typeAllMask"))
    assertFalse(config.contains("flagRetrieveInteractiveWindows"))
    assertFalse(config.contains("flagDefault"))
  }
}
