package com.toddlerlock.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toddlerlock.app.ui.theme.Charcoal
import com.toddlerlock.app.ui.theme.LightBorder
import com.toddlerlock.app.ui.theme.MyApplicationTheme
import com.toddlerlock.app.ui.theme.PaleGreenBg
import com.toddlerlock.app.ui.theme.RedWarningBg
import com.toddlerlock.app.ui.theme.RedWarningBorder
import com.toddlerlock.app.ui.theme.RedWarningIcon
import com.toddlerlock.app.ui.theme.RedWarningText
import com.toddlerlock.app.ui.theme.SageGreen
import com.toddlerlock.app.ui.theme.WarmBackground

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize(), containerColor = WarmBackground) { innerPadding ->
          ToddlerLockDashboard(
            modifier =
              Modifier
                .fillMaxSize()
                .padding(innerPadding),
          )
        }
      }
    }
  }
}

@Composable
fun ToddlerLockDashboard(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var isOverlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
  var isAccessibilityEnabled by remember { mutableStateOf(checkAccessibilityPermission(context)) }

  val isShortcutArmed by TouchBlockService.isShortcutArmed.collectAsStateWithLifecycle()
  val isLocked by TouchBlockService.isLocked.collectAsStateWithLifecycle()

  fun refreshPermissions() {
    isOverlayGranted = Settings.canDrawOverlays(context)
    isAccessibilityEnabled = checkAccessibilityPermission(context)
  }

  LaunchedEffect(isOverlayGranted, isAccessibilityEnabled) {
    if ((!isOverlayGranted || !isAccessibilityEnabled) && isShortcutArmed) {
      TouchBlockService.setShortcutArmed(false)
    }
  }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        refreshPermissions()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  Column(
    modifier =
      modifier
        .background(WarmBackground)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 22.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    Header()

    ShortcutPanel(
      isLocked = isLocked,
      isShortcutArmed = isShortcutArmed,
      onToggle = { enabled ->
        if (!isOverlayGranted || !isAccessibilityEnabled) {
          Toast.makeText(context, "Enable both permissions first.", Toast.LENGTH_LONG).show()
        } else {
          TouchBlockService.setShortcutArmed(enabled)
        }
      },
    )

    SectionTitle("Permissions")
    PermissionItem(
      title = "Display over other apps",
      description = "Allows the transparent touch-blocking layer.",
      granted = isOverlayGranted,
      onClick = {
        context.startActivity(
          Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
          ),
        )
      },
    )
    PermissionItem(
      title = "Accessibility Service",
      description = "Listens for the Volume Up + Volume Down shortcut.",
      granted = isAccessibilityEnabled,
      onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
    )

    SectionTitle("Banking apps")
    BankingSafeModePanel(
      enabled = isAccessibilityEnabled || isShortcutArmed || isLocked,
      onClick = {
        val result = TouchBlockService.enterBankingSafeMode()
        refreshPermissions()
        val message =
          when (result) {
            TouchBlockService.BankingSafeModeResult.AccessibilityServiceDisabled -> {
              isAccessibilityEnabled = false
              "Banking Safe Mode enabled. Re-enable Accessibility to use ToddlerLock."
            }
            TouchBlockService.BankingSafeModeResult.ServiceUnavailable ->
              "Shortcut disabled. If the bank app still blocks, turn off Accessibility permission."
          }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
      },
    )

    UsageSteps()
    AndroidRestrictedSettingsNotice()
    VersionFooter()
  }
}

@Composable
private fun Header() {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier =
        Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(SageGreen),
      contentAlignment = Alignment.Center,
    ) {
      Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFDFCF8))
    }
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
      Text(text = "ToddlerLock", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Charcoal)
      Text(
        text = "Touch guard for calls and videos",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF6E716B),
      )
    }
  }
}

@Composable
private fun ShortcutPanel(
  isLocked: Boolean,
  isShortcutArmed: Boolean,
  onToggle: (Boolean) -> Unit,
) {
  val statusText =
    when {
      isLocked -> "Blocking touches"
      isShortcutArmed -> "Shortcut armed"
      else -> "Shortcut disabled"
    }
  val helperText =
    when {
      isLocked -> "Hold both volume buttons for 3 seconds to unlock."
      isShortcutArmed -> "Leave this app, start your call or video, then hold both volume buttons."
      else -> "Volume button presses are ignored until this is enabled."
    }
  val statusColor = if (isLocked || isShortcutArmed) SageGreen else Color(0xFF74786F)
  val panelBackground = if (isLocked) PaleGreenBg else Color(0xFFFDFCF8)

  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(panelBackground)
        .border(1.dp, if (isLocked || isShortcutArmed) SageGreen.copy(alpha = 0.45f) else LightBorder, RoundedCornerShape(8.dp))
        .padding(18.dp),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
          StatusDot(color = statusColor)
          Text(
            text = "Shortcut",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor,
          )
        }
        Switch(
          checked = isShortcutArmed,
          enabled = true,
          onCheckedChange = onToggle,
          colors =
            SwitchDefaults.colors(
              checkedThumbColor = Color(0xFFFDFCF8),
              checkedTrackColor = SageGreen,
              checkedBorderColor = SageGreen,
              uncheckedThumbColor = Color(0xFFFDFCF8),
              uncheckedTrackColor = Color(0xFFBFC2BA),
              uncheckedBorderColor = Color(0xFF8D9288),
              disabledCheckedThumbColor = Color(0xFFFDFCF8),
              disabledCheckedTrackColor = SageGreen.copy(alpha = 0.55f),
              disabledUncheckedThumbColor = Color(0xFFFDFCF8),
              disabledUncheckedTrackColor = Color(0xFFBFC2BA),
              disabledUncheckedBorderColor = Color(0xFF8D9288),
            ),
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(text = statusText, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Charcoal)
        Text(
          text = helperText,
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFF5C5E5B),
          lineHeight = 20.sp,
        )
      }
    }
  }
}

@Composable
private fun SectionTitle(text: String) {
  Text(
    text = text,
    fontSize = 12.sp,
    fontWeight = FontWeight.Bold,
    color = Color(0xFF7B7D75),
  )
}

@Composable
private fun PermissionItem(
  title: String,
  description: String,
  granted: Boolean,
  onClick: () -> Unit,
) {
  Card(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick),
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFCF8)),
    border = BorderStroke(1.dp, LightBorder),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (granted) PaleGreenBg else Color(0xFFF0EEE7)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = if (granted) Icons.Default.Check else Icons.Default.Info,
          contentDescription = null,
          tint = if (granted) SageGreen else Color(0xFF74786F),
        )
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = title, fontWeight = FontWeight.SemiBold, color = Charcoal)
        Text(text = description, fontSize = 12.sp, color = Color(0xFF6D6A64), lineHeight = 16.sp)
      }
      Button(onClick = onClick) {
        Text(if (granted) "Open" else "Grant")
      }
    }
  }
}

@Composable
private fun BankingSafeModePanel(
  enabled: Boolean,
  onClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0xFFFFF7E3))
        .border(1.dp, Color(0xFFE5C879), RoundedCornerShape(8.dp))
        .padding(16.dp),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFF8A6400))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(text = "Banking Safe Mode", fontWeight = FontWeight.Bold, color = Charcoal)
          Text(
            text = "Temporarily disables ToddlerLock Accessibility before opening banking apps.",
            fontSize = 12.sp,
            color = Color(0xFF6D5A23),
            lineHeight = 17.sp,
          )
        }
      }
      Button(enabled = enabled, onClick = onClick) {
        Text("Enter Safe Mode")
      }
    }
  }
}

@Composable
private fun UsageSteps() {
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0xFFF3F2EC))
        .border(1.dp, LightBorder, RoundedCornerShape(8.dp))
        .padding(16.dp),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      SectionTitle("How to use")
      UsageLine("1", "Enable the shortcut here.")
      UsageLine("2", "Open WhatsApp, YouTube, or your call app.")
      UsageLine("3", "Hold Volume Up + Volume Down for 3 seconds to lock or unlock.")
    }
  }
}

@Composable
private fun UsageLine(number: String, text: String) {
  Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
    Box(
      modifier =
        Modifier
          .size(22.dp)
          .clip(CircleShape)
          .background(Color(0xFFE3E7DD)),
      contentAlignment = Alignment.Center,
    ) {
      Text(text = number, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Charcoal)
    }
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Charcoal, lineHeight = 19.sp)
  }
}

@Composable
private fun AndroidRestrictedSettingsNotice() {
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(RedWarningBg)
        .border(1.dp, RedWarningBorder, RoundedCornerShape(8.dp))
        .padding(16.dp),
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
      Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = RedWarningIcon)
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Android Restricted Settings",
          fontWeight = FontWeight.Bold,
          color = RedWarningText,
        )
        Text(
          text =
            "If Accessibility is greyed out after sideloading: open App info, tap the 3-dot menu, choose Allow restricted settings, confirm, then return here.",
          fontSize = 12.sp,
          color = RedWarningText,
          lineHeight = 17.sp,
        )
      }
    }
  }
}

@Composable
private fun VersionFooter() {
  Text(
    text = "Version ${BuildConfig.VERSION_NAME}",
    modifier = Modifier.fillMaxWidth(),
    color = Color(0xFF8A8D84),
    fontSize = 12.sp,
    textAlign = TextAlign.Center,
  )
}

@Composable
private fun StatusDot(color: Color) {
  Box(
    modifier =
      Modifier
        .size(10.dp)
        .clip(CircleShape)
        .background(color),
  )
}

private fun checkAccessibilityPermission(context: Context): Boolean {
  val expectedId = "${context.packageName}/${TouchBlockService::class.java.canonicalName}"
  val enabledSettings =
    Settings.Secure.getString(
      context.contentResolver,
      Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false

  val splitter = TextUtils.SimpleStringSplitter(':')
  splitter.setString(enabledSettings)
  while (splitter.hasNext()) {
    if (splitter.next().equals(expectedId, ignoreCase = true)) {
      return true
    }
  }
  return false
}
