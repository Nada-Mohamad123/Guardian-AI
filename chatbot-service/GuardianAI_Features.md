# GuardianAI – Feature Guide for Parents

This document explains every feature available in the GuardianAI parental-control app.
It is written so it can also be used as the **knowledge base** for the in-app chatbot
(RAG source document).

---

## 1. Home Dashboard
- Shows the current date and a "All systems active – AI scanning live" status badge, confirming the AI monitoring engine is running in real time.
- **Quick Actions** grid:
  - **Chat with AI** – opens the GuardianAI Assistant chatbot to ask questions.
  - **View Logs** – jumps to the full Incident Logs screen.
  - **Reports** – jumps to the weekly/daily insight reports.
  - **Filters** – jumps to content-control settings.
- **Your Children** section: lists each child profile with their current **Safety Score** (0–100%), calculated from how much of their recent activity was flagged as unsafe.

## 2. Family Profiles (tab: Family)
- **Add Child** button to create a new child profile.
- Each child profile lets the parent:
  - See which apps are currently being monitored for that child (e.g. Facebook).
  - **Content Suggestions** – the parent picks topics the child loves (e.g. Robotics, Technology, Tennis). The AI uses these interests to surface safe, age-appropriate content and steer the child's feed away from harmful material. Parents can also type a custom topic in "Something else?" and add it.
  - **Save Interests** – stores the chosen topics; the child's feed adjusts within 24 hours.
  - **Device Pairing** – generates a one-time 6-digit code (expires in ~30 seconds) that the parent shares with the child's device to link it to GuardianAI.
  - **Edit Monitored Apps** – a checklist (YouTube, TikTok, Instagram, Snapchat, Facebook, WhatsApp, Telegram, Discord, Chrome, etc.) where the parent chooses exactly which apps GuardianAI should watch for that child. Toggle on/off, then **Save Changes**.

## 3. Incident Logs (tab: Logs)
- Shows the total number of flagged incidents (e.g. 231 total) broken down by severity:
  - **High** – serious content (e.g. graphic violence) requiring immediate attention.
  - **Medium** – borderline content.
  - **Low** – minor flags.
- Filters let the parent narrow results by:
  - **Child** (All Children, or a specific child like Soha, Repo, Mero…).
  - **Severity** (All / High / Medium / Low).
  - **Type** (All Types / Nudity / Violence / Hate Speech).
- Each incident card shows: which child, the app/source (e.g. Chrome, "web"), severity tag, timestamp, and whether the screen was automatically **Blurred**.
- Tapping an incident opens **Incident Details**: Application, Timestamp, Category, Severity, **AI Confidence %** (how sure the AI model is that the content is unsafe), and the **Action** taken (e.g. "Screen Blurred").

## 4. Reports (tab: Reports)
- Per-child **Daily Report** with:
  - **Total** events scanned, **Unsafe** count, **Safe** count.
  - **Overall Safety Score** (e.g. 79/100) shown as a progress bar.
  - **Safety Breakdown** donut chart: Safe events %, Unsafe events %, and overall **Risk Level** (Low/Medium/High).
  - A warning banner if a meaningful percentage of content was flagged, suggesting the parent review incidents with their child.
  - **Export Full Report (PDF)** – downloads a printable report.

## 5. AI Assistant / Chatbot (tab: AI)
- A conversational assistant ("GuardianAI Assistant – Online · Powered by AI") that parents can ask anything about:
  - What settings mean (e.g. "What does Medium restriction level do?").
  - How to change settings directly from chat (e.g. "Change restriction to High").
  - Smart suggestions based on real incident patterns (e.g. recommending a time-based restriction schedule for evening hours).
- Quick-reply chips (e.g. "Change restriction to High", "Today's summary") let parents act with one tap instead of typing.
- This is the feature we are extending with **RAG (Retrieval-Augmented Generation)** so the chatbot can answer feature questions accurately by pulling from this very document before calling the LLM (Gemini).

## 6. Settings (tab: Settings)
- **Profile** – parent's name, phone number, email.
- **Notifications**
  - *Instant Alerts* – notify immediately on high-severity incidents.
  - *Weekly Reports* – summary email every Sunday morning.
- **Preferences**
  - *Cultural Context* – choose a region (e.g. Egypt) so the AI applies culture-specific sensitivity when judging content.
  - *Auto Sync* – automatically sync data when the child's device connects.
  - *Data Retention* – how long logs are stored (e.g. 90 days).
- **About GuardianAI** – app version/build info.
- **User Manual** – in-app help guide.

## 7. Restriction Levels (referenced across the app)
- **Low** – minimal filtering, mostly logging.
- **Medium** – blurs explicit nudity, graphic violence, and adult content while allowing borderline material through; recommended for ages 8–11.
- **High** – strictest filtering; blocks/blurs most flagged categories; recommended for younger children or during sensitive hours (e.g. evenings).

---

## How the Chatbot Should Use This Document (RAG)
1. Split this file into chunks (by section/heading).
2. Embed each chunk with an embedding model (e.g. Gemini's `text-embedding-004`).
3. When the parent asks a question, embed the question, retrieve the top-k most similar chunks (cosine similarity).
4. Insert those chunks into the prompt sent to the Gemini LLM as context, then ask Gemini to answer using only that context, in simple, friendly language for a parent.
