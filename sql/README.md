# Supabase SQL Schema — Optimal. App

This folder contains all database and security schema files for setting up the Supabase backend for the Optimal. app. These files define tables, apply Row-Level Security (RLS), and configure protected access to Supabase Storage buckets.

---

## 📁 Files

### `01_create_tables.sql`
Creates the core relational tables used throughout the app. These tables include:

- `profiles`: Extends `auth.users` with persona/tone settings and device tokens
- `todos`: User-defined recurring prompts (e.g., “Did you meditate today?”)
- `checkins`: Stores user responses to todos
- `goals`: Tracks broader objectives and progress
- `diary_logs`: Optional freeform journal entries
- `analytics`: Stores structured summaries (e.g., "Wrapped" reports)

### `02_rls_policies.sql`
Applies **Row-Level Security (RLS)** to ensure users can only access their own records. Each policy restricts access to rows based on the `auth.uid()` (the logged-in Supabase user).

RLS is enabled for:

- `profiles`
- `todos`
- `checkins`
- `goals`
- `diary_logs`
- `analytics`

### `03_storage_policies.sql`
Adds RLS policies for Supabase **Storage buckets**:
- `voice-clips`: Stores user voice logs
- `wrapped-reports`: Stores user-specific infographics (e.g., weekly “Wrapped” summaries)

Each policy ensures users can only access files in paths that start with their **UUID**.

---

## ✅ Requirements

### Supabase Storage Access Structure

To ensure users can only access their own files, all uploads must be saved using this structure:

```text
voice-clips/{user_id}/recording1.webm
wrapped-reports/{user_id}/wrapped_2025.png
