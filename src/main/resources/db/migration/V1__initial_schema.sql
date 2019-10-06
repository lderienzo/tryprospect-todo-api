CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS moddatetime;

CREATE TABLE todo (
  id                UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
  text              TEXT          NOT NULL,
  is_completed      BOOLEAN       NOT NULL DEFAULT FALSE,
  created_at        TIMESTAMP WITH TIME ZONE     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_modified_at  TIMESTAMP WITH TIME ZONE     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER todo_moddatetime BEFORE UPDATE ON todo
	FOR EACH ROW
	EXECUTE PROCEDURE moddatetime (last_modified_at);
