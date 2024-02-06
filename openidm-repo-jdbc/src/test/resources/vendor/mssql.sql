CREATE DATABASE [wrenidm] COLLATE Latin1_General_100_CS_AS;

USE [wrenidm];

CREATE LOGIN [wrenidm] WITH
  PASSWORD = N'wrenidm',
  CHECK_POLICY = OFF,
  CHECK_EXPIRATION = OFF,
  DEFAULT_DATABASE = [wrenidm];

CREATE USER [wrenidm] FOR LOGIN [wrenidm] WITH DEFAULT_SCHEMA = [wrenidm];

CREATE SCHEMA [wrenidm] AUTHORIZATION [wrenidm];

CREATE TABLE wrenidm.objecttypes (
  id NUMERIC(19,0) NOT NULL IDENTITY,
  objecttype NVARCHAR(255) NOT NULL,
  PRIMARY KEY CLUSTERED (id),
  CONSTRAINT idx_objecttypes_objecttype UNIQUE (objecttype)
);

CREATE TABLE wrenidm.genericobjects (
  id NUMERIC(19,0) NOT NULL IDENTITY,
  objecttypes_id NUMERIC(19,0) NOT NULL,
  objectid NVARCHAR(255) NOT NULL,
  rev NVARCHAR(38) NOT NULL,
  fullobject NTEXT,
  PRIMARY KEY (id),
  CONSTRAINT fk_genericobjects_objecttypes FOREIGN KEY (objecttypes_id)
    REFERENCES wrenidm.objecttypes (id) ON DELETE CASCADE,
  CONSTRAINT idx_genericobjects_object UNIQUE (objecttypes_id, objectid)
);

CREATE TABLE wrenidm.genericobjectproperties (
  genericobjects_id NUMERIC(19,0) NOT NULL,
  propkey NVARCHAR(255) NOT NULL,
  proptype NVARCHAR(32),
  propvalue NVARCHAR(2000),
  CONSTRAINT fk_genericobjectproperties_genericobjects FOREIGN KEY (genericobjects_id)
    REFERENCES wrenidm.genericobjects (id) ON DELETE CASCADE
);
CREATE INDEX fk_genericobjectproperties_genericobjects ON wrenidm.genericobjectproperties (genericobjects_id);
CREATE INDEX idx_genericobjectproperties_propkey ON wrenidm.genericobjectproperties (propkey);
CREATE INDEX idx_genericobjectproperties_propvalue ON wrenidm.genericobjectproperties (propvalue);

CREATE TABLE wrenidm.managedgreeting (
  objectid NVARCHAR(255) NOT NULL,
  rev NVARCHAR(38) NOT NULL,
  name NVARCHAR(255),
  priority INTEGER,
  ranking DECIMAL(10, 2),
  visible BIT,
  tags NVARCHAR(255),
  meta NVARCHAR(2048),
  PRIMARY KEY (objectid)
);
