CREATE TABLE users
(
  id         BIGSERIAL NOT NULL PRIMARY KEY,
  first_name VARCHAR(30) NOT NULL,
  last_name  VARCHAR(30) NOT NULL,
  dob        DATE        NOT NULL,
  gender     CHAR(1)     NOT NULL,
  ext_id     CHAR(24)    NOT NULL UNIQUE
);