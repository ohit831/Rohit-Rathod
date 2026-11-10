package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.GymDataPresets
import com.example.data.model.CustomFood
import com.example.data.model.PresetFood
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogFoodDialog(
    customFoods: List<CustomFood>,
    onDismiss: () -> Unit,
    onConfirmLog: (foodName: String, servingSize: String, servings: Double, cal: Long, p: Long, c: Long, f: Long, mealType: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedMealType by remember { mutableStateOf("lunch") }
    var servingsMultiplier by remember { mutableDoubleStateOf(1.0) }

    val presetItems = GymDataPresets.PRESET_FOODS
    val filteredItems = remember(searchQuery, selectedCategory, customFoods) {
        val q = searchQuery.trim().lowercase()

        val customMapped = customFoods.map {
            PresetFood(
                id = it.id,
                name = it.name,
                category = "Custom",
                servingSize = it.serving,
                calories = it.calories,
                protein = it.protein,
                carbs = it.carbs,
                fat = it.fat,
                gymTip = "Custom food entry"
            )
        }

        (presetItems + customMapped).filter { food ->
            val matchesCategory = selectedCategory == "All" || food.category.contains(selectedCategory, ignoreCase = true)
            val matchesSearch = q.isBlank() || food.name.lowercase().contains(q) || food.category.lowercase().contains(q)
            matchesCategory && matchesSearch
        }
    }

    var selectedFood by remember { mutableStateOf<PresetFood?>(filteredItems.firstOrNull()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GymDarkBg)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Search Food Database",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search chicken, oats, whey, salmon...", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AmberPrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("food_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = GymCardStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val categories = listOf("All", "Protein", "Carbs", "Fats", "Vegetables", "Custom")
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberPrimary,
                                selectedLabelColor = Color(0xFF0F172A),
                                containerColor = GymSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Meal Category Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("breakfast", "lunch", "dinner", "snack").forEach { type ->
                        val isSelected = selectedMealType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMealType = type },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AmberPrimary else GymSurfaceVariant
                        ) {
                            Text(
                                text = type.replaceFirstChar { it.uppercase() },
                                color = if (isSelected) Color(0xFF0F172A) else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Food Items List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems) { food ->
                        val isSelected = selectedFood?.id == food.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedFood = food }
                                .testTag("food_item_${food.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GymSurfaceVariant else GymSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AmberPrimary else GymCardStroke
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = food.name,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${food.servingSize} • ${food.calories} kcal",
                                            color = AmberSecondary,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("P: ${food.protein}g", color = EmeraldProtein, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("C: ${food.carbs}g", color = CyanCarbs, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("F: ${food.fat}g", color = RoseFat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (food.gymTip.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = food.gymTip,
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Selected Food Servings & Add Bar
                selectedFood?.let { food ->
                    val computedCal = (food.calories * servingsMultiplier).toLong()
                    val computedP = (food.protein * servingsMultiplier).toLong()
                    val computedC = (food.carbs * servingsMultiplier).toLong()
                    val computedF = (food.fat * servingsMultiplier).toLong()

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = GymSurface,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberPrimary)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = food.name,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = {
                                            if (servingsMultiplier > 0.5) servingsMultiplier -= 0.5
                                        }
                                    ) {
                                        Text("-0.5x", color = TextSecondary)
                                    }
                                    Text(
                                        text = "${servingsMultiplier}x",
                                        color = AmberPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    TextButton(
                                        onClick = {
                                            servingsMultiplier += 0.5
                                        }
                                    ) {
                                        Text("+0.5x", color = AmberPrimary)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$computedCal kcal | P:${computedP}g C:${computedC}g F:${computedF}g",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Button(
                                    onClick = {
                                        onConfirmLog(
                                            food.name,
                                            food.servingSize,
                                            servingsMultiplier,
                                            computedCal,
                                            computedP,
                                            computedC,
                                            computedF,
                                            selectedMealType
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmberPrimary,
                                        contentColor = Color(0xFF0F172A)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("confirm_add_food_button")
                                ) {
                                    Text("Add to Log", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomFoodDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, serving: String, cal: Long, p: Long, c: Long, f: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var serving by remember { mutableStateOf("1 serving (100g)") }
    var caloriesText by remember { mutableStateOf("250") }
    var proteinText by remember { mutableStateOf("20") }
    var carbsText by remember { mutableStateOf("25") }
    var fatText by remember { mutableStateOf("5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Food", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name", color = TextSecondary) },
                    placeholder = { Text("e.g. Protein Pancake, Beef Patty", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = GymCardStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = serving,
                    onValueChange = { serving = it },
                    label = { Text("Serving Size Description", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = GymCardStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { caloriesText = it },
                    label = { Text("Calories (kcal)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = GymCardStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = proteinText,
                        onValueChange = { proteinText = it },
                        label = { Text("Protein (g)", color = EmeraldProtein) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldProtein,
                            unfocusedBorderColor = GymCardStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = carbsText,
                        onValueChange = { carbsText = it },
                        label = { Text("Carbs (g)", color = CyanCarbs) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanCarbs,
                            unfocusedBorderColor = GymCardStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = fatText,
                        onValueChange = { fatText = it },
                        label = { Text("Fat (g)", color = RoseFat) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoseFat,
                            unfocusedBorderColor = GymCardStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val cal = caloriesText.toLongOrNull() ?: 200L
                        val p = proteinText.toLongOrNull() ?: 15L
                        val c = carbsText.toLongOrNull() ?: 20L
                        val f = fatText.toLongOrNull() ?: 5L
                        onConfirm(name, serving, cal, p, c, f)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary, contentColor = Color(0xFF0F172A))
            ) {
                Text("Save Food", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = GymSurface
    )
}

@Composable
fun EditTargetsDialog(
    currentCalories: Long,
    currentProtein: Long,
    currentCarbs: Long,
    currentFat: Long,
    onDismiss: () -> Unit,
    onSave: (calories: Long, protein: Long, carbs: Long, fat: Long) -> Unit
) {
    var calText by remember { mutableStateOf("$currentCalories") }
    var pText by remember { mutableStateOf("$currentProtein") }
    var cText by remember { mutableStateOf("$currentCarbs") }
    var fText by remember { mutableStateOf("$currentFat") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customize Daily Macro Goals", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Preset options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            calText = "2700"
                            pText = "180"
                            cText = "320"
                            fText = "75"
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GymSurfaceVariant)
                    ) {
                        Text("Bulk", fontSize = 11.sp, color = AmberPrimary)
                    }
                    Button(
                        onClick = {
                            calText = "2000"
                            pText = "170"
                            cText = "180"
                            fText = "60"
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GymSurfaceVariant)
                    ) {
                        Text("Cut", fontSize = 11.sp, color = CyanCarbs)
                    }
                    Button(
                        onClick = {
                            calText = "2400"
                            pText = "160"
                            cText = "250"
                            fText = "70"
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GymSurfaceVariant)
                    ) {
                        Text("Recomp", fontSize = 11.sp, color = EmeraldProtein)
                    }
                }

                OutlinedTextField(
                    value = calText,
                    onValueChange = { calText = it },
                    label = { Text("Target Calories (kcal)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = GymCardStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = pText,
                        onValueChange = { pText = it },
                        label = { Text("Protein (g)", color = EmeraldProtein) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldProtein,
                            unfocusedBorderColor = GymCardStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = cText,
                        onValueChange = { cText = it },
                        label = { Text("Carbs (g)", color = CyanCarbs) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanCarbs,
                            unfocusedBorderColor = GymCardStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = fText,
                        onValueChange = { fText = it },
                        label = { Text("Fat (g)", color = RoseFat) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoseFat,
                            unfocusedBorderColor = GymCardStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = calText.toLongOrNull() ?: 2400L
                    val p = pText.toLongOrNull() ?: 160L
                    val c = cText.toLongOrNull() ?: 250L
                    val f = fText.toLongOrNull() ?: 70L
                    onSave(cal, p, c, f)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary, contentColor = Color(0xFF0F172A))
            ) {
                Text("Save Targets", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = GymSurface
    )
}
