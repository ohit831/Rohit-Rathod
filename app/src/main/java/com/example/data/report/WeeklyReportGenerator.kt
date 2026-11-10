package com.example.data.report

import com.example.data.model.MealLog
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutSession
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayReportItem(
    val dateString: String,
    val dayOfWeek: String,
    val formattedDate: String,
    val workouts: List<WorkoutSession>,
    val meals: List<MealLog>,
    val dayVolumeKg: Long,
    val dayCalories: Long,
    val dayProtein: Long,
    val dayCarbs: Long,
    val dayFat: Long
)

data class WeeklyReportSummary(
    val title: String,
    val athleteName: String,
    val generatedDate: String,
    val periodRange: String,
    // Workout stats
    val totalWorkouts: Int,
    val totalDurationMinutes: Long,
    val totalVolumeKg: Long,
    val dailyAvgVolumeKg: Long,
    val peakVolumeDayText: String,
    val peakVolumeKg: Long,
    // Macro stats
    val totalCalories: Long,
    val dailyAvgCalories: Long,
    val totalProtein: Long,
    val dailyAvgProtein: Long,
    val totalCarbs: Long,
    val dailyAvgCarbs: Long,
    val totalFat: Long,
    val dailyAvgFat: Long,
    val proteinPercent: Int,
    val carbsPercent: Int,
    val fatPercent: Int,
    // Daily items
    val dayItems: List<DayReportItem>,
    val jsonString: String,
    val textString: String
)

object WeeklyReportGenerator {

    fun generateReport(
        workouts: List<WorkoutSession>,
        meals: List<MealLog>,
        profile: UserProfile? = null,
        daysCount: Int = 7
    ): WeeklyReportSummary {
        val dateFormatKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val now = Date()
        val generatedTimestamp = timestampFormat.format(now)

        // Generate date list from (daysCount - 1) days ago to today
        val dayItems = mutableListOf<DayReportItem>()
        val cal = Calendar.getInstance()

        var startDateStr = ""
        var endDateStr = ""

        for (i in (daysCount - 1) downTo 0) {
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val key = dateFormatKey.format(c.time)
            if (startDateStr.isEmpty()) startDateStr = fullDateFormat.format(c.time)
            if (i == 0) endDateStr = fullDateFormat.format(c.time)

            val dayWorkouts = workouts.filter { it.completedDate == key }
            val dayMeals = meals.filter { it.date == key }

            val dayVol = dayWorkouts.sumOf { it.totalVolumeKg }
            val dayCals = dayMeals.sumOf { it.calories }
            val dayP = dayMeals.sumOf { it.protein }
            val dayC = dayMeals.sumOf { it.carbs }
            val dayF = dayMeals.sumOf { it.fat }

            dayItems.add(
                DayReportItem(
                    dateString = key,
                    dayOfWeek = dayNameFormat.format(c.time),
                    formattedDate = fullDateFormat.format(c.time),
                    workouts = dayWorkouts,
                    meals = dayMeals,
                    dayVolumeKg = dayVol,
                    dayCalories = dayCals,
                    dayProtein = dayP,
                    dayCarbs = dayC,
                    dayFat = dayF
                )
            )
        }

        val periodRange = "$startDateStr - $endDateStr"

        // Workout aggregations
        val totalWorkouts = dayItems.sumOf { it.workouts.size }
        val totalDurationMinutes = dayItems.flatMap { it.workouts }.sumOf { it.durationMinutes }
        val totalVolumeKg = dayItems.sumOf { it.dayVolumeKg }
        val dailyAvgVolumeKg = if (dayItems.isNotEmpty()) totalVolumeKg / dayItems.size else 0L

        val peakDayItem = dayItems.maxByOrNull { it.dayVolumeKg }
        val peakVolumeKg = peakDayItem?.dayVolumeKg ?: 0L
        val peakVolumeDayText = if (peakVolumeKg > 0 && peakDayItem != null) {
            "${peakDayItem.dayOfWeek} (${peakDayItem.formattedDate}) • ${peakVolumeKg} kg"
        } else {
            "No workouts logged in period"
        }

        // Nutrition aggregations
        val totalCalories = dayItems.sumOf { it.dayCalories }
        val dailyAvgCalories = if (dayItems.isNotEmpty()) totalCalories / dayItems.size else 0L
        val totalProtein = dayItems.sumOf { it.dayProtein }
        val dailyAvgProtein = if (dayItems.isNotEmpty()) totalProtein / dayItems.size else 0L
        val totalCarbs = dayItems.sumOf { it.dayCarbs }
        val dailyAvgCarbs = if (dayItems.isNotEmpty()) totalCarbs / dayItems.size else 0L
        val totalFat = dayItems.sumOf { it.dayFat }
        val dailyAvgFat = if (dayItems.isNotEmpty()) totalFat / dayItems.size else 0L

        val totalMacroCals = (totalProtein * 4) + (totalCarbs * 4) + (totalFat * 9)
        val proteinPct = if (totalMacroCals > 0) ((totalProtein * 4 * 100) / totalMacroCals).toInt() else 30
        val carbsPct = if (totalMacroCals > 0) ((totalCarbs * 4 * 100) / totalMacroCals).toInt() else 45
        val fatPct = if (totalMacroCals > 0) ((totalFat * 9 * 100) / totalMacroCals).toInt() else 25

        // BUILD JSON STRING
        val rootJson = JSONObject().apply {
            put("reportTitle", "Apex Gym Weekly Summary Report")
            put("athleteId", profile?.userId?.ifBlank { "local_athlete" } ?: "local_athlete")
            put("experienceLevel", profile?.experienceLevel ?: "Beginner")
            put("generatedAt", generatedTimestamp)
            put("periodRange", periodRange)
            put("windowDays", daysCount)

            // Workouts block
            val workoutsJson = JSONObject().apply {
                put("totalCompletedWorkouts", totalWorkouts)
                put("totalTrainingTimeMinutes", totalDurationMinutes)
                put("totalVolumeLiftedKg", totalVolumeKg)
                put("dailyAverageVolumeKg", dailyAvgVolumeKg)
                put("peakVolumeDay", peakVolumeDayText)
                put("peakVolumeKg", peakVolumeKg)

                val sessionsArray = JSONArray()
                dayItems.flatMap { it.workouts }.forEach { session ->
                    sessionsArray.put(
                        JSONObject().apply {
                            put("workoutName", session.workoutName)
                            put("completedDate", session.completedDate)
                            put("durationMinutes", session.durationMinutes)
                            put("totalVolumeKg", session.totalVolumeKg)
                            put("notes", session.notes)
                            val exercisesArray = JSONArray()
                            session.exercises.forEach { ex ->
                                exercisesArray.put(
                                    JSONObject().apply {
                                        put("name", ex.name)
                                        put("targetMuscle", ex.targetMuscle)
                                        put("setsCount", ex.setsCount)
                                        put("repsSummary", ex.repsSummary)
                                        put("weightSummary", ex.weightSummary)
                                    }
                                )
                            }
                            put("exercises", exercisesArray)
                        }
                    )
                }
                put("sessions", sessionsArray)
            }
            put("workoutsSummary", workoutsJson)

            // Nutrition block
            val nutritionJson = JSONObject().apply {
                put("totalEnergyIntakeKcal", totalCalories)
                put("dailyAverageCaloriesKcal", dailyAvgCalories)
                put("targetCaloriesKcal", profile?.dailyCalorieTarget ?: 2400L)
                put("totalProteinGrams", totalProtein)
                put("dailyAverageProteinGrams", dailyAvgProtein)
                put("targetProteinGrams", profile?.dailyProteinTarget ?: 160L)
                put("totalCarbsGrams", totalCarbs)
                put("dailyAverageCarbsGrams", dailyAvgCarbs)
                put("targetCarbsGrams", profile?.dailyCarbsTarget ?: 250L)
                put("totalFatGrams", totalFat)
                put("dailyAverageFatGrams", dailyAvgFat)
                put("targetFatGrams", profile?.dailyFatTarget ?: 70L)

                put(
                    "macroCaloriePercentages",
                    JSONObject().apply {
                        put("proteinPercent", proteinPct)
                        put("carbsPercent", carbsPct)
                        put("fatPercent", fatPct)
                    }
                )

                val dailyArray = JSONArray()
                dayItems.forEach { day ->
                    dailyArray.put(
                        JSONObject().apply {
                            put("date", day.dateString)
                            put("dayOfWeek", day.dayOfWeek)
                            put("totalCalories", day.dayCalories)
                            put("proteinGrams", day.dayProtein)
                            put("carbsGrams", day.dayCarbs)
                            put("fatGrams", day.dayFat)
                            put("mealsCount", day.meals.size)
                        }
                    )
                }
                put("dailyBreakdown", dailyArray)

                val mealsArray = JSONArray()
                dayItems.flatMap { it.meals }.forEach { meal ->
                    mealsArray.put(
                        JSONObject().apply {
                            put("date", meal.date)
                            put("mealType", meal.mealType)
                            put("foodName", meal.foodName)
                            put("servingSize", meal.servingSize)
                            put("servings", meal.servings)
                            put("calories", meal.calories)
                            put("proteinGrams", meal.protein)
                            put("carbsGrams", meal.carbs)
                            put("fatGrams", meal.fat)
                        }
                    )
                }
                put("loggedMeals", mealsArray)
            }
            put("nutritionSummary", nutritionJson)
        }

        val jsonString = rootJson.toString(2)

        // BUILD TEXT / MARKDOWN STRING
        val sb = StringBuilder()
        sb.appendLine("================================================================================")
        sb.appendLine("                       APEX GYM - WEEKLY SUMMARY REPORT                         ")
        sb.appendLine("================================================================================")
        sb.appendLine("Generated At : $generatedTimestamp")
        sb.appendLine("Report Window: $periodRange (Last $daysCount Days)")
        sb.appendLine("Athlete ID   : ${profile?.userId?.ifBlank { "local_athlete" } ?: "local_athlete"}")
        sb.appendLine("Program Level: ${profile?.experienceLevel ?: "Beginner"}")
        sb.appendLine()
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("1. WORKOUTS & STRENGTH TRAINING SUMMARY")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("• Completed Sessions : $totalWorkouts workouts")
        sb.appendLine("• Total Training Time: $totalDurationMinutes minutes (~${String.format(Locale.US, "%.1f", totalDurationMinutes / 60.0)} hrs)")
        sb.appendLine("• Total Volume Lifted: $totalVolumeKg kg")
        sb.appendLine("• Daily Average Vol  : $dailyAvgVolumeKg kg / day")
        sb.appendLine("• Peak Lifting Day   : $peakVolumeDayText")
        sb.appendLine()
        sb.appendLine("[Workout Sessions Breakdown]")
        val allWorkouts = dayItems.flatMap { it.workouts }
        if (allWorkouts.isEmpty()) {
            sb.appendLine("  (No workout sessions logged in this 7-day period)")
        } else {
            allWorkouts.forEachIndexed { idx, s ->
                sb.appendLine("  #${idx + 1} | ${s.completedDate} — ${s.workoutName}")
                sb.appendLine("       Duration: ${s.durationMinutes} mins | Total Volume: ${s.totalVolumeKg} kg")
                if (s.exercises.isNotEmpty()) {
                    sb.appendLine("       Exercises:")
                    s.exercises.forEach { ex ->
                        sb.appendLine("         - ${ex.name}: ${ex.setsCount} sets (${ex.repsSummary}) @ ${ex.weightSummary}")
                    }
                }
                if (s.notes.isNotBlank()) {
                    sb.appendLine("       Athlete Notes: \"${s.notes}\"")
                }
                sb.appendLine()
            }
        }

        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("2. NUTRITION & MACRONUTRIENT SUMMARY")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("• Total Energy Intake: $totalCalories kcal")
        sb.appendLine("• Daily Average Cals : $dailyAvgCalories kcal / day (Target: ${profile?.dailyCalorieTarget ?: 2400L} kcal)")
        sb.appendLine("• Daily Average P    : $dailyAvgProtein g / day (Target: ${profile?.dailyProteinTarget ?: 160L} g) [$proteinPct% of kcal]")
        sb.appendLine("• Daily Average C    : $dailyAvgCarbs g / day (Target: ${profile?.dailyCarbsTarget ?: 250L} g) [$carbsPct% of kcal]")
        sb.appendLine("• Daily Average F    : $dailyAvgFat g / day (Target: ${profile?.dailyFatTarget ?: 70L} g)  [$fatPct% of kcal]")
        sb.appendLine()
        sb.appendLine("[Day-by-Day Macro Breakdown]")
        dayItems.forEach { day ->
            sb.appendLine(
                String.format(
                    Locale.US,
                    "  %-10s (%3s): %5d kcal | P: %3dg | C: %3dg | F: %3dg (%d meals logged)",
                    day.dateString,
                    day.dayOfWeek,
                    day.dayCalories,
                    day.dayProtein,
                    day.dayCarbs,
                    day.dayFat,
                    day.meals.size
                )
            )
        }
        sb.appendLine()
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("3. PROGRESSIVE OVERLOAD & COACHING INSIGHTS")
        sb.appendLine("--------------------------------------------------------------------------------")
        if (totalWorkouts >= 3) {
            sb.appendLine("✓ Frequency Goal Met: Completed $totalWorkouts of 3 targeted sessions.")
        } else {
            sb.appendLine("! Frequency Note: Logged $totalWorkouts sessions. Target is 3 workouts per week.")
        }
        if (dailyAvgProtein >= 150) {
            sb.appendLine("✓ Protein Intake Optimal: Averaging ${dailyAvgProtein}g protein daily promotes muscle hypertrophy.")
        } else {
            sb.appendLine("! Protein Target: Averaging ${dailyAvgProtein}g protein. Aim for ~160g to maximize recovery.")
        }
        if (totalVolumeKg > 0) {
            sb.appendLine("✓ Volume Progression: Total $totalVolumeKg kg moved across target compound lifts.")
        }
        sb.appendLine("================================================================================")
        sb.appendLine("                  Apex Gym Tracker • Ready for Local Export                     ")
        sb.appendLine("================================================================================")

        val textString = sb.toString()

        return WeeklyReportSummary(
            title = "Apex Gym Weekly Summary",
            athleteName = profile?.userId?.ifBlank { "local_athlete" } ?: "local_athlete",
            generatedDate = generatedTimestamp,
            periodRange = periodRange,
            totalWorkouts = totalWorkouts,
            totalDurationMinutes = totalDurationMinutes,
            totalVolumeKg = totalVolumeKg,
            dailyAvgVolumeKg = dailyAvgVolumeKg,
            peakVolumeDayText = peakVolumeDayText,
            peakVolumeKg = peakVolumeKg,
            totalCalories = totalCalories,
            dailyAvgCalories = dailyAvgCalories,
            totalProtein = totalProtein,
            dailyAvgProtein = dailyAvgProtein,
            totalCarbs = totalCarbs,
            dailyAvgCarbs = dailyAvgCarbs,
            totalFat = totalFat,
            dailyAvgFat = dailyAvgFat,
            proteinPercent = proteinPct,
            carbsPercent = carbsPct,
            fatPercent = fatPct,
            dayItems = dayItems,
            jsonString = jsonString,
            textString = textString
        )
    }
}
