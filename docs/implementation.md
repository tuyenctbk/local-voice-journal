# System Implementation Detail - AuraJournal

This document provides a technical look into the classes, databases, and third-party libraries integrated into **AuraJournal**.

---

## 1. Database Schema & Type Converters

AuraJournal uses SQLite via Jetpack Room. The schema contains a single table `journal_entries`:
* **id** (`Long`): Auto-generated primary key.
* **timestamp** (`Long`): Epoch milliseconds.
* **transcript** (`String`): Transcribed voice text.
* **stressLevel** (`String`): Extracted sentiment stress level (`LOW`, `MEDIUM`, `HIGH`).
* **themes** (`List<String>`): Extracted areas (e.g. `Career`, `Relationships`, `Fitness`).
* **stressors** (`List<String>`): Identified sources of anxiety (e.g. `Work Deadlines`, `Lack of Sleep`).
* **habits** (`List<String>`): Tracked positive actions (e.g. `Hydrated`, `Exercised`, `Meditated`).
* **durationSeconds** (`Int`): Length of reflection (max 60s).

### Serialization
Lists are stored as a single comma-separated string (`,,`) inside the SQLite row, using custom Type Converters:
* `fromString(value: String?)`: splits by `,,` and filters empty elements.
* `fromList(list: List<String>?)`: joins elements with `,,`.

---

## 2. Speech-to-Text Architecture

Offline transcription relies on the platform's `SpeechRecognizer`:
* Instantiated with `SpeechRecognizer.createSpeechRecognizer(context)`.
* Triggers an intent with `ACTION_RECOGNIZE_SPEECH` and `EXTRA_PREFER_OFFLINE` to force system-level local recognition.
* Emits real-time partial text updates through `onPartialResults` and updates `transcript` StateFlow.
* Finalizes and delivers the complete text via `onResults`.

### Reactive Synchronization Fix
To prevent the race condition where `MainActivity` inserts empty/partial transcriptions into the database, we introduce a `SharedFlow` event called `onTranscriptReady`. When the recording is stopped:
1. `MainActivity` tells `SpeechToTextManager` to stop capturing.
2. The UI enters a `processing` state.
3. The SpeechRecognizer processes the buffer and fires `onResults` (or `onError` with a fallback).
4. `SpeechToTextManager` emits the final string to `onTranscriptReady`.
5. `MainActivity` collects the finalized string, runs the AI analyzer, and inserts it into the database, ending the processing state.

---

## 3. Local NLP Analysis

If AICore is unsupported or unavailable on the user's hardware, the app falls back to `FallbackLocalAnalyzer`, which evaluates text against keyword groups:
* **Stress score**: Counts matches of anxiety-themed words (e.g. *anxious*, *deadline*, *struggle*).
* **Themes**: Matches categories such as *Career* (work, office, code), *Relationships* (family, friend, wife), *Nutrition* (water, food, meal).
* **Habits**: Detects active verbs (e.g. *read*, *meditate*, *walk*).

---

## 4. Ads & Paywall Architecture

Monetization consists of standard AdMob placeholders:
* **Banner Ads**: Small banner positioned at the bottom of the screens.
* **Native Ads**: Integrated inline within list views.
* **Interstitial Ads**: Full-screen interstitials loaded on completion events.

### Premium Guard
We use Jetpack Compose conditional compilation to check `isPremium`. If `true`, the `AdsHelper` renders empty layouts, preventing any ads from compiling or showing in the viewport.
