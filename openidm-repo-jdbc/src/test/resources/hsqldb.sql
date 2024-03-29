CREATE SCHEMA wrenidm;

CREATE TABLE wrenidm.objecttypes (
  id INTEGER IDENTITY NOT NULL,
  objecttype VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT idx_objecttypes_objecttype UNIQUE (objecttype)
);

CREATE TABLE wrenidm.genericobjects (
  id INTEGER IDENTITY NOT NULL,
  objecttypes_id INTEGER NOT NULL,
  objectid VARCHAR(255) NOT NULL,
  rev VARCHAR(38) NOT NULL,
  fullobject VARCHAR(65535),
  PRIMARY KEY (id),
  CONSTRAINT fk_genericobjects_objecttypes FOREIGN KEY (objecttypes_id)
    REFERENCES wrenidm.objecttypes (id) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT idx_genericobjects_object UNIQUE (objecttypes_id, objectid)
);

CREATE TABLE wrenidm.genericobjectproperties (
  genericobjects_id BIGINT NOT NULL,
  propkey VARCHAR(255) NOT NULL,
  proptype VARCHAR(32),
  propvalue VARCHAR(65535),
  CONSTRAINT fk_genericobjectproperties_genericobjects FOREIGN KEY (genericobjects_id)
    REFERENCES wrenidm.genericobjects (id) ON DELETE CASCADE ON UPDATE NO ACTION
);
CREATE INDEX fk_genericobjectproperties_genericobjects ON wrenidm.genericobjectproperties (genericobjects_id);
CREATE INDEX idx_genericobjectproperties_prop ON wrenidm.genericobjectproperties (propkey, propvalue);

CREATE TABLE wrenidm.managedgreeting (
  objectid VARCHAR(255) NOT NULL,
  rev VARCHAR(38) NOT NULL,
  name VARCHAR(255),
  priority INTEGER,
  ranking NUMERIC(10, 2),
  visible BOOLEAN,
  tags VARCHAR(255),
  meta VARCHAR(2048),
  PRIMARY KEY (objectid)
);
