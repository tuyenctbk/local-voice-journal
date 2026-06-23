# Privacy Policy for AuraJournal

**Last Updated: June 2026**

AuraJournal is built from the ground up as a **privacy-first, local-first application**. We believe that your daily reflections, thoughts, and emotional insights are deeply personal and should remain under your absolute control.

---

## 1. Local-First Data Processing

- **Voice Reflections**: When you speak to record a journal entry, the audio data is captured and processed **entirely on your physical device**.
- **Speech-to-Text Transcription**: Transcription is performed locally using your Android device's offline Speech Recognition service. No audio streams are sent to remote servers for processing.
- **Local AI Analysis**: Emotional analysis, theme extraction, and habit tracking calculations are conducted completely offline on-device (via local natural language heuristics or on-device AICore/Gemini Nano models).

---

## 2. No Data Collection or Server Uploads

- **No Cloud Database**: We do not maintain remote databases or user account servers. All journal entries, transcripts, and insights are stored in a private SQLite database inside your app's isolated local storage.
- **No Third-Party Sharing**: Since your personal journaling data never leaves your device, it is impossible for us to share or sell your reflections to advertisers, developers, or cloud providers.

---

## 3. Analytics & Diagnostics

- **Firebase Crashlytics & Analytics**: The app includes standard crash logging and anonymous usage statistic frameworks to help us maintain software stability.
  - **No Personal Data In Logs**: We configure these integrations to ensure that **no journal transcripts, audio bytes, or metadata** are ever included in diagnostic packets. Only technical application stack traces (e.g., system out-of-memory warnings) are dispatched.
- **AdMob**: Standard AdMob test ads are present as placeholders. These placeholders respect user device configuration settings regarding tracking.

---

## 4. User Control & Data Deletion

You own your data completely. At any time, you can access the **Settings** screen in AuraJournal to:
1. **Export Local Backup**: Save a readable JSON format of all your journal entries directly to your local file system.
2. **Wipe Database**: Permanently erase all local database records from your device memory. This action is instantaneous and irreversible.
