DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT
      FROM   pg_catalog.pg_roles
      WHERE  rolname = 'tq_proxy') THEN

      CREATE ROLE tq_proxy INHERIT;
   END IF;
END
$do$;
--
-- Create a schema to hide the proxy tables from public view.
--
CREATE SCHEMA IF NOT EXISTS tqos_dictionary;
GRANT ALL PRIVILEGES ON tqos_dictionary TO tq_proxy;

CREATE TABLE IF NOT EXISTS 
tqos_dictionary.dictionary (
	id int NOT NULL,
	word text NOT NULL,
	lc_word text NOT NULL,
	PRIMARY KEY (id, word)
);

CREATE INDEX IF NOT EXISTS idx_id ON tqos_dictionary.dictionary (id);
CREATE INDEX IF NOT EXISTS idx_doc ON tqos_dictionary.dictionary (word);
CREATE INDEX IF NOT EXISTS idx_par ON tqos_dictionary.dictionary (lc_word);

GRANT ALL PRIVILEGES ON tqos_dictionary.dictionary TO tq_proxy;
GRANT SELECT ON tqos_dictionary.dictionary TO tq_proxy_ro;

CREATE TABLE IF NOT EXISTS 
tqos_dictionary.latest (
	latest int PRIMARY KEY
);

GRANT ALL PRIVILEGES ON tqos_dictionary.latest TO tq_proxy;

