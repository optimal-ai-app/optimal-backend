-- ========== RLS: Enable Row-Level Security on all tables ==========
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE todos ENABLE ROW LEVEL SECURITY;
ALTER TABLE checkins ENABLE ROW LEVEL SECURITY;
ALTER TABLE goals ENABLE ROW LEVEL SECURITY;
ALTER TABLE diary_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE analytics ENABLE ROW LEVEL SECURITY;

-- ========== POLICY: profiles ==========
CREATE POLICY "User can manage own profile"
  ON profiles
  FOR ALL
  USING (auth.uid() = id)
  WITH CHECK (auth.uid() = id);

-- ========== POLICY: todos ==========
CREATE POLICY "User can access own todos"
  ON todos
  FOR ALL
  USING (auth.uid() = user_id);

-- ========== POLICY: checkins ==========
CREATE POLICY "User can access own checkins"
  ON checkins
  FOR ALL
  USING (auth.uid() = user_id);

-- ========== POLICY: goals ==========
CREATE POLICY "User can access own goals"
  ON goals
  FOR ALL
  USING (auth.uid() = user_id);

-- ========== POLICY: diary_logs ==========
CREATE POLICY "User can access own diary logs"
  ON diary_logs
  FOR ALL
  USING (auth.uid() = user_id);

-- ========== POLICY: analytics ==========
CREATE POLICY "User can access own analytics"
  ON analytics
  FOR ALL
  USING (auth.uid() = user_id);
