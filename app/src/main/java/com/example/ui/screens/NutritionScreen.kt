package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MealLog
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
import com.example.ui.viewmodel.GymViewModel

@Composable
fun NutritionScreen(
    viewModel: GymViewModel
) {
    val meals by viewModel.mealsState.collectAsState()
    val workouts by viewModel.workoutsState.collectAsState()
    val customFoods by viewModel.customFoodsState.collectAsState()
    val profile by viewModel.profileState.collectAsState()
    val nutritionDate by viewModel.nutritionDate.collectAsState()

    var showQuickLogMacroDialog by remember { mutableStateOf(false) }
    var quickLogInitialMealType by remember { mutableStateOf("lunch") }
    var showLogFoodDialog by remember { mutableStateOf(false) }
    var showEditTargetsDialog by remember { mutableStateOf(false) }
    var showAddCustomFoodDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    // Targets (from profile or gym defaults)
    val targetCalories = profile?.dailyCalorieTarget ?: 2400L
    val targetProtein = profile?.dailyProteinTarget ?: 160L
    val targetCarbs = profile?.dailyCarbsTarget ?: 250L
    val targetFat = profile?.dailyFatTarget ?: 70L

    // Filter meals for the selected date
    val dayMeals = remember(meals, nutritionDate) {
        meals.filter { it.date == nutritionDate }
    }

    val consumedCalories = remember(dayMeals) { dayMeals.sumOf { it.calories } }
    val consumedProtein = remember(dayMeals) { dayMeals.sumOf { it.protein } }
    val consumedCarbs = remember(dayMeals) { dayMeals.sumOf { it.carbs } }
    val consumedFat = remember(dayMeals) { dayMeals.sumOf { it.fat } }

    val remainingCalories = (targetCalories - consumedCalories).coerceAtLeast(0L)
    val remainingProtein = (targetProtein - consumedProtein).coerceAtLeast(0L)
    val remainingCarbs = (targetCarbs - consumedCarbs).coerceAtLeast(0L)
    val remainingFat = (targetFat - consumedFat).coerceAtLeast(0L)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GymDarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Date Switcher Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = GymSurface,
                border = BorderStroke(1.dp, GymCardStroke)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.shiftNutritionDate(-1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = TextSecondary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DAILY MACRO TRACKER",
                            color = AmberSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = nutritionDate,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { viewModel.shiftNutritionDate(1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = TextSecondary)
                    }
                }
            }
        }

        // Weekly Macro & Workout Report Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showReportDialog = true }
                    .testTag("nutrition_export_report_btn"),
                shape = RoundedCornerShape(16.dp),
                color = GymSurface,
                border = BorderStroke(1.dp, EmeraldProtein.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(EmeraldProtein.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = EmeraldProtein,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Weekly Macro & Workout Report",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Generate local JSON / Text file",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldProtein.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Save Report",
                            color = EmeraldProtein,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Calorie & Daily Macro Target Progress Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = GymSurface,
                border = BorderStroke(1.dp, GymCardStroke)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DAILY CALORIE BUDGET",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$consumedCalories",
                                    color = TextPrimary,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = " / $targetCalories kcal",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { showEditTargetsDialog = true },
                            modifier = Modifier.testTag("edit_macro_targets_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Targets", tint = AmberPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Calorie Progress Bar
                    val calorieFraction = (consumedCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { calorieFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AmberPrimary,
                        trackColor = GymSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (remainingCalories > 0) "$remainingCalories kcal remaining today" else "Daily calorie goal reached!",
                        color = if (remainingCalories > 0) TextSecondary else EmeraldProtein,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Macronutrient 3-Column Breakdown (Protein, Carbs, Fat)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MacroProgressBar(
                            modifier = Modifier.weight(1f),
                            label = "PROTEIN",
                            consumed = consumedProtein,
                            target = targetProtein,
                            remaining = remainingProtein,
                            accentColor = EmeraldProtein
                        )
                        MacroProgressBar(
                            modifier = Modifier.weight(1f),
                            label = "CARBS",
                            consumed = consumedCarbs,
                            target = targetCarbs,
                            remaining = remainingCarbs,
                            accentColor = CyanCarbs
                        )
                        MacroProgressBar(
                            modifier = Modifier.weight(1f),
                            label = "FAT",
                            consumed = consumedFat,
                            target = targetFat,
                            remaining = remainingFat,
                            accentColor = RoseFat
                        )
                    }
                }
            }
        }

        // Quick Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Action: Quick Log Macros
                Button(
                    onClick = {
                        quickLogInitialMealType = "lunch"
                        showQuickLogMacroDialog = true
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("quick_log_macros_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberPrimary,
                        contentColor = Color(0xFF0F172A)
                    )
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Macros", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }

                // Secondary Action: Search Database
                Button(
                    onClick = { showLogFoodDialog = true },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(48.dp)
                        .testTag("log_food_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GymSurfaceVariant,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmberSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Search Food", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Custom food creator
                IconButton(
                    onClick = { showAddCustomFoodDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(GymSurfaceVariant)
                        .testTag("create_custom_food_button")
                ) {
                    Icon(Icons.Default.Fastfood, contentDescription = "Add Custom Food", tint = AmberPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Four distinct meal categories with per-meal macro breakdowns & direct logging
        val mealCategories = listOf(
            MealCategoryMeta("breakfast", "Breakfast", Icons.Default.LocalCafe),
            MealCategoryMeta("lunch", "Lunch", Icons.Default.LunchDining),
            MealCategoryMeta("dinner", "Dinner", Icons.Default.DinnerDining),
            MealCategoryMeta("snack", "Snacks", Icons.Default.Fastfood)
        )

        mealCategories.forEach { meta ->
            val categoryMeals = dayMeals.filter { it.mealType.equals(meta.key, ignoreCase = true) }
            val catCalories = categoryMeals.sumOf { it.calories }
            val catProtein = categoryMeals.sumOf { it.protein }
            val catCarbs = categoryMeals.sumOf { it.carbs }
            val catFat = categoryMeals.sumOf { it.fat }

            item(key = meta.key) {
                EnhancedMealSection(
                    title = meta.label,
                    mealKey = meta.key,
                    icon = meta.icon,
                    totalCalories = catCalories,
                    protein = catProtein,
                    carbs = catCarbs,
                    fat = catFat,
                    meals = categoryMeals,
                    onQuickLogMacros = {
                        quickLogInitialMealType = meta.key
                        showQuickLogMacroDialog = true
                    },
                    onDeleteMeal = { mealId -> viewModel.deleteMeal(mealId) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    // Quick Log Macro Dialog (direct entry of P, C, F per meal)
    if (showQuickLogMacroDialog) {
        QuickLogMealDialog(
            initialMealType = quickLogInitialMealType,
            onDismiss = { showQuickLogMacroDialog = false },
            onConfirm = { mealType, foodName, p, c, f, cal ->
                viewModel.logMealMacros(mealType, foodName, p, c, f, cal)
                showQuickLogMacroDialog = false
            }
        )
    }

    // Log Food Database Dialog
    if (showLogFoodDialog) {
        LogFoodDialog(
            customFoods = customFoods,
            onDismiss = { showLogFoodDialog = false },
            onConfirmLog = { foodName, servingSize, servings, cal, p, c, f, mealType ->
                viewModel.logFood(foodName, servingSize, servings, cal, p, c, f, mealType)
                showLogFoodDialog = false
            }
        )
    }

    // Add Custom Food Dialog
    if (showAddCustomFoodDialog) {
        AddCustomFoodDialog(
            onDismiss = { showAddCustomFoodDialog = false },
            onConfirm = { name, serving, cal, p, c, f ->
                viewModel.saveCustomFood(name, serving, cal, p, c, f)
                showAddCustomFoodDialog = false
            }
        )
    }

    // Edit Targets Dialog
    if (showEditTargetsDialog) {
        EditTargetsDialog(
            currentCalories = targetCalories,
            currentProtein = targetProtein,
            currentCarbs = targetCarbs,
            currentFat = targetFat,
            onDismiss = { showEditTargetsDialog = false },
            onSave = { cal, p, c, f ->
                viewModel.updateProfileTargets(cal, p, c, f, profile?.experienceLevel ?: "Beginner")
                showEditTargetsDialog = false
            }
        )
    }

    // Weekly Summary Report Dialog
    if (showReportDialog) {
        WeeklyReportDialog(
            workouts = workouts,
            meals = meals,
            profile = profile,
            onDismiss = { showReportDialog = false }
        )
    }
}

@Composable
fun MacroProgressBar(
    modifier: Modifier = Modifier,
    label: String,
    consumed: Long,
    target: Long,
    remaining: Long,
    accentColor: Color
) {
    val fraction = if (target > 0) (consumed.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = GymSurfaceVariant,
        border = BorderStroke(0.5.dp, GymCardStroke)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${consumed}g",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = " / ${target}g",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 2.dp, bottom = 1.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = GymSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${remaining}g left",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EnhancedMealSection(
    title: String,
    mealKey: String,
    icon: ImageVector,
    totalCalories: Long,
    protein: Long,
    carbs: Long,
    fat: Long,
    meals: List<MealLog>,
    onQuickLogMacros: () -> Unit,
    onDeleteMeal: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = GymSurface,
        border = BorderStroke(1.dp, GymCardStroke)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Meal Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AmberPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = AmberPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$totalCalories kcal",
                            color = AmberSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Direct "+ Log Macros" button for this specific meal
                OutlinedButton(
                    onClick = onQuickLogMacros,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AmberPrimary
                    ),
                    modifier = Modifier.testTag("quick_log_macro_meal_$mealKey")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Macros", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Per-Meal Macro Badges Sub-header
            if (meals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MealMacroChip(label = "Protein", value = "${protein}g", color = EmeraldProtein)
                    MealMacroChip(label = "Carbs", value = "${carbs}g", color = CyanCarbs)
                    MealMacroChip(label = "Fat", value = "${fat}g", color = RoseFat)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Logged Food Entries
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    meals.forEach { meal ->
                        MealEntryRow(meal = meal, onDelete = { onDeleteMeal(meal.id) })
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onQuickLogMacros),
                    color = GymSurfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "No macros logged for $title yet",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "+ Quick Log",
                            color = AmberPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealMacroChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$label: ", color = TextSecondary, fontSize = 10.sp)
            Text(text = value, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MealEntryRow(meal: MealLog, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GymSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = meal.foodName,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${meal.calories} kcal",
                    color = AmberSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "•", color = TextMuted, fontSize = 10.sp)
                Text(text = "P: ${meal.protein}g", color = EmeraldProtein, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "C: ${meal.carbs}g", color = CyanCarbs, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "F: ${meal.fat}g", color = RoseFat, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete Food",
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private data class MealCategoryMeta(
    val key: String,
    val label: String,
    val icon: ImageVector
)
