package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GymDataPresets
import com.example.data.model.ActiveExerciseLog
import com.example.data.model.ActiveSetLog
import com.example.data.model.CustomFood
import com.example.data.model.LoggedExercise
import com.example.data.model.MealLog
import com.example.data.model.PresetFood
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSession
import com.example.data.repository.GymRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppTab {
    WORKOUTS,
    CALENDAR,
    NUTRITION
}

class GymViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GymRepository(application.applicationContext)
    private val auth = Firebase.auth
    private val currentUserId: String get() = auth.currentUser?.uid ?: ""

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // --- NAVIGATION STATE ---
    private val _currentTab = MutableStateFlow(AppTab.WORKOUTS)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- FIRESTORE STREAMS ---
    val workoutsState: StateFlow<List<WorkoutSession>> = repository.observeWorkouts(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val mealsState: StateFlow<List<MealLog>> = repository.observeMeals(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val customFoodsState: StateFlow<List<CustomFood>> = repository.observeCustomFoods(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val profileState: StateFlow<UserProfile?> = repository.observeProfile(currentUserId)
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    // --- ACTIVE WORKOUT TRACKER ---
    private val _activeRoutine = MutableStateFlow<WorkoutRoutine?>(null)
    val activeRoutine: StateFlow<WorkoutRoutine?> = _activeRoutine.asStateFlow()

    private val _activeExercises = MutableStateFlow<List<ActiveExerciseLog>>(emptyList())
    val activeExercises: StateFlow<List<ActiveExerciseLog>> = _activeExercises.asStateFlow()

    private val _isWorkoutActive = MutableStateFlow(false)
    val isWorkoutActive: StateFlow<Boolean> = _isWorkoutActive.asStateFlow()

    private val _workoutDurationSeconds = MutableStateFlow(0L)
    val workoutDurationSeconds: StateFlow<Long> = _workoutDurationSeconds.asStateFlow()

    private var timerJob: Job? = null

    // Rest timer
    private val _restSecondsRemaining = MutableStateFlow(0)
    val restSecondsRemaining: StateFlow<Int> = _restSecondsRemaining.asStateFlow()

    private val _isRestTimerRunning = MutableStateFlow(false)
    val isRestTimerRunning: StateFlow<Boolean> = _isRestTimerRunning.asStateFlow()

    private val _activeRestExerciseName = MutableStateFlow("")
    val activeRestExerciseName: StateFlow<String> = _activeRestExerciseName.asStateFlow()

    private var restTimerJob: Job? = null

    fun startWorkout(routine: WorkoutRoutine) {
        _activeRoutine.value = routine
        _activeExercises.value = routine.exercises.map { exercise ->
            ActiveExerciseLog(
                exerciseName = exercise.name,
                targetMuscle = exercise.targetMuscle,
                restSeconds = exercise.restSeconds,
                sets = (1..exercise.sets).map { setIndex ->
                    ActiveSetLog(
                        setNumber = setIndex,
                        weightKg = 30.0,
                        reps = 10,
                        completed = false
                    )
                }
            )
        }
        _workoutDurationSeconds.value = 0L
        _isWorkoutActive.value = true

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _workoutDurationSeconds.value += 1
            }
        }
    }

    fun updateSet(exerciseIndex: Int, setIndex: Int, weight: Double, reps: Int, completed: Boolean) {
        val list = _activeExercises.value.toMutableList()
        if (exerciseIndex in list.indices) {
            val exercise = list[exerciseIndex]
            val sets = exercise.sets.toMutableList()
            if (setIndex in sets.indices) {
                val oldCompleted = sets[setIndex].completed
                sets[setIndex] = sets[setIndex].copy(
                    weightKg = weight,
                    reps = reps,
                    completed = completed
                )
                list[exerciseIndex] = exercise.copy(sets = sets)
                _activeExercises.value = list

                // Automatically start rest timer when set is checked off
                if (!oldCompleted && completed) {
                    startRestTimer(exercise.restSeconds, exercise.exerciseName)
                }
            }
        }
    }

    fun startRestTimer(seconds: Int, exerciseName: String = "") {
        restTimerJob?.cancel()
        _restSecondsRemaining.value = seconds
        _isRestTimerRunning.value = true
        _activeRestExerciseName.value = exerciseName

        restTimerJob = viewModelScope.launch {
            while (_restSecondsRemaining.value > 0) {
                delay(1000L)
                _restSecondsRemaining.value -= 1
            }
            _isRestTimerRunning.value = false
            triggerRestTimerVibration()
        }
    }

    fun addRestSeconds(seconds: Int) {
        val updated = (_restSecondsRemaining.value + seconds).coerceAtLeast(0)
        _restSecondsRemaining.value = updated
        if (!_isRestTimerRunning.value && updated > 0) {
            _isRestTimerRunning.value = true
            startRestTimer(updated, _activeRestExerciseName.value)
        }
    }

    fun skipRestTimer() {
        restTimerJob?.cancel()
        _restSecondsRemaining.value = 0
        _isRestTimerRunning.value = false
    }

    private fun triggerRestTimerVibration() {
        try {
            val context = getApplication<Application>().applicationContext
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 300), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500L)
            }
        } catch (_: Exception) {}
    }

    fun finishWorkout(notes: String = "", onFinished: (Long, Long, Long) -> Unit = { _, _, _ -> }) {
        val routine = _activeRoutine.value ?: return
        val durationMins = (_workoutDurationSeconds.value / 60).coerceAtLeast(1)

        var totalVolume = 0.0
        val loggedExercises = _activeExercises.value.map { activeEx ->
            var exVolume = 0.0
            val completedSets = activeEx.sets.filter { it.completed }
            completedSets.forEach { s ->
                exVolume += (s.weightKg * s.reps)
            }
            totalVolume += exVolume

            val repsSummary = completedSets.joinToString(", ") { "${it.reps}" }
            val weightSummary = completedSets.map { it.weightKg }.maxOrNull()?.let { "${it}kg" } ?: "0kg"

            LoggedExercise(
                name = activeEx.exerciseName,
                targetMuscle = activeEx.targetMuscle,
                setsCount = completedSets.size.toLong(),
                repsSummary = repsSummary.ifBlank { "0 reps" },
                weightSummary = weightSummary
            )
        }

        val todayDate = dateFormat.format(Date())
        val calendar = Calendar.getInstance()
        val dayOfWeekStr = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)

        val session = WorkoutSession(
            userId = currentUserId,
            workoutName = routine.title,
            dayOfWeek = dayOfWeekStr,
            completedDate = todayDate,
            durationMinutes = durationMins,
            totalVolumeKg = totalVolume.toLong(),
            exercises = loggedExercises,
            notes = notes
        )

        viewModelScope.launch {
            repository.saveWorkoutSession(session)
        }

        val completedCount = _activeExercises.value.sumOf { ex -> ex.sets.count { it.completed } }

        timerJob?.cancel()
        restTimerJob?.cancel()
        _isWorkoutActive.value = false
        _isRestTimerRunning.value = false
        _activeRoutine.value = null

        onFinished(totalVolume.toLong(), completedCount.toLong(), durationMins)
    }

    fun cancelWorkout() {
        timerJob?.cancel()
        restTimerJob?.cancel()
        _isWorkoutActive.value = false
        _isRestTimerRunning.value = false
        _activeRoutine.value = null
        _activeExercises.value = emptyList()
    }

    // --- CALENDAR & HISTORY STATE ---
    private val calendarInstance = Calendar.getInstance()

    private val _calendarYear = MutableStateFlow(calendarInstance.get(Calendar.YEAR))
    val calendarYear: StateFlow<Int> = _calendarYear.asStateFlow()

    private val _calendarMonth = MutableStateFlow(calendarInstance.get(Calendar.MONTH)) // 0-based
    val calendarMonth: StateFlow<Int> = _calendarMonth.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    fun selectCalendarDate(dateStr: String) {
        _selectedCalendarDate.value = dateStr
    }

    fun previousMonth() {
        if (_calendarMonth.value == 0) {
            _calendarMonth.value = 11
            _calendarYear.value -= 1
        } else {
            _calendarMonth.value -= 1
        }
    }

    fun nextMonth() {
        if (_calendarMonth.value == 11) {
            _calendarMonth.value = 0
            _calendarYear.value += 1
        } else {
            _calendarMonth.value += 1
        }
    }

    fun logPastWorkout(
        workoutName: String,
        dateStr: String,
        durationMinutes: Long,
        totalVolumeKg: Long,
        notes: String
    ) {
        val session = WorkoutSession(
            userId = currentUserId,
            workoutName = workoutName,
            completedDate = dateStr,
            durationMinutes = durationMinutes,
            totalVolumeKg = totalVolumeKg,
            notes = notes
        )
        viewModelScope.launch {
            repository.saveWorkoutSession(session)
        }
    }

    fun deleteWorkoutSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteWorkoutSession(sessionId)
        }
    }

    // --- NUTRITION STATE ---
    private val _nutritionDate = MutableStateFlow(dateFormat.format(Date()))
    val nutritionDate: StateFlow<String> = _nutritionDate.asStateFlow()

    private val _foodSearchQuery = MutableStateFlow("")
    val foodSearchQuery: StateFlow<String> = _foodSearchQuery.asStateFlow()

    private val _foodCategoryFilter = MutableStateFlow("All")
    val foodCategoryFilter: StateFlow<String> = _foodCategoryFilter.asStateFlow()

    fun setNutritionDate(dateStr: String) {
        _nutritionDate.value = dateStr
    }

    fun shiftNutritionDate(deltaDays: Int) {
        try {
            val current = dateFormat.parse(_nutritionDate.value) ?: Date()
            val cal = Calendar.getInstance().apply { time = current }
            cal.add(Calendar.DATE, deltaDays)
            _nutritionDate.value = dateFormat.format(cal.time)
        } catch (_: Exception) {}
    }

    fun setFoodSearchQuery(query: String) {
        _foodSearchQuery.value = query
    }

    fun setFoodCategoryFilter(cat: String) {
        _foodCategoryFilter.value = cat
    }

    fun logFood(
        foodName: String,
        servingSize: String,
        servings: Double,
        calories: Long,
        protein: Long,
        carbs: Long,
        fat: Long,
        mealType: String
    ) {
        val meal = MealLog(
            userId = currentUserId,
            date = _nutritionDate.value,
            mealType = mealType,
            foodName = foodName,
            servingSize = servingSize,
            servings = servings,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat
        )
        viewModelScope.launch {
            repository.logMeal(meal)
        }
    }

    fun logMealMacros(
        mealType: String,
        foodName: String,
        protein: Long,
        carbs: Long,
        fat: Long,
        calories: Long? = null
    ) {
        val calculatedCal = calories ?: ((protein * 4L) + (carbs * 4L) + (fat * 9L))
        val normalizedMealType = mealType.lowercase().trim().let {
            if (it in listOf("breakfast", "lunch", "dinner", "snack")) it else "lunch"
        }
        val safeName = foodName.trim().ifBlank {
            "${normalizedMealType.replaceFirstChar { it.uppercase() }} Macros"
        }.take(100)

        val meal = MealLog(
            userId = currentUserId,
            date = _nutritionDate.value,
            mealType = normalizedMealType,
            foodName = safeName,
            servingSize = "1 serving",
            servings = 1.0,
            calories = calculatedCal.coerceIn(0L, 10000L),
            protein = protein.coerceIn(0L, 1000L),
            carbs = carbs.coerceIn(0L, 1000L),
            fat = fat.coerceIn(0L, 1000L)
        )
        viewModelScope.launch {
            repository.logMeal(meal)
        }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            repository.deleteMeal(mealId)
        }
    }

    fun saveCustomFood(
        name: String,
        serving: String,
        calories: Long,
        protein: Long,
        carbs: Long,
        fat: Long
    ) {
        val food = CustomFood(
            userId = currentUserId,
            name = name,
            serving = serving,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat
        )
        viewModelScope.launch {
            repository.saveCustomFood(food)
        }
    }

    fun deleteCustomFood(foodId: String) {
        viewModelScope.launch {
            repository.deleteCustomFood(foodId)
        }
    }

    fun updateProfileTargets(
        calories: Long,
        protein: Long,
        carbs: Long,
        fat: Long,
        experienceLevel: String
    ) {
        val profile = UserProfile(
            userId = currentUserId,
            dailyCalorieTarget = calories,
            dailyProteinTarget = protein,
            dailyCarbsTarget = carbs,
            dailyFatTarget = fat,
            experienceLevel = experienceLevel
        )
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }
}
