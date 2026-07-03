# Quality Assurance & Testing Plan - AuraJournal

This document outlines the testing strategy, test cases, and automated test suite details for **AuraJournal**.

---

## 1. Automated Test Plan

We use JVM-based unit tests to verify critical logic layers. Tests run under both `:core` and `:mobile` submodules.

### Core Module Tests
* **ConvertersTest.kt**: Verifies Room DB list type serialization.
  * *Case 1*: Null or empty list converts to empty string, and vice versa.
  * *Case 2*: Standard lists serialize with `,,` delimiter and deserialize correctly.
* **JournalDatabaseTest.kt**: Verifies Room database DAO operations using an in-memory database instance.
  * *Case 1*: Entry insertion and ID return.
  * *Case 2*: Ordered query sorting by timestamp.
  * *Case 3*: Entry deletion.
  * *Case 4*: Database clear operations.

### Mobile Module Tests
* **FallbackLocalAnalyzerTest.kt**: Validates the local NLP heuristics rule engine.
  * *Case 1*: Low stress detection (calm diary input).
  * *Case 2*: High stress & workload stressors detection.
  * *Case 3*: Positive habit completion checks (hydration, exercise, meditation).

### Running Automated Tests
Run unit tests from the project root using:
```bash
./gradlew test
```

---

## 2. Manual Verification Plan

### Test Scenario A: Language Localization Check
1. Change device settings language to Spanish (`es`), Japanese (`ja`), Russian (`ru`), or Turkish (`tr`).
2. Open AuraJournal.
3. Verify Onboarding, Home, and Settings screen text are translated correctly.

### Test Scenario B: Speech-to-Text Asynchronous Capture
1. Go to Home Screen.
2. Tap the Microphone icon. Speak several words (in emulator, wait for simulation).
3. Tap Stop.
4. Verify that the UI displays a processing state and does not immediately write the fallback text.
5. Check that the Detail Screen displays your spoken transcript.

### Test Scenario C: Premium & Ad Disappearance
1. Verify that "Ad Banner" placeholders are visible on Home, Settings, History, and Detail screens.
2. Go to Settings and toggle the Premium Status switch to On.
3. Verify that all Ad Banner placeholders instantly disappear.
4. Verify that the Weekly Analytics graph on the Dashboard is unlocked and legible.
5. Return to Settings and toggle Premium to Off. Ads should reappear and analytics should lock.

### Test Scenario D: Local JSON Backup
1. Save 2 or 3 reflections.
2. Go to Settings and tap "Export Local Backup (JSON)".
3. Verify that the system launches an Android Share Sheet allowing you to save the file to Downloads, Google Drive, or send it to other apps.
