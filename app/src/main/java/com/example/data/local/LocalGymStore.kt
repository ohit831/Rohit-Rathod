package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CustomFood
import com.example.data.model.LoggedExercise
import com.example.data.model.MealLog
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutSession
import org.json.JSONArray
import org.json.JSONObject

class LocalGymStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("apex_gym_offline_store", Context.MODE_PRIVATE)

    fun loadWorkouts(): List<WorkoutSession> {
        val raw = prefs.getString("saved_workouts", null)
        if (raw == null) {
            val defaults = generateStarterWorkouts()
            saveWorkouts(defaults)
            return defaults
        }
        return try {
            val array = JSONArray(raw)
            val result = mutableListOf<WorkoutSession>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val exArray = obj.optJSONArray("exercises") ?: JSONArray()
                val exercises = mutableListOf<LoggedExercise>()
                for (j in 0 until exArray.length()) {
                    val exObj = exArray.getJSONObject(j)
                    exercises.add(
                        LoggedExercise(
                            name = exObj.optString("name", ""),
                            targetMuscle = exObj.optString("targetMuscle", ""),
                            setsCount = exObj.optLong("setsCount", 0L),
                            repsSummary = exObj.optString("repsSummary", ""),
                            weightSummary = exObj.optString("weightSummary", "")
                        )
                    )
                }
                result.add(
                    WorkoutSession(
                        id = obj.optString("id", ""),
                        userId = obj.optString("userId", "local_user"),
                        workoutName = obj.optString("workoutName", ""),
                        dayOfWeek = obj.optString("dayOfWeek", ""),
                        completedDate = obj.optString("completedDate", ""),
                        durationMinutes = obj.optLong("durationMinutes", 0L),
                        totalVolumeKg = obj.optLong("totalVolumeKg", 0L),
                        exercises = exercises,
                        notes = obj.optString("notes", "")
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveWorkouts(list: List<WorkoutSession>) {
        try {
            val array = JSONArray()
            list.forEach { session ->
                val obj = JSONObject().apply {
                    put("id", session.id)
                    put("userId", session.userId)
                    put("workoutName", session.workoutName)
                    put("dayOfWeek", session.dayOfWeek)
                    put("completedDate", session.completedDate)
                    put("durationMinutes", session.durationMinutes)
                    put("totalVolumeKg", session.totalVolumeKg)
                    put("notes", session.notes)
                    val exArray = JSONArray()
                    session.exercises.forEach { ex ->
                        exArray.put(
                            JSONObject().apply {
                                put("name", ex.name)
                                put("targetMuscle", ex.targetMuscle)
                                put("setsCount", ex.setsCount)
                                put("repsSummary", ex.repsSummary)
                                put("weightSummary", ex.weightSummary)
                            }
                        )
                    }
                    put("exercises", exArray)
                }
                array.put(obj)
            }
            prefs.edit().putString("saved_workouts", array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun loadMeals(): List<MealLog> {
        val raw = prefs.getString("saved_meals", null)
        if (raw == null) {
            val defaults = generateStarterMeals()
            saveMeals(defaults)
            return defaults
        }
        return try {
            val array = JSONArray(raw)
            val result = mutableListOf<MealLog>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    MealLog(
                        id = obj.optString("id", ""),
                        userId = obj.optString("userId", "local_user"),
                        date = obj.optString("date", ""),
                        mealType = obj.optString("mealType", "lunch"),
                        foodName = obj.optString("foodName", ""),
                        servingSize = obj.optString("servingSize", "1 serving"),
                        servings = obj.optDouble("servings", 1.0),
                        calories = obj.optLong("calories", 0L),
                        protein = obj.optLong("protein", 0L),
                        carbs = obj.optLong("carbs", 0L),
                        fat = obj.optLong("fat", 0L)
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveMeals(list: List<MealLog>) {
        try {
            val array = JSONArray()
            list.forEach { meal ->
                val obj = JSONObject().apply {
                    put("id", meal.id)
                    put("userId", meal.userId)
                    put("date", meal.date)
                    put("mealType", meal.mealType)
                    put("foodName", meal.foodName)
                    put("servingSize", meal.servingSize)
                    put("servings", meal.servings)
                    put("calories", meal.calories)
                    put("protein", meal.protein)
                    put("carbs", meal.carbs)
                    put("fat", meal.fat)
                }
                array.put(obj)
            }
            prefs.edit().putString("saved_meals", array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun loadCustomFoods(): List<CustomFood> {
        val raw = prefs.getString("saved_custom_foods", null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val result = mutableListOf<CustomFood>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    CustomFood(
                        id = obj.optString("id", ""),
                        userId = obj.optString("userId", "local_user"),
                        name = obj.optString("name", ""),
                        serving = obj.optString("serving", "1 serving"),
                        calories = obj.optLong("calories", 0L),
                        protein = obj.optLong("protein", 0L),
                        carbs = obj.optLong("carbs", 0L),
                        fat = obj.optLong("fat", 0L)
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCustomFoods(list: List<CustomFood>) {
        try {
            val array = JSONArray()
            list.forEach { food ->
                val obj = JSONObject().apply {
                    put("id", food.id)
                    put("userId", food.userId)
                    put("name", food.name)
                    put("serving", food.serving)
                    put("calories", food.calories)
                    put("protein", food.protein)
                    put("carbs", food.carbs)
                    put("fat", food.fat)
                }
                array.put(obj)
            }
            prefs.edit().putString("saved_custom_foods", array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun loadProfile(): UserProfile {
        val raw = prefs.getString("saved_profile", null) ?: return UserProfile(userId = "local_user")
        return try {
            val obj = JSONObject(raw)
            UserProfile(
                id = "main",
                userId = obj.optString("userId", "local_user"),
                dailyCalorieTarget = obj.optLong("dailyCalorieTarget", 2400L),
                dailyProteinTarget = obj.optLong("dailyProteinTarget", 160L),
                dailyCarbsTarget = obj.optLong("dailyCarbsTarget", 250L),
                dailyFatTarget = obj.optLong("dailyFatTarget", 70L),
                experienceLevel = obj.optString("experienceLevel", "Beginner")
            )
        } catch (_: Exception) {
            UserProfile(userId = "local_user")
        }
    }

    fun saveProfile(profile: UserProfile) {
        try {
            val obj = JSONObject().apply {
                put("id", "main")
                put("userId", profile.userId)
                put("dailyCalorieTarget", profile.dailyCalorieTarget)
                put("dailyProteinTarget", profile.dailyProteinTarget)
                put("dailyCarbsTarget", profile.dailyCarbsTarget)
                put("dailyFatTarget", profile.dailyFatTarget)
                put("experienceLevel", profile.experienceLevel)
            }
            prefs.edit().putString("saved_profile", obj.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun generateStarterWorkouts(): List<WorkoutSession> {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()

        // 5 days ago: Day 1 Squat & Push (3,840 kg)
        cal.time = java.util.Date()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -5)
        val d5 = dateFormat.format(cal.time)

        // 3 days ago: Day 2 Hinge & Pull (4,200 kg)
        cal.time = java.util.Date()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -3)
        val d3 = dateFormat.format(cal.time)

        // 1 day ago: Day 3 Leg Press & Upper (4,650 kg)
        cal.time = java.util.Date()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val d1 = dateFormat.format(cal.time)

        return listOf(
            WorkoutSession(
                id = "sample_w1",
                userId = "local_user",
                workoutName = "Day 1: Full-Body (Squat & Push Focus)",
                dayOfWeek = "Day 1",
                completedDate = d5,
                durationMinutes = 48,
                totalVolumeKg = 3840L,
                exercises = listOf(
                    LoggedExercise(name = "Barbell Back Squat", targetMuscle = "Quads & Glutes", setsCount = 3, repsSummary = "10, 10, 10", weightSummary = "60 kg"),
                    LoggedExercise(name = "Barbell Bench Press", targetMuscle = "Chest & Triceps", setsCount = 3, repsSummary = "10, 10, 8", weightSummary = "50 kg"),
                    LoggedExercise(name = "Seated Cable Row", targetMuscle = "Lats & Back", setsCount = 3, repsSummary = "12, 12, 12", weightSummary = "40 kg")
                ),
                notes = "Felt strong on squats, solid depth achieved."
            ),
            WorkoutSession(
                id = "sample_w2",
                userId = "local_user",
                workoutName = "Day 2: Full-Body (Hinge & Pull Focus)",
                dayOfWeek = "Day 2",
                completedDate = d3,
                durationMinutes = 52,
                totalVolumeKg = 4200L,
                exercises = listOf(
                    LoggedExercise(name = "Romanian Deadlift", targetMuscle = "Hamstrings & Glutes", setsCount = 3, repsSummary = "10, 10, 10", weightSummary = "70 kg"),
                    LoggedExercise(name = "Lat Pulldown", targetMuscle = "Lats", setsCount = 3, repsSummary = "10, 10, 10", weightSummary = "45 kg"),
                    LoggedExercise(name = "Incline Dumbbell Press", targetMuscle = "Upper Chest", setsCount = 3, repsSummary = "10, 10, 10", weightSummary = "35 kg")
                ),
                notes = "Good hip hinge form, hamstring stretch felt great."
            ),
            WorkoutSession(
                id = "sample_w3",
                userId = "local_user",
                workoutName = "Day 3: Full-Body (Leg Press & Upper Hypertrophy)",
                dayOfWeek = "Day 3",
                completedDate = d1,
                durationMinutes = 55,
                totalVolumeKg = 4650L,
                exercises = listOf(
                    LoggedExercise(name = "Incline Leg Press", targetMuscle = "Quads & Glutes", setsCount = 3, repsSummary = "12, 12, 12", weightSummary = "100 kg"),
                    LoggedExercise(name = "Overhead Dumbbell Press", targetMuscle = "Shoulders", setsCount = 3, repsSummary = "10, 10, 10", weightSummary = "25 kg"),
                    LoggedExercise(name = "Dumbbell Hammer Curls", targetMuscle = "Biceps & Forearms", setsCount = 3, repsSummary = "12, 12, 12", weightSummary = "15 kg")
                ),
                notes = "Hit PR on leg press! Great week 1 progression."
            )
        )
    }

    private fun generateStarterMeals(): List<MealLog> {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        val list = mutableListOf<MealLog>()

        for (i in 0..6) {
            cal.time = java.util.Date()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val d = dateFormat.format(cal.time)

            list.add(
                MealLog(
                    id = "sample_m_b_$i",
                    userId = "local_user",
                    date = d,
                    mealType = "breakfast",
                    foodName = "Rolled Oats & Whey Protein Bowl",
                    servingSize = "80g oats + 30g whey",
                    servings = 1.0,
                    calories = 430,
                    protein = 36,
                    carbs = 56,
                    fat = 7
                )
            )
            list.add(
                MealLog(
                    id = "sample_m_l_$i",
                    userId = "local_user",
                    date = d,
                    mealType = "lunch",
                    foodName = "Grilled Chicken Breast with Brown Rice",
                    servingSize = "200g chicken + 1 cup rice",
                    servings = 1.0,
                    calories = 540,
                    protein = 58,
                    carbs = 48,
                    fat = 8
                )
            )
            list.add(
                MealLog(
                    id = "sample_m_d_$i",
                    userId = "local_user",
                    date = d,
                    mealType = "dinner",
                    foodName = "Lean Ground Beef Bowl with Sweet Potato",
                    servingSize = "180g beef + 200g potato",
                    servings = 1.0,
                    calories = 590,
                    protein = 46,
                    carbs = 54,
                    fat = 16
                )
            )
            list.add(
                MealLog(
                    id = "sample_m_s_$i",
                    userId = "local_user",
                    date = d,
                    mealType = "snack",
                    foodName = "Nonfat Greek Yogurt & Almonds",
                    servingSize = "1 cup yogurt + 1 oz almonds",
                    servings = 1.0,
                    calories = 290,
                    protein = 28,
                    carbs = 14,
                    fat = 14
                )
            )
        }
        return list
    }
}
