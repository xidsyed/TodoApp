-- Create role domain
CREATE DOMAIN public.newzroom_role AS text
  CHECK (VALUE IN ('writer', 'admin'));

-- Create user_profiles table
CREATE TABLE IF NOT EXISTS public.user_profiles (
    id uuid PRIMARY KEY,
    email text NOT NULL UNIQUE,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    role public.newzroom_role NOT NULL,
    display_name text NOT NULL,
    profile_pic text,
    freeze_till timestamptz,
    freeze_cause text
);

CREATE INDEX idx_user_profiles_created_at ON public.user_profiles(created_at);
CREATE INDEX idx_user_profiles_email ON public.user_profiles(email);

-- Auto-update updated_at on modification
CREATE TRIGGER user_profiles_moddatetime
BEFORE UPDATE ON public.user_profiles
FOR EACH ROW EXECUTE FUNCTION extensions.moddatetime('updated_at');
