-- creating main table
CREATE TABLE IF NOT EXISTS students
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255),
    experience VARCHAR(255),
    salary     INT NOT NULL,
    created    TIMESTAMP DEFAULT current_timestamp,
    updated    TIMESTAMP
);

-- creating trigger for modify
CREATE OR REPLACE FUNCTION upd_timestamp() RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    NEW.updated = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

CREATE TRIGGER students
    BEFORE UPDATE
    ON students
    FOR EACH ROW
EXECUTE PROCEDURE upd_timestamp();