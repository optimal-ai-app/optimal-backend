-- ========== TABLE: profiles ==========
-- Extends auth.users with persona settings and device info
CREATE TABLE profiles (
  id UUID PRIMARY KEY REFERENCES auth.users(id),
  name TEXT,
  persona TEXT DEFAULT 'Neutral Coach',
  tone_settings JSONB DEFAULT '{}'::JSONB,
  device_token TEXT,
  created_at TIMESTAMP DEFAULT now()
);

-- ========== TABLE: todos ==========
-- Represents a recurring question/task (e.g., “Did you meditate today?”)
CREATE TABLE todos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES auth.users(id),
  question_text TEXT NOT NULL,
  scheduled_time TIME NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT now()
);

-- ========== TABLE: checkins ==========
-- Stores user responses to todo questions
CREATE TABLE checkins (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES auth.users(id),
  todo_id UUID REFERENCES todos(id),
  response_text TEXT,
  quick_tap BOOLEAN,
  timestamp TIMESTAMP DEFAULT now()
);

-- ========== TABLE: goals ==========
-- Higher-level goals with status tracking and progress %
CREATE TABLE goals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES auth.users(id),
  title TEXT,
  description TEXT,
  status TEXT DEFAULT 'in_progress', -- values: 'in_progress', 'complete', 'paused'
  progress INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT now()
);

-- ========== TABLE: diary_logs ==========
-- Optional freeform journal entries, tagged by context
CREATE TABLE diary_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES auth.users(id),
  entry TEXT,
  tags TEXT[],
  created_at TIMESTAMP DEFAULT now()
);

-- ========== TABLE: analytics ==========
-- Stores JSON analytics or wrapped reports
CREATE TABLE analytics (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES auth.users(id),
  type TEXT,              -- e.g., 'wrapped', 'summary'
  data JSONB,             -- flexible structure for OpenAI output, chart data, etc.
  created_at TIMESTAMP DEFAULT now()
);
