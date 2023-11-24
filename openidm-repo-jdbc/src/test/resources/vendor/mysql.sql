CREATE TABLE wrenidm.objecttypes (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  objecttype VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT idx_objecttypes_objecttype UNIQUE (objecttype)
);

CREATE TABLE wrenidm.genericobjects (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  objecttypes_id BIGINT UNSIGNED NOT NULL,
  objectid VARCHAR(255) NOT NULL,
  rev VARCHAR(38) NOT NULL,
  fullobject MEDIUMTEXT,
  PRIMARY KEY (id),
  CONSTRAINT fk_genericobjects_objecttypes  FOREIGN KEY (objecttypes_id)
    REFERENCES wrenidm.objecttypes (id) ON DELETE CASCADE,
  CONSTRAINT idx_genericobjects_object UNIQUE (objecttypes_id, objectid)
) ENGINE = InnoDB;

CREATE TABLE wrenidm.genericobjectproperties (
  genericobjects_id BIGINT UNSIGNED NOT NULL,
  propkey VARCHAR(255) NOT NULL,
  proptype VARCHAR(32),
  propvalue VARCHAR(768),
  CONSTRAINT fk_genericobjectproperties_genericobjects FOREIGN KEY (genericobjects_id)
    REFERENCES wrenidm.genericobjects (id) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB;
CREATE INDEX fk_genericobjectproperties_genericobjects ON wrenidm.genericobjectproperties (genericobjects_id);
CREATE INDEX idx_genericobjectproperties_propkey ON wrenidm.genericobjectproperties (propkey);
CREATE INDEX idx_genericobjectproperties_propvalue ON wrenidm.genericobjectproperties (propvalue);

CREATE TABLE wrenidm.managedgreeting (
  objectid VARCHAR(255) NOT NULL,
  rev VARCHAR(38) NOT NULL,
  name VARCHAR(255),
  ranking VARCHAR(32),
  visible VARCHAR(5),
  tags VARCHAR(255),
  meta VARCHAR(2048),
  PRIMARY KEY (objectid)
) ENGINE = InnoDB;
