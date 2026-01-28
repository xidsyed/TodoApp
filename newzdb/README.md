# Setup
1. Create a `.env` file in the root directory of the project
```.env
SUPABASE_AUTH_EXTERNAL_GOOGLE_SECRET=
SUPABASE_AUTH_EXTERNAL_GOOGLE_CLIENT_ID=
SITE_URL=http://localhost:5173
SUPABASE_CALLBACK_URL=http://localhost:54321/auth/v1/callback
CUSTOM_ACCESS_TOKEN_HOOK_URL=http://host.docker.internal:8080/auth/hooks/custom_access_token
BEFORE_USER_CREATED_HOOK_URL=http://host.docker.internal:8080/auth/hooks/before_user_created
WEB_HOOK_SECRETS=v1,whsec_***
```
2. Generate a `supabase/signing_key.json` as per instructions. This is expected by the `supabase/config.toml` 
 - https://supabase.com/docs/reference/cli/supabase-gen-signing-key
 - https://supabase.com/docs/guides/auth/signing-keys#how-to-create-mint-jwts-if-access-to-the-private-key-or-shared-secret-is-not-possible

3. Add the secret to the `todoapp/src/main/resources/secrets.yaml`