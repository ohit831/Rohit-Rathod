const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read workouts or meals", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("workouts").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("meals").get());
});

test("Authenticated user: Alice can create her own workout session", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("workouts").doc("w1").set({
      id: "w1",
      userId: ALICE_UID,
      workoutName: "Full Body Beginner A",
      dayOfWeek: "Monday",
      completedDate: "2026-10-31",
      durationMinutes: 45,
      totalVolumeKg: 1200,
      exercises: [
        { name: "Barbell Squat", sets: 3, reps: 10, weightKg: 40 }
      ],
      notes: "Felt great!",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: Bob cannot read Alice's workout session", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("workouts").doc("w1").set({
      id: "w1",
      userId: ALICE_UID,
      workoutName: "Full Body Beginner A",
      completedDate: "2026-10-31",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("workouts").doc("w1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("workouts").get());
});

test("Authenticated user: Alice can log a meal and Bob cannot read it", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("meals").doc("m1").set({
      id: "m1",
      userId: ALICE_UID,
      date: "2026-10-31",
      mealType: "breakfast",
      foodName: "Rolled Oatmeal with Whey",
      servingSize: "1 bowl",
      servings: 1.0,
      calories: 380,
      protein: 32,
      carbs: 48,
      fat: 6,
      createdAt: new Date(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("meals").doc("m1").get());
});

test("Schema validation: Ghost fields are rejected on create", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("workouts").doc("w2").set({
      id: "w2",
      userId: ALICE_UID,
      workoutName: "Full Body A",
      completedDate: "2026-10-31",
      ghostField: true,
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: Alice can update her own workout notes", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("workouts").doc("w1").set({
      id: "w1",
      userId: ALICE_UID,
      workoutName: "Full Body Beginner A",
      completedDate: "2026-10-31",
      notes: "First note",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("workouts").doc("w1").update({
      notes: "Updated note with better form",
      updatedAt: new Date(),
    })
  );
});
