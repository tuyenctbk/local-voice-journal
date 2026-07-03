# Project Plan - AuraJournal

This document outlines the development phases, release plan, and roadmap for **AuraJournal**.

---

## Phase 1: Foundation & Bug Fixes (Current Sprint)
* **Objective**: Stabilize the application core, fix critical audio/STT race conditions, and complete basic functional local features.
* **Deliverables**:
  1. Fix the asynchronous speech transcription bug (bypassing of SpeechRecognizer).
  2. Implement proper AdMob premium hiding logic.
  3. Support 15 localizations (German, Spanish, French, Japanese, Korean, Portuguese, Vietnamese, Chinese, Hindi, Arabic, Russian, Italian, Indonesian, Turkish).
  4. Write automated JUnit unit tests for DB queries, type converters, and NLP analyzers.
  5. Enable a functional on-device database backup via Share Intent.
  6. Fix thread calling violations in Android Auto context.

---

## Phase 2: On-Device GenAI & Smart Analytics (Next Sprint)
* **Objective**: Elevate the local heuristic NLP to run a real on-device LLM model using Google Play Services AICore or MediaPipe GenAI Tasks.
* **Deliverables**:
  1. Integrate MediaPipe LLM Tasks library (`com.google.mediapipe:tasks-genai`).
  2. Download/load a quantized Gemini Nano or Gemma 2B model on compatible devices.
  3. Design safety-filtered system instructions to generate wellness encouragement responses locally.
  4. Build a premium-only "Habit-Stressor Correlation" algorithm that finds statistical links between positive habits (e.g. Meditated, Hydrated) and stress reduction.

---

## Phase 3: Connected Ecosystem (Future Roadmap)
* **Objective**: Create seamless syncing between Phone, Wear OS, and TV without violating local-first principles.
* **Deliverables**:
  1. Replace Wear OS mockup recording with real system voice input.
  2. Implement the `Wearable DataClient API` to automatically transfer recorded transcripts from watch to phone DB.
  3. Add Android Auto remote transcription triggers.
  4. Create encrypted Google Drive or Dropbox backup option (allowing sync across devices using the user's personal private cloud without central app servers).
