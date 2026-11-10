package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.local.LocalGymStore
import com.example.data.model.CustomFood
import com.example.data.model.MealLog
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutSession
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GymRepository(
    private val db: FirebaseFirestore,
    context: Context? = null
) {
    private val localStore = context?.let { LocalGymStore(it) }

    private val _localWorkouts = MutableStateFlow<List<WorkoutSession>>(localStore?.loadWorkouts() ?: emptyList())
    private val _localMeals = MutableStateFlow<List<MealLog>>(localStore?.loadMeals() ?: emptyList())
    private val _localCustomFoods = MutableStateFlow<List<CustomFood>>(localStore?.loadCustomFoods() ?: emptyList())
    private val _localProfile = MutableStateFlow<UserProfile?>(localStore?.loadProfile() ?: UserProfile(userId = "local_user"))

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        context
    )

    init {
        // Configure unlimited persistent local disk cache for complete offline access
        try {
            db.firestoreSettings = FirebaseFirestoreSettings.Builder(db.firestoreSettings)
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()
                )
                .build()
        } catch (_: Exception) {
            // Settings already locked or customized by emulator test harness
        }
    }

    private val auth = Firebase.auth

    private fun currentUserId(): String? {
        return auth.currentUser?.uid
    }

    // --- WORKOUT SESSIONS ---

    fun observeWorkouts(userId: String = currentUserId() ?: "local_user"): Flow<List<WorkoutSession>> {
        val uid = currentUserId()
        return if (uid != null) {
            flow {
                val path = "users/$uid/workouts"
                emitAll(
                    db.collection("users").document(uid).collection("workouts")
                        .snapshots()
                        .map { snapshot ->
                            val remote = snapshot.toObjects(WorkoutSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            // Update local store with remote data
                            _localWorkouts.value = remote
                            localStore?.saveWorkouts(remote)
                            remote
                        }
                        .catch { error ->
                            if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                            emit(_localWorkouts.value)
                        }
                )
            }
        } else {
            _localWorkouts.asStateFlow()
        }
    }

    suspend fun saveWorkoutSession(session: WorkoutSession): Result<String> {
        val docId = if (session.id.isNotBlank()) session.id else UUID.randomUUID().toString()
        val uid = currentUserId() ?: "local_user"
        val savedSession = session.copy(id = docId, userId = uid)

        // Always save locally immediately
        val updated = _localWorkouts.value.filter { it.id != docId } + savedSession
        _localWorkouts.value = updated
        localStore?.saveWorkouts(updated)

        val currentUid = currentUserId()
        if (currentUid != null) {
            val docRef = db.collection("users").document(currentUid).collection("workouts").document(docId)
            try {
                val payload = hashMapOf<String, Any>(
                    "id" to docId,
                    "userId" to currentUid,
                    "workoutName" to savedSession.workoutName,
                    "dayOfWeek" to savedSession.dayOfWeek,
                    "completedDate" to savedSession.completedDate,
                    "durationMinutes" to savedSession.durationMinutes,
                    "totalVolumeKg" to savedSession.totalVolumeKg,
                    "exercises" to savedSession.exercises.map {
                        mapOf(
                            "name" to it.name,
                            "targetMuscle" to it.targetMuscle,
                            "setsCount" to it.setsCount,
                            "repsSummary" to it.repsSummary,
                            "weightSummary" to it.weightSummary
                        )
                    },
                    "notes" to savedSession.notes,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
            }
        }

        return Result.success(docId)
    }

    suspend fun deleteWorkoutSession(sessionId: String): Result<Unit> {
        val updated = _localWorkouts.value.filter { it.id != sessionId }
        _localWorkouts.value = updated
        localStore?.saveWorkouts(updated)

        val currentUid = currentUserId()
        if (currentUid != null) {
            val docRef = db.collection("users").document(currentUid).collection("workouts").document(sessionId)
            try {
                docRef.delete().await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.DELETE, docRef.path)
            }
        }

        return Result.success(Unit)
    }

    // --- MEAL LOGS ---

    fun observeMeals(userId: String = currentUserId() ?: "local_user"): Flow<List<MealLog>> {
        val uid = currentUserId()
        return if (uid != null) {
            flow {
                val path = "users/$uid/meals"
                emitAll(
                    db.collection("users").document(uid).collection("meals")
                        .snapshots()
                        .map { snapshot ->
                            val remote = snapshot.toObjects(MealLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            _localMeals.value = remote
                            localStore?.saveMeals(remote)
                            remote
                        }
                        .catch { error ->
                            if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                            emit(_localMeals.value)
                        }
                )
            }
        } else {
            _localMeals.asStateFlow()
        }
    }

    suspend fun logMeal(meal: MealLog): Result<String> {
        val docId = if (meal.id.isNotBlank()) meal.id else UUID.randomUUID().toString()
        val uid = currentUserId() ?: "local_user"
        val savedMeal = meal.copy(id = docId, userId = uid)

        val updated = _localMeals.value.filter { it.id != docId } + savedMeal
        _localMeals.value = updated
        localStore?.saveMeals(updated)

        val currentUid = currentUserId()
        if (currentUid != null) {
            val docRef = db.collection("users").document(currentUid).collection("meals").document(docId)
            try {
                val payload = hashMapOf<String, Any>(
                    "id" to docId,
                    "userId" to currentUid,
                    "date" to savedMeal.date,
                    "mealType" to savedMeal.mealType,
                    "foodName" to savedMeal.foodName,
                    "servingSize" to savedMeal.servingSize,
                    "servings" to savedMeal.servings,
                    "calories" to savedMeal.calories,
                    "protein" to savedMeal.protein,
                    "carbs" to savedMeal.carbs,
                    "fat" to savedMeal.fat,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
            }
        }

        return Result.success(docId)
    }

    suspend fun deleteMeal(mealId: String): Result<Unit> {
        val updated = _localMeals.value.filter { it.id != mealId }
        _localMeals.value = updated
        localStore?.saveMeals(updated)

        val currentUid = currentUserId()
        if (currentUid != null) {
            val docRef = db.collection("users").document(currentUid).collection("meals").document(mealId)
            try {
                docRef.delete().await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.DELETE, docRef.path)
            }
        }

        return Result.success(Unit)
    }

    // --- CUSTOM FOODS ---

    fun observeCustomFoods(userId: String = currentUserId() ?: "local_user"): Flow<List<CustomFood>> {
        val uid = currentUserId()
        return if (uid != null) {
            flow {
                val path = "users/$uid/custom_foods"
                emitAll(
                    db.collection("users").document(uid).collection("custom_foods")
                        .snapshots()
                        .map { snapshot ->
                            val remote = snapshot.toObjects(CustomFood::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            _localCustomFoods.value = remote
                            localStore?.saveCustomFoods(remote)
                            remote
                        }
                        .catch { error ->
                            if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                            emit(_localCustomFoods.value)
                        }
                )
            }
        } else {
            _localCustomFoods.asStateFlow()
        }
    }

    suspend fun saveCustomFood(food: CustomFood): Result<String> {
        val docId = if (food.id.isNotBlank()) food.id else UUID.randomUUID().toString()
        val uid = currentUserId() ?: "local_user"
        val savedFood = food.copy(id = docId, userId = uid)

        val updated = _localCustomFoods.value.filter { it.id != docId } + savedFood
        _localCustomFoods.value = updated
        localStore?.saveCustomFoods(updated)

        val currentUid = currentUserId()
        if (currentUid != null) {
            val docRef = db.collection("users").document(currentUid).collection("custom_foods").document(docId)
            try {
                val payload = hashMapOf<String, Any>(
                    "id" to docId,
                    "userId" to currentUid,
                    "name" to savedFood.name,
                    "serving" to savedFood.serving,
                    "calories" to savedFood.calories,
                    "protein" to savedFood.protein,
                    "carbs" to savedFood.carbs,
                    "fat" to savedFood.fat,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
            }
        }

        return Result.success(docId)
    }

    suspend fun deleteCustomFood(foodId: String): Result<Unit> {
        val updated = _localCustomFoods.value.filter { it.id != foodId }
        _localCustomFoods.value = updated
        localStore?.saveCustomFoods(updated)

        val currentUid = currentUserId()
        if (currentUid != null) {
            val docRef = db.collection("users").document(currentUid).collection("custom_foods").document(foodId)
            try {
                docRef.delete().await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.DELETE, docRef.path)
            }
        }

        return Result.success(Unit)
    }

    // --- USER PROFILE ---

    fun observeProfile(userId: String = currentUserId() ?: "local_user"): Flow<UserProfile?> {
        val uid = currentUserId()
        return if (uid != null) {
            flow {
                val path = "users/$uid/profile/main"
                emitAll(
                    db.collection("users").document(uid).collection("profile").document("main")
                        .snapshots()
                        .map { snapshot ->
                            val remote = snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            if (remote != null) {
                                _localProfile.value = remote
                                localStore?.saveProfile(remote)
                            }
                            remote ?: _localProfile.value
                        }
                        .catch { error ->
                            if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                            emit(_localProfile.value)
                        }
                )
            }
        } else {
            _localProfile.asStateFlow()
        }
    }

    suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        val uid = currentUserId() ?: "local_user"
        val saved = profile.copy(userId = uid)
        _localProfile.value = saved
        localStore?.saveProfile(saved)

        val currentUid = currentUserId()
        if (currentUid != null) {
            val docRef = db.collection("users").document(currentUid).collection("profile").document("main")
            try {
                val payload = hashMapOf<String, Any>(
                    "id" to "main",
                    "userId" to currentUid,
                    "dailyCalorieTarget" to saved.dailyCalorieTarget,
                    "dailyProteinTarget" to saved.dailyProteinTarget,
                    "dailyCarbsTarget" to saved.dailyCarbsTarget,
                    "dailyFatTarget" to saved.dailyFatTarget,
                    "experienceLevel" to saved.experienceLevel,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.WRITE, docRef.path)
            }
        }

        return Result.success(Unit)
    }
}
