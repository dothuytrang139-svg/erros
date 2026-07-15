/*
 * Make by Kiều Lương Quân
 * Main Activity containing a professional cyber-gaming UI to control the Free Fire FOV Aim Overlay.
 */

package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Entity
import com.example.service.OverlayService
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF141218) // Custom Sleek Interface background
                ) { innerPadding ->
                    DashboardScreen(
                        modifier = Modifier.padding(innerPadding),
                        onToggleService = { activate ->
                            if (activate) {
                                if (Settings.canDrawOverlays(this)) {
                                    startOverlayService()
                                } else {
                                    requestOverlayPermission()
                                }
                            } else {
                                stopOverlayService()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_START
        }
        startService(intent)
        Toast.makeText(this, "Đã kích hoạt lớp phủ AotForms!", Toast.LENGTH_SHORT).show()
    }

    private fun stopOverlayService() {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_STOP
        }
        startService(intent)
        Toast.makeText(this, "Đã tắt lớp phủ!", Toast.LENGTH_SHORT).show()
    }

    private fun requestOverlayPermission() {
        Toast.makeText(this, "Vui lòng cấp quyền vẽ lên ứng dụng khác!", Toast.LENGTH_LONG).show()
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }
}

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onToggleService: (Boolean) -> Unit
) {
    val context = LocalContext.current
    
    // Live update triggers
    var tick by remember { mutableStateOf(0) }
    var isOverlayEnabled by remember { mutableStateOf(OverlayConfig.isActivated) }
    var fovSize by remember { mutableStateOf(OverlayConfig.fovRadius) }
    var selectedBone by remember { mutableStateOf(OverlayConfig.targetBone) }
    var isSimulating by remember { mutableStateOf(OverlayConfig.isSimulating) }
    var isAimLocked by remember { mutableStateOf(OverlayConfig.isAimLocked) }
    var simulatedEntity by remember { mutableStateOf(OverlayConfig.simulatedEntity) }
    var hasPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    // Periodic coordinate reader polling (20 fps)
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            tick++
            isOverlayEnabled = OverlayConfig.isActivated
            isAimLocked = OverlayConfig.isAimLocked
            simulatedEntity = OverlayConfig.simulatedEntity
            hasPermission = Settings.canDrawOverlays(context)
            if (!hasPermission && isOverlayEnabled) {
                // If permission revoked while running, auto stop
                OverlayConfig.isActivated = false
                isOverlayEnabled = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. HEADER (Sleek Interface Style) ---
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "AOTFORMS SYSTEM",
                    color = Color(0xFFD0BCFF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Game Assistant",
                    color = Color(0xFFE6E1E5),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp
                )
            }
            
            // Circular initial badge "KQ" for Kiều Lương Quân
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF49454F), CircleShape)
                    .border(1.dp, Color(0xFF938F99), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "KQ",
                    color = Color(0xFFE6E1E5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Permission Warning ---
        if (!hasPermission) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                border = BoxBorder(1.dp, Color(0xFFFF3366)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning Icon",
                            tint = Color(0xFFFF3366),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "YÊU CẦU CẤP QUYỀN LỚP PHỦ",
                            color = Color(0xFFFF3366),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ứng dụng cần quyền vẽ trên màn hình để hiển thị vòng tròn khóa mục tiêu FOV khi chạy nền.",
                        color = Color(0xFFE6E1E5).copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3366)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("CẤP QUYỀN NGAY", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 2. SERVICE STATUS CARD (Sleek Interface Style) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
            border = BoxBorder(1.dp, Color(0xFF49454F)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Service Status",
                        color = Color(0xFFE6E1E5),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Animated pulse dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFD0BCFF), CircleShape)
                        )
                        Text(
                            text = if (isOverlayEnabled) "Active" else "Inactive",
                            color = Color(0xFFD0BCFF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button styled exactly like 'Deactivate/Activate Service' in Sleek design
                Button(
                    onClick = {
                        val newState = !isOverlayEnabled
                        onToggleService(newState)
                        OverlayConfig.isActivated = newState
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF381E72), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isOverlayEnabled) "DEACTIVATE SERVICE" else "ACTIVATE SERVICE",
                            color = Color(0xFF381E72),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "OVERLAY ACTIVE ON FREE FIRE • PID: 8291",
                    color = Color(0xFF938F99),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. LOCK CONFIGURATION (Sleek Interface Style) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B20)),
            border = BoxBorder(1.dp, Color(0xFF49454F)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header with purple block indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(20.dp)
                            .background(Color(0xFFD0BCFF), RoundedCornerShape(2.dp))
                    )
                    Text(
                        text = "LOCK CONFIGURATION",
                        color = Color(0xFFCAC4D0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // FOV Slider Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FOV Area Radius",
                        color = Color(0xFFE6E1E5),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${fovSize.toInt()}px",
                        color = Color(0xFFD0BCFF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = fovSize,
                    onValueChange = {
                        fovSize = it
                        OverlayConfig.fovRadius = it
                        OverlayConfig.triggerUpdate()
                    },
                    valueRange = 100f..600f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFD0BCFF),
                        activeTrackColor = Color(0xFFD0BCFF),
                        inactiveTrackColor = Color(0xFF49454F)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Target Bone Selection Block
                Text(
                    text = "TARGET BONE (AUTO-LOCK)",
                    color = Color(0xFF938F99),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2x3 grid mapping for beautiful layout
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val row1 = listOf("HEAD" to "HEAD", "NECK" to "NECK", "SPINE" to "SPINE")
                    val row2 = listOf("ROOT" to "ROOT", "HIP" to "HIP", "ELBOW" to "ELBOW")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row1.forEach { (key, label) ->
                            val isSelected = selectedBone == key
                            val containerCol = if (isSelected) Color(0xFF381E72) else Color.Transparent
                            val borderCol = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F)
                            val textCol = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF938F99)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .background(containerCol, RoundedCornerShape(12.dp))
                                    .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedBone = key
                                        OverlayConfig.targetBone = key
                                        OverlayConfig.triggerUpdate()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = textCol,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row2.forEach { (key, label) ->
                            val isSelected = selectedBone == key
                            val containerCol = if (isSelected) Color(0xFF381E72) else Color.Transparent
                            val borderCol = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F)
                            val textCol = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF938F99)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .background(containerCol, RoundedCornerShape(12.dp))
                                    .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedBone = key
                                        OverlayConfig.targetBone = key
                                        OverlayConfig.triggerUpdate()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = textCol,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. LIVE DEBUG CONSOLE (Real-time memory reader representation) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B20)),
            border = BoxBorder(1.dp, Color(0xFF49454F)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Scanner Icon",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AotForms.Entity Memory Reader",
                            color = Color(0xFFE6E1E5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Simulated status pulse
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (isAimLocked) Color(0xFFFF3366) else Color(0xFF00FF66),
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAimLocked) "LOCK ENGAGED" else "SCANNING",
                            color = if (isAimLocked) Color(0xFFFF3366) else Color(0xFF00FF66),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // C# class structure print code block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF49454F), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "// C# Memory Map (AotForms.Entity)",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "internal class Entity {",
                            color = Color(0xFFE5C07B),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        // Class Fields
                        FieldLine(name = "Name", value = "\"${simulatedEntity.name}\"", type = "string")
                        FieldLine(name = "Address", value = String.format("0x%08X", simulatedEntity.address), type = "uint")
                        FieldLine(name = "Health", value = "${simulatedEntity.health} HP", type = "short")
                        FieldLine(name = "IsDead", value = "${simulatedEntity.isDead}", type = "bool")
                        FieldLine(name = "IsKnocked", value = "${simulatedEntity.isKnocked}", type = "bool")
                        FieldLine(name = "IsTeam", value = "Bool3${simulatedEntity.isTeam}", type = "Bool3")
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "    // Skeletal Vectors (WorldToScreen Projected)",
                            color = Color.Gray.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        FieldLine(name = "Head", value = "Vector3(${simulatedEntity.head})", type = "Vector3", isLock = selectedBone == "HEAD" && isAimLocked)
                        FieldLine(name = "Neck", value = "Vector3(${simulatedEntity.neck})", type = "Vector3", isLock = selectedBone == "NECK" && isAimLocked)
                        FieldLine(name = "Spine", value = "Vector3(${simulatedEntity.spine})", type = "Vector3", isLock = selectedBone == "SPINE" && isAimLocked)
                        FieldLine(name = "Hip", value = "Vector3(${simulatedEntity.hip})", type = "Vector3", isLock = selectedBone == "HIP" && isAimLocked)
                        FieldLine(name = "LeftShoulder", value = "Vector3(${simulatedEntity.leftShoulder})", type = "Vector3")
                        FieldLine(name = "RightShoulder", value = "Vector3(${simulatedEntity.rightShoulder})", type = "Vector3")
                        
                        Text(
                            text = "}",
                            color = Color(0xFFE5C07B),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "Lưu ý: Ứng dụng vẽ một vòng tròn lớp phủ (FOV) ở trung tâm màn hình. Khi bạn kéo nút AIM TRIGGER trên màn hình, nó sẽ khóa mục tiêu vào các tọa độ khung xương giả định để demo cơ chế hoạt động của AotForms.",
                    color = Color(0xFF938F99),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- 5. EXPLANATION OF OPERATIONS (How to use) ---
        Text(
            text = "HƯỚNG DẪN SỬ DỤNG",
            color = Color(0xFFE6E1E5),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        InstructionStep(step = "1", text = "Cấp quyền hiển thị lớp phủ (Draw Overlays) cho ứng dụng.")
        InstructionStep(step = "2", text = "Bấm nút [ACTIVATE SERVICE] để hiển thị vòng tròn FOV ở giữa màn hình.")
        InstructionStep(step = "3", text = "Sử dụng thanh kéo để thu nhỏ hoặc mở rộng vòng tròn FOV theo ý muốn.")
        InstructionStep(step = "4", text = "Một nút AIM TRIGGER màu hồng sẽ hiển thị. Bạn có thể kéo nút này tới bất kỳ đâu.")
        InstructionStep(step = "5", text = "Khi kẻ địch đi vào vòng tròn FOV, nhấn vào nút AIM TRIGGER để kích hoạt tự động khóa tâm (Aim-lock) vào phần đầu kẻ địch.")
        InstructionStep(step = "6", text = "Bấm [DEACTIVATE SERVICE] trong app hoặc tắt lớp phủ bất kỳ lúc nào.")
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // Navigation-like row for Sleek Interface theme matching
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF2B2930), CircleShape)
                    .border(1.dp, Color(0xFF49454F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("SET", color = Color(0xFFD0BCFF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFD0BCFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("HOME", color = Color(0xFF381E72), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF2B2930), CircleShape)
                    .border(1.dp, Color(0xFF49454F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("INFO", color = Color(0xFFD0BCFF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Footer signature
        Text(
            text = "MAKE BY KIỀU LƯƠNG QUÂN",
            color = Color(0xFF938F99),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .width(32.dp)
                .height(2.dp)
                .background(Color(0xFFD0BCFF).copy(alpha = 0.5f), RoundedCornerShape(1.dp))
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FieldLine(name: String, value: String, type: String, isLock: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Text(
                text = "    internal ",
                color = Color(0xFFC678DD),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "$type ",
                color = Color(0xFF61AFEF),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = name,
                color = Color(0xFFABB2BF),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = "= $value;",
            color = if (isLock) Color(0xFFFF3366) else Color(0xFF98C379),
            fontWeight = if (isLock) FontWeight.Bold else FontWeight.Normal,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun InstructionStep(step: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(Color(0xFF00FF66), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

// Simple composable wrapper to support BoxBorder
@Composable
fun BoxBorder(width: androidx.compose.ui.unit.Dp, color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(width, color)
}
