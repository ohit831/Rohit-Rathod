package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MealLog
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutSession
import com.example.data.report.DayReportItem
import com.example.data.report.WeeklyReportGenerator
import com.example.data.report.WeeklyReportSummary
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CyanCarbs
import com.example.ui.theme.EmeraldProtein
import com.example.ui.theme.GymCardStroke
import com.example.ui.theme.GymDarkBg
import com.example.ui.theme.GymSurface
import com.example.ui.theme.GymSurfaceVariant
import com.example.ui.theme.RoseFat
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ReportTab {
    SUMMARY,
    JSON_VIEW,
    TEXT_VIEW
}

@Composable
fun WeeklyReportDialog(
    workouts: List<WorkoutSession>,
    meals: List<MealLog>,
    profile: UserProfile?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(ReportTab.SUMMARY) }
    var copyConfirmed by remember { mutableStateOf(false) }

    val report: WeeklyReportSummary = remember(workouts, meals, profile) {
        WeeklyReportGenerator.generateReport(workouts, meals, profile)
    }

    val dateSlug = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Storage Access Framework Document Creators
    val jsonFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream: OutputStream ->
                    stream.write(report.jsonString.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Saved JSON report successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save JSON: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val textFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream: OutputStream ->
                    stream.write(report.textString.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Saved Text report successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save Text file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, AmberPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .testTag("weekly_report_dialog"),
            color = GymSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AmberPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "WEEKLY REPORT",
                                    color = AmberSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = EmeraldProtein.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "LOCAL EXPORT",
                                        color = EmeraldProtein,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Workouts & Macros Summary",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_report_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date & Athlete pill strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GymDarkBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Window: ${report.periodRange}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Total Lifted: ${report.totalVolumeKg} kg",
                        color = AmberPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Visual Summary | JSON View | Text View
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GymSurfaceVariant)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ReportTabButton(
                        modifier = Modifier.weight(1f),
                        title = "Visual Summary",
                        icon = Icons.Default.Assessment,
                        isSelected = activeTab == ReportTab.SUMMARY,
                        onClick = { activeTab = ReportTab.SUMMARY },
                        tag = "tab_report_summary"
                    )
                    ReportTabButton(
                        modifier = Modifier.weight(1f),
                        title = "JSON File",
                        icon = Icons.Default.DataObject,
                        isSelected = activeTab == ReportTab.JSON_VIEW,
                        onClick = { activeTab = ReportTab.JSON_VIEW },
                        tag = "tab_report_json"
                    )
                    ReportTabButton(
                        modifier = Modifier.weight(1f),
                        title = "Text File",
                        icon = Icons.Default.Description,
                        isSelected = activeTab == ReportTab.TEXT_VIEW,
                        onClick = { activeTab = ReportTab.TEXT_VIEW },
                        tag = "tab_report_text"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main Content View Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (activeTab) {
                        ReportTab.SUMMARY -> VisualSummaryView(report = report)
                        ReportTab.JSON_VIEW -> CodePreviewView(code = report.jsonString)
                        ReportTab.TEXT_VIEW -> CodePreviewView(code = report.textString)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Export Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Save as JSON Button
                    Button(
                        onClick = {
                            jsonFileLauncher.launch("apex_gym_weekly_summary_$dateSlug.json")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_json_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save JSON",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Save as Text Button
                    Button(
                        onClick = {
                            textFileLauncher.launch("apex_gym_weekly_summary_$dateSlug.txt")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_text_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GymSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Text",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Copy to clipboard
                    OutlinedButton(
                        onClick = {
                            val contentToCopy = if (activeTab == ReportTab.JSON_VIEW) {
                                report.jsonString
                            } else {
                                report.textString
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Apex Gym Weekly Report", contentToCopy)
                            clipboard.setPrimaryClip(clip)
                            copyConfirmed = true
                            Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GymCardStroke),
                        modifier = Modifier.testTag("copy_report_btn")
                    ) {
                        Icon(
                            imageVector = if (copyConfirmed) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy Report",
                            tint = if (copyConfirmed) EmeraldProtein else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Share button
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Apex Gym Weekly Report ($dateSlug)")
                                putExtra(Intent.EXTRA_TEXT, report.textString)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Weekly Report"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GymCardStroke),
                        modifier = Modifier.testTag("share_report_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Report",
                            tint = AmberSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportTabButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) AmberPrimary else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF0F172A) else TextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isSelected) Color(0xFF0F172A) else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun VisualSummaryView(report: WeeklyReportSummary) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Workout Stats Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = GymDarkBg,
                border = BorderStroke(1.dp, GymCardStroke)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WORKOUTS & STRENGTH",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AmberPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${report.totalWorkouts} SESSIONS",
                                color = AmberPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryPill(
                            modifier = Modifier.weight(1f),
                            label = "Total Volume",
                            value = "${report.totalVolumeKg} kg",
                            accent = AmberPrimary
                        )
                        SummaryPill(
                            modifier = Modifier.weight(1f),
                            label = "Daily Avg",
                            value = "${report.dailyAvgVolumeKg} kg/d",
                            accent = CyanCarbs
                        )
                        SummaryPill(
                            modifier = Modifier.weight(1f),
                            label = "Total Time",
                            value = "${report.totalDurationMinutes} min",
                            accent = EmeraldProtein
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Peak Lift: ${report.peakVolumeDayText}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Nutrition Stats Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = GymDarkBg,
                border = BorderStroke(1.dp, GymCardStroke)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = EmeraldProtein,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NUTRITION & MACROS",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldProtein.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "AVG ${report.dailyAvgCalories} KCAL/DAY",
                                color = EmeraldProtein,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryPill(
                            modifier = Modifier.weight(1f),
                            label = "Daily Protein",
                            value = "${report.dailyAvgProtein}g",
                            accent = EmeraldProtein
                        )
                        SummaryPill(
                            modifier = Modifier.weight(1f),
                            label = "Daily Carbs",
                            value = "${report.dailyAvgCarbs}g",
                            accent = CyanCarbs
                        )
                        SummaryPill(
                            modifier = Modifier.weight(1f),
                            label = "Daily Fat",
                            value = "${report.dailyAvgFat}g",
                            accent = RoseFat
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Macro Ratio: ${report.proteinPercent}% Protein • ${report.carbsPercent}% Carbs • ${report.fatPercent}% Fat",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Daily Breakdown List Header
        item {
            Text(
                text = "DAY-BY-DAY LOGS",
                color = AmberSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Day Items
        items(report.dayItems) { day ->
            DayReportRow(day = day)
        }
    }
}

@Composable
private fun DayReportRow(day: DayReportItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GymDarkBg),
        border = BorderStroke(1.dp, GymCardStroke.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${day.dayOfWeek}, ${day.formattedDate}",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (day.dayVolumeKg > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AmberPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${day.dayVolumeKg} kg lifted",
                            color = AmberPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Rest Day",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${day.dayCalories} kcal • ${day.dayProtein}g P • ${day.dayCarbs}g C • ${day.dayFat}g F",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "${day.meals.size} meals",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            if (day.workouts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                day.workouts.forEach { w ->
                    Text(
                        text = "🏋️ ${w.workoutName} (${w.durationMinutes} min)",
                        color = AmberSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = GymSurfaceVariant
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun CodePreviewView(code: String) {
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp)),
        color = GymDarkBg,
        border = BorderStroke(1.dp, GymCardStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(scrollState)
                .horizontalScroll(horizontalScrollState)
        ) {
            Text(
                text = code,
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }
    }
}
