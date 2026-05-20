package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = WarmBackground
                ) { innerPadding ->
                    ToddlerLockDashboard(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
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

    var isOverlayGranted by remember { mutableStateOf(false) }
    var isAccessibilityEnabled by remember { mutableStateOf(false) }

    // Read the flows from our TouchBlockService
    val isServiceRunning by TouchBlockService.isServiceRunning.collectAsStateWithLifecycle()
    val isLocked by TouchBlockService.isLocked.collectAsStateWithLifecycle()

    var testLockCountdown by remember { mutableStateOf(0) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    // Native support for design HTML navigation tabs: 0 = Home, 1 = Logs, 2 = Alerts/Troubleshoot
    var activeTab by remember { mutableStateOf(0) }

    // Sample logs trace to make the "Logs" tab highly functional and immersive!
    val activationLogs = remember {
        mutableStateListOf(
            "Service bound successfully to system accessibility pipeline.",
            "TouchBlock overlay listener initialized."
        )
    }

    // Keep state of lock activations
    LaunchedEffect(isLocked) {
        if (isLocked) {
            activationLogs.add(0, "[LOCK] Engagement activated via hardware key short trigger.")
        } else {
            if (activationLogs.isNotEmpty() && activationLogs.first().startsWith("[LOCK]")) {
                activationLogs.add(0, "[UNLOCK] Disengaged via keys / manual touch toggle.")
            }
        }
    }

    // Periodically checks permission states on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isOverlayGranted = Settings.canDrawOverlays(context)
                isAccessibilityEnabled = checkAccessibilityPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmBackground)
    ) {
        // HEADER row styling matching HTML: Alabaster header text, sage accent box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sage Green Shield Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SageGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "ToddlerLock Emblem",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "ToddlerLock",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    letterSpacing = (-0.5).sp
                )
            }

            // Circular setting button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(WarmClay)
                    .clickable {
                        Toast.makeText(context, "ToddlerLock is ready globally!", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "App Settings Info",
                    tint = Charcoal,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // SCROLLABLE INTERACTIVE VIEW CONTENT
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                0 -> { // HOME VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Main Protection Status Card with warm sand styling
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(32.dp))
                                .background(BiscuitBeige)
                                .border(1.dp, BiscuitBorder, RoundedCornerShape(32.dp))
                                .padding(24.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Protection Status",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SageGreen,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = if (isLocked) "Service Active" else "Ready / Idle",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Charcoal
                                        )
                                    }

                                    // Custom Rounded Switch Knob representing HTML layout toggle
                                    Box(
                                        modifier = Modifier
                                            .width(48.dp)
                                            .height(26.dp)
                                            .clip(CircleShape)
                                            .background(if (isLocked) SageGreen else Color(0xFFCBC9BF))
                                            .clickable {
                                                if (!isAccessibilityEnabled || !isOverlayGranted) {
                                                    Toast.makeText(context, "Please enable display and accessibility permissions first!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    TouchBlockService.instance?.toggleLock()
                                                }
                                            }
                                            .padding(2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .align(if (isLocked) Alignment.CenterEnd else Alignment.CenterStart)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }

                                Text(
                                    text = "The screen touch block will engage immediately when you hold Volume Up + Volume Down keys for 3 seconds.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF5C5E5B),
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        // System Permissions header section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "System Permissions",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA5A298),
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            // Display Over Apps Permission Item Box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White)
                                    .border(1.dp, LightBorder, RoundedCornerShape(18.dp))
                                    .clickable {
                                        if (!isOverlayGranted) {
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:${context.packageName}")
                                            )
                                            context.startActivity(intent)
                                        } else {
                                            Toast.makeText(context, "Overlay authorization is granted!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Soft light-green badge container for overlay eye icon
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(PaleGreenBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            tint = DarkGreenText,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Display over other apps",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Charcoal
                                        )
                                        Text(
                                            text = "Required to overlay block touch layer",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8A8881)
                                        )
                                    }
                                }

                                if (isOverlayGranted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Configured Checkmark",
                                        tint = SageGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE58B8B))
                                    )
                                }
                            }

                            // Accessibility Permission Item Box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White)
                                    .border(1.dp, LightBorder, RoundedCornerShape(18.dp))
                                    .clickable {
                                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                        context.startActivity(intent)
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Soft light-yellow clay container for user symbol check
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF8F2EA)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBox,
                                            contentDescription = null,
                                            tint = Color(0xFF7C6345),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Accessibility Service",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Charcoal
                                        )
                                        Text(
                                            text = "Enables hardware volume key shortcuts",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8A8881)
                                        )
                                    }
                                }

                                if (isAccessibilityEnabled) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active Hook Checkmark",
                                        tint = SageGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE58B8B))
                                    )
                                }
                            }
                        }

                        // Test utilities section if helper is active
                        if (isOverlayGranted && isAccessibilityEnabled) {
                            val instance = TouchBlockService.instance
                            if (instance != null) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = { instance.toggleLock() },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isLocked) Color(0xFFE58B8B) else SageGreen
                                            ),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text(if (isLocked) "Stop Lock" else "Instant Start Lock", fontSize = 13.sp)
                                        }

                                        if (testLockCountdown == 0 && !isLocked) {
                                            OutlinedButton(
                                                onClick = {
                                                    testLockCountdown = 5
                                                    Toast.makeText(context, "Switch to YouTube/Call now! Lock triggers in 5s", Toast.LENGTH_LONG).show()
                                                    handler.post(object : Runnable {
                                                        override fun run() {
                                                            if (testLockCountdown > 1) {
                                                                testLockCountdown--
                                                                handler.postDelayed(this, 1000)
                                                            } else {
                                                                testLockCountdown = 0
                                                                instance.toggleLock()
                                                            }
                                                        }
                                                    })
                                                },
                                                modifier = Modifier.weight(1f),
                                                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreen)
                                            ) {
                                                Text("5s Preview", fontSize = 13.sp)
                                            }
                                        }
                                    }

                                    if (testLockCountdown > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(SageGreen.copy(alpha = 0.1f))
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Locking screens in $testLockCountdown seconds...",
                                                color = Charcoal,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Android 13+ warning notice from template styling
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(RedWarningBg)
                                .border(1.dp, RedWarningBorder, RoundedCornerShape(18.dp))
                                .padding(18.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Caution alerts",
                                    tint = RedWarningIcon,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Android 13+ Warning",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RedWarningText
                                    )
                                    Text(
                                        text = "If permissions are greyed out: App info → click the 3 dots in the top right → select \"Allow restricted settings\" to support direct setup.",
                                        fontSize = 12.sp,
                                        color = RedWarningText.copy(alpha = 0.85f),
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> { // SYSTEM TRACE LOGS VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Historical Activity",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )

                        Text(
                            text = "Activity tracking records service operations on key activations locally.",
                            fontSize = 12.sp,
                            color = Color(0xFF8A8881)
                        )

                        HorizontalDivider(color = LightBorder)

                        if (activationLogs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No log records found yet.",
                                    color = Color(0xFFA5A298),
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            activationLogs.forEachIndexed { index, log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, LightBorder, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (log.contains("LOCK")) SageGreen else Color(0xFFCBC9BF))
                                    )
                                    Text(
                                        text = log,
                                        fontSize = 12.sp,
                                        color = Charcoal,
                                        fontWeight = if (log.contains("[LOCK]")) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> { // TROUBLESHOOTING & ALERTS VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Troubleshooting",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )

                        Text(
                            text = "Learn how to bypass system restrictions to setup the overlay lock safely.",
                            fontSize = 12.sp,
                            color = Color(0xFF8A8881)
                        )

                        HorizontalDivider(color = LightBorder)

                        // FAQ items
                        TroubleShootItem(
                            title = "How does locked disengagement function?",
                            desc = "Simply press and hold Volume UP + Volume DOWN keys simultaneously for 3 continuous seconds. Since Android blocks back home clicks, this mechanism is the only secure pipeline designed."
                        )

                        TroubleShootItem(
                            title = "Why does volume toggle change sounds?",
                            desc = "Since ToddlerLock filters key interactions, standard volume changes might occur momentarily during trigger countdowns but are consumed cleanly afterwards."
                        )

                        TroubleShootItem(
                            title = "Restricted settings greyed out?",
                            desc = "Go to device settings → Apps → ToddlerLock. Under the triple-dot menu choice in outer top boundary, press 'Allow restricted settings'. Confirm PIN security lock and reboot the application!"
                        )
                    }
                }
            }
        }

        // BOTTOM SOLID NAVIGATION BAR styled exactly like HTML: Standard outline, responsive buttons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, LightBorder, androidx.compose.ui.graphics.RectangleShape)
                .navigationBarsPadding()
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // HOME Tab button
                Column(
                    modifier = Modifier
                        .clickable { activeTab = 0 }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Dashboard",
                        tint = if (activeTab == 0) SageGreen else Color(0xFFA5A298),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Home",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == 0) SageGreen else Color(0xFFA5A298)
                    )
                }

                // LOGS Tab button
                Column(
                    modifier = Modifier
                        .clickable { activeTab = 1 }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Logs",
                        tint = if (activeTab == 1) SageGreen else Color(0xFFA5A298),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Logs",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == 1) SageGreen else Color(0xFFA5A298)
                    )
                }

                // ALERTS Tab button
                Column(
                    modifier = Modifier
                        .clickable { activeTab = 2 }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = if (activeTab == 2) SageGreen else Color(0xFFA5A298),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Trouble",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == 2) SageGreen else Color(0xFFA5A298)
                    )
                }
            }
        }
    }
}

@Composable
fun TroubleShootItem(title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, LightBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal
        )
        Text(
            text = desc,
            fontSize = 12.sp,
            color = Color(0xFF5C5E5B),
            lineHeight = 17.sp
        )
    }
}

// Helper to split and check active accessibility pipeline bindings
private fun checkAccessibilityPermission(context: Context): Boolean {
    val expectedId = "${context.packageName}/${TouchBlockService::class.java.canonicalName}"
    val enabledSettings = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val splitter = TextUtils.SimpleStringSplitter(':')
    splitter.setString(enabledSettings)
    while (splitter.hasNext()) {
        val service = splitter.next()
        if (service.equals(expectedId, ignoreCase = true)) {
            return true
        }
    }
    return false
}
