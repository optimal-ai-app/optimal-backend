-- ========== POLICY: voice-clips ==========
-- Allow users to access only their own voice files based on path format: "user_id/filename"

CREATE POLICY "Users can access their own voice clips"
  ON storage.objects
  FOR ALL
  USING (
    bucket_id = 'voice-clips'
    AND auth.uid()::text = split_part(name, '/', 1)
  );

-- ========== POLICY: wrapped-reports ==========
-- Allow users to access only their own infographics based on path format: "user_id/filename"

CREATE POLICY "Users can access their own wrapped reports"
  ON storage.objects
  FOR ALL
  USING (
    bucket_id = 'wrapped-reports'
    AND auth.uid()::text = split_part(name, '/', 1)
  );
