CREATE SCHEMA IF NOT EXISTS extensions;
ALTER DATABASE newzdb SET search_path TO public, extensions;
CREATE EXTENSION IF NOT EXISTS "moddatetime" WITH SCHEMA extensions;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA extensions;
