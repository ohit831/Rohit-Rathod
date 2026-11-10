package com.example.data

import com.example.data.model.PresetFood
import com.example.data.model.RoutineExercise
import com.example.data.model.WorkoutRoutine

object GymDataPresets {

    val BEGINNER_FULL_BODY_PLAN: List<WorkoutRoutine> = listOf(
        WorkoutRoutine(
            id = "routine_a",
            title = "Day 1: Full-Body (Squat & Push Focus)",
            tag = "Workout A",
            description = "Primary focus on quad development, horizontal pushing strength, and upper back pulling. Perfect for building foundational compound strength.",
            targetDays = "Monday / Week Start",
            estimatedMinutes = 50,
            exercises = listOf(
                RoutineExercise(
                    id = "e1_squat",
                    name = "Barbell Back Squat",
                    targetMuscle = "Quadriceps & Glutes",
                    sets = 3,
                    repsRange = "8 - 10 reps",
                    restSeconds = 90,
                    equipment = "Barbell & Squat Rack",
                    formTips = "Keep chest upright, brace core before descending, break at hips and knees simultaneously, hit parallel depth."
                ),
                RoutineExercise(
                    id = "e1_bench",
                    name = "Barbell or DB Flat Bench Press",
                    targetMuscle = "Chest & Triceps",
                    sets = 3,
                    repsRange = "8 - 10 reps",
                    restSeconds = 90,
                    equipment = "Flat Bench & Barbell / Dumbbells",
                    formTips = "Plant feet flat on floor, retract shoulder blades, lower bar with control to mid-chest, drive up firmly."
                ),
                RoutineExercise(
                    id = "e1_row",
                    name = "Seated Cable Row",
                    targetMuscle = "Lats & Upper Back",
                    sets = 3,
                    repsRange = "10 - 12 reps",
                    restSeconds = 60,
                    equipment = "Cable Machine (Row attachment)",
                    formTips = "Sit tall without swinging backwards. Pull handles toward your navel, pausing for 1 second at full contraction."
                ),
                RoutineExercise(
                    id = "e1_ohp",
                    name = "Standing Dumbbell Overhead Press",
                    targetMuscle = "Shoulders & Core",
                    sets = 3,
                    repsRange = "10 - 12 reps",
                    restSeconds = 60,
                    equipment = "Dumbbells",
                    formTips = "Squeeze glutes and brace abdominals. Press dumbbells straight upward without overarching your lower back."
                ),
                RoutineExercise(
                    id = "e1_plank",
                    name = "Core Plank Hold",
                    targetMuscle = "Core & Stabilizers",
                    sets = 3,
                    repsRange = "30 - 45 seconds",
                    restSeconds = 45,
                    equipment = "Yoga Mat",
                    formTips = "Maintain a rigid straight line from shoulders to ankles. Squeeze quads and actively pull elbows toward toes."
                )
            )
        ),
        WorkoutRoutine(
            id = "routine_b",
            title = "Day 2: Full-Body (Hinge & Pull Focus)",
            tag = "Workout B",
            description = "Develops posterior chain power (hamstrings and glutes), vertical pulling for lat width, and shoulder stabilization.",
            targetDays = "Wednesday / Mid-Week",
            estimatedMinutes = 50,
            exercises = listOf(
                RoutineExercise(
                    id = "e2_rdl",
                    name = "Romanian Deadlift (RDL)",
                    targetMuscle = "Hamstrings & Glutes",
                    sets = 3,
                    repsRange = "8 - 10 reps",
                    restSeconds = 90,
                    equipment = "Barbell or Dumbbells",
                    formTips = "Slight knee bend, push hips far backwards toward the wall behind you. Keep bar in contact with legs throughout."
                ),
                RoutineExercise(
                    id = "e2_incline",
                    name = "Incline Dumbbell Press",
                    targetMuscle = "Upper Chest & Front Delts",
                    sets = 3,
                    repsRange = "10 - 12 reps",
                    restSeconds = 75,
                    equipment = "Adjustable Incline Bench & Dumbbells",
                    formTips = "Set bench to 30 degrees incline. Lower dumbbells under control, press up in a slight arc."
                ),
                RoutineExercise(
                    id = "e2_latpulldown",
                    name = "Wide-Grip Lat Pulldown",
                    targetMuscle = "Latissimus Dorsi",
                    sets = 3,
                    repsRange = "10 - 12 reps",
                    restSeconds = 60,
                    equipment = "Cable Lat Pulldown Machine",
                    formTips = "Lean back slightly (10-15 degrees), drive elbows down toward your back pockets rather than pulling with wrists."
                ),
                RoutineExercise(
                    id = "e2_lunges",
                    name = "Dumbbell Walking Lunges",
                    targetMuscle = "Quads, Glutes & Balance",
                    sets = 3,
                    repsRange = "10 reps per leg",
                    restSeconds = 60,
                    equipment = "Pair of Dumbbells",
                    formTips = "Take comfortable strides, maintain upright chest, lower back knee gently toward floor with control."
                ),
                RoutineExercise(
                    id = "e2_facepull",
                    name = "Cable Face Pull",
                    targetMuscle = "Rear Delts & Rotator Cuff",
                    sets = 3,
                    repsRange = "12 - 15 reps",
                    restSeconds = 45,
                    equipment = "Cable Machine with Rope",
                    formTips = "Pull rope toward bridge of nose, spreading ends apart to achieve external rotation for posture protection."
                )
            )
        ),
        WorkoutRoutine(
            id = "routine_c",
            title = "Day 3: Full-Body (Volume & Arms Focus)",
            tag = "Workout C",
            description = "High-efficiency leg press, chest hypertrophy, single-arm back work, and direct arm and abdominal training.",
            targetDays = "Friday / Weekend",
            estimatedMinutes = 55,
            exercises = listOf(
                RoutineExercise(
                    id = "e3_legpress",
                    name = "Incline Leg Press Machine",
                    targetMuscle = "Quads & Glutes",
                    sets = 3,
                    repsRange = "10 - 12 reps",
                    restSeconds = 90,
                    equipment = "45-Degree Leg Press",
                    formTips = "Position feet shoulder-width, bring sled down until knees reach 90 degrees, do not lock knees aggressively at top."
                ),
                RoutineExercise(
                    id = "e3_dbbench",
                    name = "Flat Dumbbell Chest Press",
                    targetMuscle = "Pectorals",
                    sets = 3,
                    repsRange = "10 - 12 reps",
                    restSeconds = 75,
                    equipment = "Flat Bench & Dumbbells",
                    formTips = "Provides greater range of motion than barbell. Feel a mild stretch in chest at bottom, squeeze at apex."
                ),
                RoutineExercise(
                    id = "e3_1armrow",
                    name = "Single-Arm Dumbbell Row",
                    targetMuscle = "Lats & Rhomboids",
                    sets = 3,
                    repsRange = "10 reps per side",
                    restSeconds = 60,
                    equipment = "Flat Bench & Single Dumbbell",
                    formTips = "Brace knee and hand on bench. Keep spine neutral, pull elbow back toward hip pocket."
                ),
                RoutineExercise(
                    id = "e3_latraise",
                    name = "Standing Dumbbell Lateral Raise",
                    targetMuscle = "Side Deltoids",
                    sets = 3,
                    repsRange = "12 - 15 reps",
                    restSeconds = 45,
                    equipment = "Light Dumbbells",
                    formTips = "Slight torso forward lean. Lead with elbows, raise out to sides up to shoulder height."
                ),
                RoutineExercise(
                    id = "e3_bicepcurl",
                    name = "Dumbbell Bicep Curl",
                    targetMuscle = "Biceps Brachii",
                    sets = 3,
                    repsRange = "10 - 12 reps",
                    restSeconds = 45,
                    equipment = "Dumbbells",
                    formTips = "Keep elbows pinned to sides, suppress swinging, supinate wrists at top for maximum peak."
                ),
                RoutineExercise(
                    id = "e3_tricep_pushdown",
                    name = "Cable Rope Tricep Pushdown",
                    targetMuscle = "Triceps",
                    sets = 3,
                    repsRange = "10 - 12 reps",
                    restSeconds = 45,
                    equipment = "Cable Machine with Rope",
                    formTips = "Keep upper arms stationary perpendicular to floor. Push down and spread ropes outward at the bottom."
                )
            )
        )
    )

    val PRESET_FOODS: List<PresetFood> = listOf(
        PresetFood(
            id = "f_chicken",
            name = "Chicken Breast (Grilled)",
            category = "Protein",
            servingSize = "100g",
            calories = 165,
            protein = 31,
            carbs = 0,
            fat = 4,
            gymTip = "Gold standard lean gym protein. High leucine content for muscle protein synthesis."
        ),
        PresetFood(
            id = "f_eggs",
            name = "Whole Large Egg",
            category = "Protein",
            servingSize = "1 large (50g)",
            calories = 72,
            protein = 6,
            carbs = 0,
            fat = 5,
            gymTip = "Complete amino acid profile plus choline and micronutrients for hormone support."
        ),
        PresetFood(
            id = "f_egg_whites",
            name = "Liquid Egg Whites",
            category = "Protein",
            servingSize = "100g",
            calories = 52,
            protein = 11,
            carbs = 1,
            fat = 0,
            gymTip = "Virtually pure protein with near-zero fat and calories."
        ),
        PresetFood(
            id = "f_whey",
            name = "Whey Protein Powder",
            category = "Protein",
            servingSize = "1 scoop (30g)",
            calories = 120,
            protein = 24,
            carbs = 3,
            fat = 2,
            gymTip = "Fast-digesting post-workout protein source, convenient and versatile."
        ),
        PresetFood(
            id = "f_tuna",
            name = "Canned Tuna in Water",
            category = "Protein",
            servingSize = "1 can drained (120g)",
            calories = 130,
            protein = 29,
            carbs = 0,
            fat = 1,
            gymTip = "High protein density and very budget friendly for active gym-goers."
        ),
        PresetFood(
            id = "f_salmon",
            name = "Atlantic Salmon (Baked)",
            category = "Protein & Fats",
            servingSize = "100g",
            calories = 208,
            protein = 22,
            carbs = 0,
            fat = 13,
            gymTip = "Rich in Omega-3 EPA/DHA fatty acids for reducing joint soreness and inflammation."
        ),
        PresetFood(
            id = "f_greek_yogurt",
            name = "Nonfat Greek Yogurt",
            category = "Protein",
            servingSize = "1 cup (200g)",
            calories = 120,
            protein = 22,
            carbs = 7,
            fat = 0,
            gymTip = "Slow-digesting casein protein, ideal for breakfast or bedtime snack."
        ),
        PresetFood(
            id = "f_cottage_cheese",
            name = "Low-Fat Cottage Cheese",
            category = "Protein",
            servingSize = "1/2 cup (113g)",
            calories = 90,
            protein = 13,
            carbs = 5,
            fat = 2,
            gymTip = "Loaded with slow-release casein protein and bone-building calcium."
        ),
        PresetFood(
            id = "f_oatmeal",
            name = "Rolled Oats / Oatmeal",
            category = "Carbs",
            servingSize = "1/2 cup dry (40g)",
            calories = 150,
            protein = 5,
            carbs = 27,
            fat = 3,
            gymTip = "Complex low-GI carbohydrate providing sustained glycogen energy for heavy lifts."
        ),
        PresetFood(
            id = "f_brown_rice",
            name = "Cooked Brown Rice",
            category = "Carbs",
            servingSize = "1 cup (195g)",
            calories = 216,
            protein = 5,
            carbs = 45,
            fat = 2,
            gymTip = "Fiber-rich whole grain fuel that supports stable blood sugar and endurance."
        ),
        PresetFood(
            id = "f_white_rice",
            name = "Cooked Jasmine White Rice",
            category = "Carbs",
            servingSize = "1 cup (158g)",
            calories = 205,
            protein = 4,
            carbs = 45,
            fat = 0,
            gymTip = "Easy to digest, perfect for replenishing muscle glycogen immediately post-workout."
        ),
        PresetFood(
            id = "f_sweet_potato",
            name = "Baked Sweet Potato",
            category = "Carbs",
            servingSize = "1 medium (114g)",
            calories = 103,
            protein = 2,
            carbs = 24,
            fat = 0,
            gymTip = "High in potassium and beta-carotene to prevent muscle cramps and fuel reps."
        ),
        PresetFood(
            id = "f_banana",
            name = "Banana",
            category = "Carbs",
            servingSize = "1 medium (118g)",
            calories = 105,
            protein = 1,
            carbs = 27,
            fat = 0,
            gymTip = "The ultimate quick-digesting pre-workout energy snack packed with potassium."
        ),
        PresetFood(
            id = "f_peanut_butter",
            name = "Natural Peanut Butter",
            category = "Fats",
            servingSize = "2 tbsp (32g)",
            calories = 190,
            protein = 8,
            carbs = 7,
            fat = 16,
            gymTip = "Calorie-dense healthy monounsaturated fats to support healthy testosterone levels."
        ),
        PresetFood(
            id = "f_almonds",
            name = "Raw Almonds",
            category = "Fats",
            servingSize = "1 oz (28g / ~23 nuts)",
            calories = 164,
            protein = 6,
            carbs = 6,
            fat = 14,
            gymTip = "Antioxidant Vitamin E and magnesium for optimal muscle recovery."
        ),
        PresetFood(
            id = "f_olive_oil",
            name = "Extra Virgin Olive Oil",
            category = "Fats",
            servingSize = "1 tbsp (14g)",
            calories = 119,
            protein = 0,
            carbs = 0,
            fat = 14,
            gymTip = "Heart-healthy oleic acid, ideal drizzled over grilled meats and vegetables."
        ),
        PresetFood(
            id = "f_broccoli",
            name = "Steamed Broccoli",
            category = "Vegetables",
            servingSize = "1 cup chopped (91g)",
            calories = 31,
            protein = 3,
            carbs = 6,
            fat = 0,
            gymTip = "Cruciferous vegetable loaded with DIM, sulforaphane, and micronutrients."
        ),
        PresetFood(
            id = "f_quinoa",
            name = "Cooked Quinoa",
            category = "Carbs",
            servingSize = "1 cup (185g)",
            calories = 222,
            protein = 8,
            carbs = 39,
            fat = 4,
            gymTip = "Rare plant source containing all 9 essential amino acids alongside complex carbs."
        )
    )
}
