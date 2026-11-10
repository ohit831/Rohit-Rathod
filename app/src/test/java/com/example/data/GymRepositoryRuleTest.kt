package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.LoggedExercise
import com.example.data.model.MealLog
import com.example.data.model.WorkoutSession
import com.example.data.repository.GymRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class GymRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private fun generateEmail() = "test_${UUID.randomUUID()}@gymtest.com"

    @Test
    fun saveWorkoutSession_validPayload_createsDocument() = runBlocking {
        signInTestUser(generateEmail())
        val repository = GymRepository(firestore)

        val session = WorkoutSession(
            workoutName = "Day 1: Full-Body Squat Focus",
            dayOfWeek = "Monday",
            completedDate = "2026-10-31",
            durationMinutes = 50,
            totalVolumeKg = 2400,
            exercises = listOf(
                LoggedExercise(
                    name = "Barbell Back Squat",
                    targetMuscle = "Quadriceps",
                    setsCount = 3L,
                    repsSummary = "8, 8, 8",
                    weightSummary = "60kg"
                )
            ),
            notes = "Completed all sets with good form."
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveWorkoutSession(session)
        }
        assertTrue("Workout session save should succeed", result.isSuccess)
        val docId = result.getOrThrow()
        assertTrue("Document ID should not be blank", docId.isNotBlank())
    }

    @Test
    fun observeWorkouts_authenticatedOwner_emitsSession() = runBlocking {
        val uid = signInTestUser(generateEmail())
        val repository = GymRepository(firestore)

        val session = WorkoutSession(
            workoutName = "Day 2: Full-Body Hinge Focus",
            dayOfWeek = "Wednesday",
            completedDate = "2026-10-31",
            durationMinutes = 45,
            totalVolumeKg = 1800,
            exercises = listOf(
                LoggedExercise(
                    name = "Romanian Deadlift",
                    targetMuscle = "Hamstrings",
                    setsCount = 3L,
                    repsSummary = "10, 10, 10",
                    weightSummary = "50kg"
                )
            )
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveWorkoutSession(session)
        }
        val docId = saveResult.getOrThrow()

        val emittedList = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeWorkouts(uid).first { list -> list.any { it.id == docId } }
        }
        assertTrue("Emitted list should contain created workout", emittedList.any { it.id == docId })
    }

    @Test
    fun logMeal_authenticatedUser_savesSuccessfully() = runBlocking {
        signInTestUser(generateEmail())
        val repository = GymRepository(firestore)

        val meal = MealLog(
            date = "2026-10-31",
            mealType = "lunch",
            foodName = "Grilled Chicken Breast & Jasmine Rice",
            servingSize = "1 plate",
            servings = 1.0,
            calories = 520,
            protein = 45,
            carbs = 60,
            fat = 8
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.logMeal(meal)
        }
        assertTrue("Meal log should succeed", result.isSuccess)
    }

    @Test
    fun observeWorkouts_unauthenticatedUser_failsWithPermissionDenied() {
        auth.signOut()
        val repository = GymRepository(firestore)

        val exception = assertThrows(Exception::class.java) {
            runBlocking {
                withTimeout(FLOW_TIMEOUT_MS) {
                    repository.observeWorkouts("any_user").first()
                }
            }
        }
        val firestoreException = checkNotNull(exception.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $exception"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }

    @Test
    fun crossUserAccess_bobCannotReadAliceWorkouts() = runBlocking {
        val aliceUid = signInTestUser(generateEmail())
        val aliceRepo = GymRepository(firestore)

        val session = WorkoutSession(
            workoutName = "Private Alice Workout",
            dayOfWeek = "Friday",
            completedDate = "2026-10-31"
        )
        val docId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveWorkoutSession(session).getOrThrow()
        }
        assertNotNull(docId)

        // Now Bob signs in and attempts to observe Alice's workouts collection
        signInTestUser(generateEmail())
        val bobRepo = GymRepository(firestore)

        val exception = assertThrows(Exception::class.java) {
            runBlocking {
                withTimeout(FLOW_TIMEOUT_MS) {
                    bobRepo.observeWorkouts(aliceUid).first()
                }
            }
        }
        val firestoreException = checkNotNull(exception.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $exception"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}
