DROP TABLE IF EXISTS companies;
DROP SEQUENCE IF EXISTS global_seq;
CREATE SEQUENCE IF NOT EXISTS global_seq START WITH 1;

CREATE TABLE companies
(
    id         INTEGER PRIMARY KEY DEFAULT nextval('global_seq'),
    name       VARCHAR NOT NULL,
    city       VARCHAR NOT NULL,
    foundation VARCHAR NOT NULL
);

/*unique index for exclude duplication*/
CREATE UNIQUE INDEX companies_unique_idx ON companies (name, foundation);
