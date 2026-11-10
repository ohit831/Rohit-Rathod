package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutSession
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CyanCarbs
import com.example.ui.theme.EmeraldProtein
import com.example.ui.theme.GymCardStroke
import com.example.ui.theme.GymDarkBg
import com.example.ui.theme.GymSurface
import com.example.ui.theme.GymSurfaceVariant
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyVolumeData(
    val dateString: String,      // "2026-11-03"
    val dayOfWeekShort: String,  // "Mon", "Tue"
    val dayOfMonth: String,      // "3"
    val formattedDateFull: String, // "Tuesday, Nov 3"
    val totalVolumeKg: Long,     // 4650
    val sessions: List<WorkoutSession>,
    val isToday: Boolean,
    val isPeak: Boolean
)

enum class VolumeChartMode {
    BAR_CHART,
    AREA_TREND
}

@Composable
fun WeeklyVolumeChart(
    workouts: List<WorkoutSession>,
    modifier: Modifier = Modifier,
    onLogWorkoutClick: (() -> Unit)? = null
) {
    var chartMode by remember { mutableStateOf(VolumeChartMode.BAR_CHART) }
    var selectedDayIndex by remember { mutableIntStateOf(6) } // Default to latest/today

    // Build rolling 7-day data (from 6 days ago up to today)
    val dailyPoints = remember(workouts) {
        val dateFormatKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayNumFormat = SimpleDateFormat("d", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val todayStr = dateFormatKey.format(Date())

        val list = mutableListOf<DailyVolumeData>()
        val cal = Calendar.getInstance()

        // Peak volume finder
        val tempVolumes = mutableListOf<Long>()
        for (i in 6 downTo 0) {
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dStr = dateFormatKey.format(c.time)
            val dayVol = workouts.filter { it.completedDate == dStr }.sumOf { it.totalVolumeKg }
            tempVolumes.add(dayVol)
        }
        val maxVol = tempVolumes.maxOrNull() ?: 0L

        for (i in 6 downTo 0) {
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dStr = dateFormatKey.format(c.time)
            val matchedSessions = workouts.filter { it.completedDate == dStr }
            val vol = matchedSessions.sumOf { it.totalVolumeKg }
            list.add(
                DailyVolumeData(
                    dateString = dStr,
                    dayOfWeekShort = dayOfWeekFormat.format(c.time).uppercase(),
                    dayOfMonth = dayNumFormat.format(c.time),
                    formattedDateFull = fullDateFormat.format(c.time),
                    totalVolumeKg = vol,
                    sessions = matchedSessions,
                    isToday = dStr == todayStr,
                    isPeak = vol > 0 && vol == maxVol
                )
            )
        }
        list
    }

    val total7DayVolume = remember(dailyPoints) { dailyPoints.sumOf { it.totalVolumeKg } }
    val maxDailyVolume = remember(dailyPoints) {
        val max = dailyPoints.maxOfOrNull { it.totalVolumeKg } ?: 0L
        if (max <= 0L) 5000L else max
    }
    val peakDay = remember(dailyPoints) { dailyPoints.maxByOrNull { it.totalVolumeKg } }
    val workoutDaysCount = remember(dailyPoints) { dailyPoints.count { it.totalVolumeKg > 0 } }
    val dailyAvgVolume = if (dailyPoints.isNotEmpty()) total7DayVolume / dailyPoints.size else 0L

    // Progressive overload / strength gain calculation
    val strengthGainPercent = remember(dailyPoints) {
        val nonZeroVolumes = dailyPoints.filter { it.totalVolumeKg > 0 }.map { it.totalVolumeKg }
        if (nonZeroVolumes.size >= 2) {
            val first = nonZeroVolumes.first()
            val last = nonZeroVolumes.last()
            if (first > 0) {
                ((last - first).toFloat() / first.toFloat() * 100f).toInt()
            } else 0
        } else 0
    }

    val selectedDay = dailyPoints.getOrNull(selectedDayIndex) ?: dailyPoints.lastOrNull()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_volume_chart"),
        shape = RoundedCornerShape(24.dp),
        color = GymSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GymCardStroke)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Header Row: Title & Recharts-style Live Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AmberPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Strength Gains",
                            tint = AmberPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "WEEKLY STRENGTH GAINS",
                                color = AmberSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AmberPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "RECHARTS",
                                    color = AmberPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        Text(
                            text = "Last 7 Days Weight Lifted",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Mode toggle (Bar / Area)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GymSurfaceVariant)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (chartMode == VolumeChartMode.BAR_CHART) AmberPrimary else Color.Transparent)
                            .clickable { chartMode = VolumeChartMode.BAR_CHART }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("chart_mode_bars"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Bar Chart",
                            tint = if (chartMode == VolumeChartMode.BAR_CHART) Color(0xFF0F172A) else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (chartMode == VolumeChartMode.AREA_TREND) AmberPrimary else Color.Transparent)
                            .clickable { chartMode = VolumeChartMode.AREA_TREND }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("chart_mode_trend"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Area Trend",
                            tint = if (chartMode == VolumeChartMode.AREA_TREND) Color(0xFF0F172A) else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Strength Metrics Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCallout(
                    modifier = Modifier.weight(1f),
                    title = "7-Day Volume",
                    value = formatVolumeK(total7DayVolume),
                    unit = "kg lifted",
                    accent = AmberPrimary,
                    icon = Icons.Default.FitnessCenter
                )
                MetricCallout(
                    modifier = Modifier.weight(1f),
                    title = "Daily Average",
                    value = formatVolumeK(dailyAvgVolume),
                    unit = "kg/day",
                    accent = CyanCarbs,
                    icon = Icons.Default.Speed
                )
                MetricCallout(
                    modifier = Modifier.weight(1f),
                    title = if (strengthGainPercent >= 0) "Strength Gain" else "Progression",
                    value = if (strengthGainPercent >= 0) "+$strengthGainPercent%" else "$strengthGainPercent%",
                    unit = if (workoutDaysCount >= 3) "3x completed" else "$workoutDaysCount/3 target",
                    accent = if (strengthGainPercent >= 0) EmeraldProtein else AmberSecondary,
                    icon = if (strengthGainPercent >= 0) Icons.Default.TrendingUp else Icons.Default.EmojiEvents
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Chart Canvas Container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = GymDarkBg.copy(alpha = 0.75f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GymCardStroke.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp)) {

                    // Y-Axis Ceiling Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PEAK: ${formatVolumeK(maxDailyVolume)} kg",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Tap a bar to inspect day details",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (chartMode == VolumeChartMode.BAR_CHART) {
                        // RECHARTS BAR CHART IMPLEMENTATION
                        BarChartVolumeView(
                            dailyPoints = dailyPoints,
                            maxDailyVolume = maxDailyVolume,
                            selectedIndex = selectedDayIndex,
                            onSelectDay = { index -> selectedDayIndex = index }
                        )
                    } else {
                        // RECHARTS AREA / SPLINE TREND IMPLEMENTATION
                        AreaTrendVolumeView(
                            dailyPoints = dailyPoints,
                            maxDailyVolume = maxDailyVolume,
                            selectedIndex = selectedDayIndex,
                            onSelectDay = { index -> selectedDayIndex = index }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recharts-style Tooltip / Inspector Card for Selected Day
            selectedDay?.let { day ->
                DayVolumeInspectorCard(
                    data = day,
                    onLogWorkoutClick = onLogWorkoutClick
                )
            }
        }
    }
}

@Composable
private fun BarChartVolumeView(
    dailyPoints: List<DailyVolumeData>,
    maxDailyVolume: Long,
    selectedIndex: Int,
    onSelectDay: (Int) -> Unit
) {
    val effectiveMax = if (maxDailyVolume <= 0L) 1L else maxDailyVolume

    Column(modifier = Modifier.fillMaxWidth()) {
        // Grid background with bars
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            // Horizontal dashed guide lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineStroke = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
                val lineCol = GymCardStroke.copy(alpha = 0.35f)
                // 100% line
                drawLine(
                    color = lineCol,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
                // 50% line
                drawLine(
                    color = lineCol,
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = 1.dp.toPx()
                )
                // Base line
                drawLine(
                    color = GymCardStroke.copy(alpha = 0.6f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // 7 Columns of Bars
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyPoints.forEachIndexed { index, day ->
                    val isSelected = selectedIndex == index
                    val rawFraction = (day.totalVolumeKg.toFloat() / effectiveMax.toFloat()).coerceIn(0f, 1f)

                    val animatedFraction by animateFloatAsState(
                        targetValue = rawFraction,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "barHeight_$index"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelectDay(index) }
                            .padding(horizontal = 3.dp)
                            .testTag("volume_bar_day_$index"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Value label on top of bar
                        if (day.totalVolumeKg > 0) {
                            Text(
                                text = formatShortK(day.totalVolumeKg),
                                color = if (isSelected) AmberSecondary else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                maxLines = 1
                            )
                        } else {
                            Text(
                                text = "—",
                                color = TextMuted.copy(alpha = 0.6f),
                                fontSize = 9.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // The Bar Pillar
                        val barHeight = if (day.totalVolumeKg > 0) {
                            (120.dp * animatedFraction).coerceAtLeast(14.dp)
                        } else {
                            6.dp // Rest day stub
                        }

                        val barBrush = remember(day.isPeak, isSelected, day.totalVolumeKg) {
                            when {
                                day.totalVolumeKg == 0L -> Brush.verticalGradient(
                                    listOf(GymCardStroke.copy(alpha = 0.6f), GymCardStroke.copy(alpha = 0.3f))
                                )
                                day.isPeak -> Brush.verticalGradient(
                                    listOf(EmeraldProtein, AmberPrimary)
                                )
                                isSelected -> Brush.verticalGradient(
                                    listOf(AmberSecondary, AmberPrimary)
                                )
                                else -> Brush.verticalGradient(
                                    listOf(AmberPrimary, AmberPrimary.copy(alpha = 0.65f))
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (isSelected) 0.85f else 0.7f)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                .background(barBrush)
                                .then(
                                    if (isSelected) Modifier.border(
                                        width = 1.5.dp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                                    ) else Modifier
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // X-Axis Day Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dailyPoints.forEachIndexed { index, day ->
                val isSelected = selectedIndex == index
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectDay(index) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.dayOfWeekShort,
                        color = when {
                            isSelected -> AmberPrimary
                            day.isToday -> EmeraldProtein
                            else -> TextSecondary
                        },
                        fontSize = 10.sp,
                        fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Medium
                    )
                    Text(
                        text = day.dayOfMonth,
                        color = if (isSelected) TextPrimary else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                    )
                    if (day.isToday) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(EmeraldProtein)
                        )
                    } else if (day.isPeak) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(AmberSecondary)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaTrendVolumeView(
    dailyPoints: List<DailyVolumeData>,
    maxDailyVolume: Long,
    selectedIndex: Int,
    onSelectDay: (Int) -> Unit
) {
    val effectiveMax = if (maxDailyVolume <= 0L) 1L else maxDailyVolume

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val pointCount = dailyPoints.size

                if (pointCount > 1) {
                    val stepX = width / (pointCount - 1)

                    // Build area and line path
                    val linePath = Path()
                    val areaPath = Path()

                    val coordinates = dailyPoints.mapIndexed { i, pt ->
                        val x = i * stepX
                        val fraction = (pt.totalVolumeKg.toFloat() / effectiveMax.toFloat()).coerceIn(0f, 1f)
                        val y = height - (fraction * (height - 24.dp.toPx())) - 8.dp.toPx()
                        Offset(x, y)
                    }

                    linePath.moveTo(coordinates[0].x, coordinates[0].y)
                    areaPath.moveTo(coordinates[0].x, height)
                    areaPath.lineTo(coordinates[0].x, coordinates[0].y)

                    for (i in 1 until coordinates.size) {
                        val p0 = coordinates[i - 1]
                        val p1 = coordinates[i]
                        // Smooth bezier curve
                        val cX = (p0.x + p1.x) / 2f
                        linePath.cubicTo(cX, p0.y, cX, p1.y, p1.x, p1.y)
                        areaPath.cubicTo(cX, p0.y, cX, p1.y, p1.x, p1.y)
                    }

                    areaPath.lineTo(coordinates.last().x, height)
                    areaPath.close()

                    // Draw Gradient Fill
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AmberPrimary.copy(alpha = 0.45f),
                                AmberPrimary.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw Stroke Line
                    drawPath(
                        path = linePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(AmberSecondary, AmberPrimary, EmeraldProtein)
                        ),
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // Draw Points
                    coordinates.forEachIndexed { i, offset ->
                        val isSelected = i == selectedIndex
                        val pt = dailyPoints[i]

                        // Halo
                        if (isSelected) {
                            drawCircle(
                                color = AmberPrimary.copy(alpha = 0.4f),
                                radius = 10.dp.toPx(),
                                center = offset
                            )
                        }

                        // Dot
                        drawCircle(
                            color = if (pt.isPeak) EmeraldProtein else AmberPrimary,
                            radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                            center = offset
                        )

                        // Center white core
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 2.5.dp.toPx() else 1.5.dp.toPx(),
                            center = offset
                        )
                    }
                }
            }

            // Clickable overlay slices
            Row(modifier = Modifier.fillMaxSize()) {
                dailyPoints.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelectDay(index) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // X-Axis Day Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dailyPoints.forEachIndexed { index, day ->
                val isSelected = selectedIndex == index
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectDay(index) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.dayOfWeekShort,
                        color = if (isSelected) AmberPrimary else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    Text(
                        text = "${formatShortK(day.totalVolumeKg)}kg",
                        color = if (isSelected) TextPrimary else TextMuted,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun DayVolumeInspectorCard(
    data: DailyVolumeData,
    onLogWorkoutClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("volume_inspector_card"),
        shape = RoundedCornerShape(16.dp),
        color = GymSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (data.isPeak) EmeraldProtein.copy(alpha = 0.5f) else AmberPrimary.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Date & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (data.totalVolumeKg > 0) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (data.totalVolumeKg > 0) EmeraldProtein else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = data.formattedDateFull,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (data.isPeak) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldProtein.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "WEEK PEAK ★",
                            color = EmeraldProtein,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (data.isToday) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AmberPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "TODAY",
                            color = AmberPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Volume Stat Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL WEIGHT LIFTED",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${data.totalVolumeKg}",
                            color = AmberPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "kg",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                if (data.totalVolumeKg > 0) {
                    Text(
                        text = "${data.sessions.size} workout session${if (data.sessions.size > 1) "s" else ""}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Workouts & Exercises list or Rest day note
            if (data.sessions.isNotEmpty()) {
                data.sessions.forEach { session ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = GymDarkBg.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = session.workoutName.ifBlank { "Full-Body Workout" },
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${session.durationMinutes} min • ${session.totalVolumeKg} kg",
                                    color = AmberSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (session.exercises.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                session.exercises.take(3).forEach { ex ->
                                    Text(
                                        text = "• ${ex.name} (${ex.setsCount} sets • ${ex.weightSummary})",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (session.exercises.size > 3) {
                                    Text(
                                        text = "+ ${session.exercises.size - 3} more exercises",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Rest Day Note
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "Rest & Recovery Day. Muscle protein synthesis and recovery occurs during rest periods between sessions.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    if (onLogWorkoutClick != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onLogWorkoutClick,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("log_past_workout_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Log Completed Workout For This Day",
                                color = AmberPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCallout(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = GymSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, GymCardStroke.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = accent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = unit,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

private fun formatVolumeK(volumeKg: Long): String {
    return if (volumeKg >= 1000) {
        val thousands = volumeKg / 1000
        val remainder = (volumeKg % 1000) / 100
        if (remainder > 0) "$thousands.${remainder}k" else "${thousands}k"
    } else {
        "$volumeKg"
    }
}

private fun formatShortK(volumeKg: Long): String {
    return if (volumeKg >= 1000) {
        val thousands = volumeKg / 1000
        val rem = (volumeKg % 1000) / 100
        if (rem > 0) "$thousands.${rem}k" else "${thousands}k"
    } else {
        "$volumeKg"
    }
}
