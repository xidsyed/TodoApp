CREATE TABLE IF NOT EXISTS public.invitations (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    email text NOT NULL UNIQUE,
    assignor uuid NOT NULL REFERENCES public.user_profiles(id) ON DELETE CASCADE,
    assignee uuid REFERENCES public.user_profiles(id) ON DELETE CASCADE,
    role public.newzroom_role NOT NULL,
    eat timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

-- Trigger to auto-update updated_at
CREATE TRIGGER invitations_moddatetime
BEFORE UPDATE ON public.invitations
FOR EACH ROW EXECUTE FUNCTION moddatetime('updated_at');
