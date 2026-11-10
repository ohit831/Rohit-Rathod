package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickLogMealDialog(
    initialMealType: String = "lunch",
    onDismiss: () -> Unit,
    onConfirm: (mealType: String, foodName: String, protein: Long, carbs: Long, fat: Long, calories: Long?) -> Unit
) {
    var selectedMealType by remember { mutableStateOf(initialMealType) }
    var mealDescription by remember { mutableStateOf("") }

    var protein by remember { mutableLongStateOf(30L) }
    var carbs by remember { mutableLongStateOf(40L) }
    var fat by remember { mutableLongStateOf(10L) }

    var customCaloriesText by remember { mutableStateOf("") }
    var useCustomCalories by remember { mutableStateOf(false) }

    val calculatedCalories = (protein * 4L) + (carbs * 4L) + (fat * 9L)
    val displayCalories = if (useCustomCalories && customCaloriesText.isNotBlank()) {
        customCaloriesText.toLongOrNull() ?: calculatedCalories
    } else {
        calculatedCalories
    }

    val mealTypes = listOf(
        MealTypeItem("breakfast", "Breakfast", Icons.Default.LocalCafe),
        MealTypeItem("lunch", "Lunch", Icons.Default.LunchDining),
        MealTypeItem("dinner", "Dinner", Icons.Default.DinnerDining),
        MealTypeItem("snack", "Snack", Icons.Default.Fastfood)
    )

    val quickPresetSuggestions = listOf(
        "Chicken & Rice",
        "Oats & Protein",
        "Eggs & Toast",
        "Post-Workout Shake",
        "Greek Yogurt Bowl",
        "Steak & Sweet Potato"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, GymCardStroke, RoundedCornerShape(24.dp)),
            color = GymDarkBg
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LOG MEAL MACROS",
                            color = AmberSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Daily Macro Tracker",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Meal Category Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    mealTypes.forEach { item ->
                        val isSelected = selectedMealType.equals(item.key, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedMealType = item.key }
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) AmberPrimary else GymCardStroke,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("select_meal_type_${item.key}"),
                            color = if (isSelected) AmberPrimary.copy(alpha = 0.2f) else GymSurfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) AmberPrimary else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.label,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Meal Description
                OutlinedTextField(
                    value = mealDescription,
                    onValueChange = { mealDescription = it },
                    placeholder = {
                        Text(
                            text = "e.g. Chicken breast & sweet potato, Whey shake...",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    },
                    label = { Text("Meal Description (Optional)", color = TextSecondary, fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("macro_meal_description_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = GymCardStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick preset pill suggestions
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickPresetSuggestions.forEach { suggestion ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { mealDescription = suggestion }
                                .border(0.5.dp, GymCardStroke, RoundedCornerShape(8.dp)),
                            color = GymSurface
                        ) {
                            Text(
                                text = suggestion,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 3 MACRO INPUT CARDS: PROTEIN, CARBS, FAT ---
                MacroInputRow(
                    label = "Protein",
                    unit = "g",
                    accentColor = EmeraldProtein,
                    value = protein,
                    onValueChange = { protein = it.coerceIn(0L, 500L) },
                    kcalPerGram = 4,
                    testTagPrefix = "protein"
                )

                Spacer(modifier = Modifier.height(10.dp))

                MacroInputRow(
                    label = "Carbohydrates",
                    unit = "g",
                    accentColor = CyanCarbs,
                    value = carbs,
                    onValueChange = { carbs = it.coerceIn(0L, 500L) },
                    kcalPerGram = 4,
                    testTagPrefix = "carbs"
                )

                Spacer(modifier = Modifier.height(10.dp))

                MacroInputRow(
                    label = "Healthy Fats",
                    unit = "g",
                    accentColor = RoseFat,
                    value = fat,
                    onValueChange = { fat = it.coerceIn(0L, 300L) },
                    kcalPerGram = 9,
                    testTagPrefix = "fat"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Calculated Calories & Macro Proportion Breakdown
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = GymSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GymCardStroke)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ESTIMATED ENERGY",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "$displayCalories",
                                        color = TextPrimary,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = " kcal",
                                        color = AmberSecondary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Macro distribution badges
                            val totalGrams = (protein + carbs + fat).coerceAtLeast(1L).toFloat()
                            val pPct = ((protein / totalGrams) * 100).toInt()
                            val cPct = ((carbs / totalGrams) * 100).toInt()
                            val fPct = (100 - pPct - cPct).coerceAtLeast(0)

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                MacroBadge(label = "P", pct = pPct, color = EmeraldProtein)
                                MacroBadge(label = "C", pct = cPct, color = CyanCarbs)
                                MacroBadge(label = "F", pct = fPct, color = RoseFat)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual Multi-Color Macro Ratio Bar
                        val totalGramsFloat = (protein + carbs + fat).coerceAtLeast(1L).toFloat()
                        val pWeight = (protein / totalGramsFloat).coerceIn(0.01f, 1f)
                        val cWeight = (carbs / totalGramsFloat).coerceIn(0.01f, 1f)
                        val fWeight = (fat / totalGramsFloat).coerceIn(0.01f, 1f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(GymSurfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(pWeight)
                                    .height(8.dp)
                                    .background(EmeraldProtein)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(cWeight)
                                    .height(8.dp)
                                    .background(CyanCarbs)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(fWeight)
                                    .height(8.dp)
                                    .background(RoseFat)
                            )
                        }

                        // Calorie override option toggle
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { useCustomCalories = !useCustomCalories }
                            ) {
                                Text(
                                    text = if (useCustomCalories) "Using custom calories" else "Adjust calories manually?",
                                    color = AmberSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            if (useCustomCalories) {
                                OutlinedTextField(
                                    value = customCaloriesText,
                                    onValueChange = { customCaloriesText = it },
                                    label = { Text("kcal", fontSize = 10.sp) },
                                    modifier = Modifier.width(90.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AmberPrimary,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val finalCalories = if (useCustomCalories && customCaloriesText.isNotBlank()) {
                                customCaloriesText.toLongOrNull() ?: calculatedCalories
                            } else {
                                calculatedCalories
                            }
                            onConfirm(
                                selectedMealType,
                                mealDescription,
                                protein,
                                carbs,
                                fat,
                                finalCalories
                            )
                        },
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp)
                            .testTag("confirm_log_macros_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberPrimary,
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Text(
                            text = "Log ${selectedMealType.replaceFirstChar { it.uppercase() }}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroInputRow(
    label: String,
    unit: String,
    accentColor: Color,
    value: Long,
    onValueChange: (Long) -> Unit,
    kcalPerGram: Int,
    testTagPrefix: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GymSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GymCardStroke)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${value * kcalPerGram} kcal)",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                // Value Display & Steppers
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$value",
                        color = accentColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = unit,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 2.dp, end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fast Increment / Decrement Stepper Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickStepButton("-10", onClick = { onValueChange(value - 10) })
                QuickStepButton("-5", onClick = { onValueChange(value - 5) })
                QuickStepButton("+5", isPositive = true, accent = accentColor, onClick = { onValueChange(value + 5) })
                QuickStepButton("+10", isPositive = true, accent = accentColor, onClick = { onValueChange(value + 10) })
                QuickStepButton("+25", isPositive = true, accent = accentColor, onClick = { onValueChange(value + 25) })
            }
        }
    }
}

@Composable
private fun QuickStepButton(
    label: String,
    isPositive: Boolean = false,
    accent: Color = AmberPrimary,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .border(
                width = 0.5.dp,
                color = if (isPositive) accent.copy(alpha = 0.5f) else GymCardStroke,
                shape = RoundedCornerShape(8.dp)
            ),
        color = if (isPositive) accent.copy(alpha = 0.15f) else GymSurfaceVariant
    ) {
        Text(
            text = label,
            color = if (isPositive) accent else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun MacroBadge(label: String, pct: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = "$pct%", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private data class MealTypeItem(
    val key: String,
    val label: String,
    val icon: ImageVector
)
