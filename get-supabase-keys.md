# Get Supabase API Keys

## To fix the 401 Unauthorized error, you need to get the correct API keys from your Supabase project:

### 1. Go to your Supabase project dashboard:

```
https://app.supabase.com/project/umndbnimuswczlydijnf
```

### 2. Navigate to Settings → API

### 3. Copy the following keys:

**Project URL:**

```
https://umndbnimuswczlydijnf.supabase.co
```

**Anon Key (public):**

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVtbmRibmltdXN3Y3pseWRpam5mIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjI0NDczMDQsImV4cCI6MjAzODAyMzMwNH0.CZFbL9dqO6UJVmMlh3BFLnMQcHnJZWL1-qPKwEhDQj4
```

**Service Role Key (secret):**

```
[COPY FROM SUPABASE DASHBOARD]
```

**JWT Secret:**

```
[COPY FROM SUPABASE DASHBOARD]
```

### 4. Create a `.env` file in the optimal-backend directory:

```bash
SUPABASE_ANON_KEY=your_actual_anon_key_here
SUPABASE_JWT_SECRET=your_actual_jwt_secret_here
```

### 5. Alternative: Set environment variables:

```bash
export SUPABASE_ANON_KEY="your_actual_anon_key_here"
export SUPABASE_JWT_SECRET="your_actual_jwt_secret_here"
```

### 6. Ensure Authentication is enabled in Supabase:

- Go to Authentication → Settings
- Make sure "Enable email confirmations" is configured as needed
- Check that "Enable sign ups" is enabled
- Verify authentication providers are configured

## Test the connection:

After updating the keys, test with:

```bash
curl -X GET http://localhost:8080/api/test/supabase-config
```
