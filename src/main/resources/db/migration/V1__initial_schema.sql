CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE todo (
  id                UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
  text              TEXT          NOT NULL,
  is_completed      BOOLEAN       NOT NULL DEFAULT FALSE,
  created_at        TIMESTAMP WITH TIME ZONE     NOT NULL DEFAULT now(),
  last_modified_at  TIMESTAMP WITH TIME ZONE     NOT NULL DEFAULT now()
);
