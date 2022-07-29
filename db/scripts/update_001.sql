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