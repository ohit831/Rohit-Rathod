package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class LoggedExercise(
    var name: String = "",
    var targetMuscle: String = "",
    var setsCount: Long = 0L,
    var repsSummary: String = "",
    var weightSummary: String = ""
)

@IgnoreExtraProperties
data class WorkoutSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val workoutName: String = "",
    val dayOfWeek: String = "",
    val completedDate: String = "", // YYYY-MM-DD
    val durationMinutes: Long = 0L,
    val totalVolumeKg: Long = 0L,
    val exercises: List<LoggedExercise> = emptyList(),
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MealLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val date: String = "", // YYYY-MM-DD
    val mealType: String = "lunch", // "breakfast", "lunch", "dinner", "snack"
    val foodName: String = "",
    val servingSize: String = "",
    val servings: Double = 1.0,
    val calories: Long = 0L,
    val protein: Long = 0L,
    val carbs: Long = 0L,
    val fat: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CustomFood(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val serving: String = "1 serving",
    val calories: Long = 0L,
    val protein: Long = 0L,
    val carbs: Long = 0L,
    val fat: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dailyCalorieTarget: Long = 2400L,
    val dailyProteinTarget: Long = 160L,
    val dailyCarbsTarget: Long = 250L,
    val dailyFatTarget: Long = 70L,
    val experienceLevel: String = "Beginner",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

// In-app static food database item
data class PresetFood(
    val id: String,
    val name: String,
    val category: String,
    val servingSize: String,
    val calories: Long,
    val protein: Long,
    val carbs: Long,
    val fat: Long,
    val gymTip: String = ""
)

// In-app beginner workout plan definitions
data class RoutineExercise(
    val id: String,
    val name: String,
    val targetMuscle: String,
    val sets: Int,
    val repsRange: String,
    val restSeconds: Int,
    val equipment: String,
    val formTips: String
)

data class WorkoutRoutine(
    val id: String,
    val title: String,
    val tag: String,
    val description: String,
    val targetDays: String,
    val estimatedMinutes: Int,
    val exercises: List<RoutineExercise>
)

data class ActiveExerciseLog(
    val exerciseName: String,
    val targetMuscle: String,
    val restSeconds: Int,
    val sets: List<ActiveSetLog>
)

data class ActiveSetLog(
    val setNumber: Int,
    var weightKg: Double = 20.0,
    var reps: Int = 10,
    var completed: Boolean = false
)
