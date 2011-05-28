SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `OpenIDM` ;
CREATE SCHEMA IF NOT EXISTS `OpenIDM` DEFAULT CHARACTER SET utf8 ;
USE `OpenIDM` ;

-- -----------------------------------------------------
-- Table `OpenIDM`.`Objects`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`Objects` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`Objects` (
  `uuid` VARCHAR(36) NOT NULL ,
  `version` INT NULL ,
  `repomod` DATETIME NOT NULL ,
  `dtype` VARCHAR(45) NOT NULL ,
  `objectType` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `OpenIDM`.`Accounts`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`Accounts` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`Accounts` (
  `uuid` VARCHAR(36) NOT NULL ,
  `name` VARCHAR(250) NOT NULL ,
  `resource_uuid` VARCHAR(36) NOT NULL ,
  `user_uuid` VARCHAR(36) NULL ,
  `object_class` VARCHAR(256) NOT NULL ,
  PRIMARY KEY (`uuid`) ,
  CONSTRAINT `FK_Accounts`
    FOREIGN KEY (`uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE UNIQUE INDEX `name_UNIQUE` ON `OpenIDM`.`Accounts` (`name` ASC) ;

CREATE INDEX `FK_Accounts_Resources` ON `OpenIDM`.`Accounts` (`resource_uuid` ASC) ;

CREATE INDEX `FK_Accounts_Persons` ON `OpenIDM`.`Accounts` (`user_uuid` ASC) ;

CREATE INDEX `FK_Accounts` ON `OpenIDM`.`Accounts` (`uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`BooleanProperties`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`BooleanProperties` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`BooleanProperties` (
  `uuid` VARCHAR(36) NOT NULL ,
  `attrname` VARCHAR(128) NOT NULL ,
  `value` BIT NULL ,
  PRIMARY KEY (`uuid`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `OpenIDM`.`DateProperties`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`DateProperties` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`DateProperties` (
  `uuid` VARCHAR(36) NOT NULL ,
  `attrname` VARCHAR(128) NOT NULL ,
  `value` DATETIME NULL ,
  PRIMARY KEY (`uuid`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `OpenIDM`.`Domains`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`Domains` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`Domains` (
  `uuid` VARCHAR(36) NOT NULL ,
  `name` VARCHAR(128) NOT NULL ,
  `regex` VARCHAR(500) NOT NULL ,
  `reserved_regex` VARCHAR(500) NOT NULL ,
  `unallowable_regex` VARCHAR(500) NOT NULL ,
  `domain_uuid` VARCHAR(36) NULL ,
  PRIMARY KEY (`uuid`) ,
  CONSTRAINT `FK_Domains`
    FOREIGN KEY (`uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE UNIQUE INDEX `name_UNIQUE` ON `OpenIDM`.`Domains` (`name` ASC) ;

CREATE INDEX `FK_Domains` ON `OpenIDM`.`Domains` (`uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`GenericEntities`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`GenericEntities` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`GenericEntities` (
  `uuid` VARCHAR(36) NOT NULL ,
  `name` VARCHAR(128) NOT NULL ,
  PRIMARY KEY (`uuid`) ,
  CONSTRAINT `FK_GenericEntities`
    FOREIGN KEY (`uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `FK_GenericEntities` ON `OpenIDM`.`GenericEntities` (`uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`IntegerProperties`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`IntegerProperties` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`IntegerProperties` (
  `uuid` VARCHAR(36) NOT NULL ,
  `attrname` VARCHAR(128) NOT NULL ,
  `value` INT NULL ,
  PRIMARY KEY (`uuid`) )
ENGINE = InnoDB
COMMENT = '\n';


-- -----------------------------------------------------
-- Table `OpenIDM`.`Users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`Users` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`Users` (
  `uuid` VARCHAR(36) NOT NULL ,
  `name` VARCHAR(128) NOT NULL ,
  `familyName` VARCHAR(128) NULL ,
  `fullName` VARCHAR(128) NULL ,
  `givenName` VARCHAR(128) NULL ,
  `employeeNumber` varchar(128) DEFAULT NULL,
  `honorificPrefix` varchar(128) DEFAULT NULL,
  `honorificSuffix` varchar(128) DEFAULT NULL,
  `locality` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`uuid`) ,
  CONSTRAINT `FK_Users`
    FOREIGN KEY (`uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE UNIQUE INDEX `name_UNIQUE` ON `OpenIDM`.`Users` (`name` ASC) ;

CREATE INDEX `FK_Users` ON `OpenIDM`.`Users` (`uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`Resources`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`Resources` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`Resources` (
  `uuid` VARCHAR(36) NOT NULL ,
  `name` VARCHAR(128) NOT NULL ,
  `type` VARCHAR(128) NOT NULL ,
  `namespace` VARCHAR(128) NOT NULL ,
  PRIMARY KEY (`uuid`) ,
  CONSTRAINT `FK_Resources`
    FOREIGN KEY (`uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE UNIQUE INDEX `name_UNIQUE` ON `OpenIDM`.`Resources` (`name` ASC) ;

CREATE INDEX `FK_Resources` ON `OpenIDM`.`Resources` (`uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`StringProperties`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`StringProperties` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`StringProperties` (
  `uuid` VARCHAR(36) NOT NULL ,
  `attrname` VARCHAR(128) NOT NULL ,
  `value` MEDIUMTEXT NULL ,
  PRIMARY KEY (`uuid`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `OpenIDM`.`Domains_Objects`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`Domains_Objects` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`Domains_Objects` (
  `domain_uuid` VARCHAR(36) NOT NULL ,
  `object_uuid` VARCHAR(36) NOT NULL ,
  CONSTRAINT `FK_Object`
    FOREIGN KEY (`object_uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_Domain`
    FOREIGN KEY (`domain_uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `PR_ID` ON `OpenIDM`.`Domains_Objects` (`domain_uuid` ASC, `object_uuid` ASC) ;

CREATE INDEX `FK_Object` ON `OpenIDM`.`Domains_Objects` (`object_uuid` ASC) ;

CREATE INDEX `FK_Domain` ON `OpenIDM`.`Domains_Objects` (`domain_uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`Objects_Properties`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`Objects_Properties` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`Objects_Properties` (
  `object_uuid` VARCHAR(36) NOT NULL ,
  `property_type` CHAR NOT NULL ,
  `property_uuid` VARCHAR(36) NOT NULL ,
  `property_index` INT NOT NULL ,
  PRIMARY KEY (`object_uuid`, `property_index`) ,
  CONSTRAINT `FK_Object_Property`
    FOREIGN KEY (`object_uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `FK_Object_Property` ON `OpenIDM`.`Objects_Properties` (`object_uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`AccountAttributes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`AccountAttributes` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`AccountAttributes` (
  `uuid` VARCHAR(36) NOT NULL ,
  `attrname` VARCHAR(255) NOT NULL ,
  `attrvalue` VARCHAR(255) NOT NULL ,
  CONSTRAINT `FK_AccountAttributes`
    FOREIGN KEY (`uuid` )
    REFERENCES `OpenIDM`.`Accounts` (`uuid` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `FK_AccountAttributes` ON `OpenIDM`.`AccountAttributes` (`uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`ResourceObjectShadows`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`ResourceObjectShadows` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`ResourceObjectShadows` (
  `uuid` VARCHAR(36) NOT NULL ,
  `name` VARCHAR(250) NOT NULL ,
  `resource_uuid` VARCHAR(36) NOT NULL ,
  `object_class` VARCHAR(256) NOT NULL ,
  PRIMARY KEY (`uuid`) ,
  CONSTRAINT `FK_ResourceObjectShadows`
    FOREIGN KEY (`uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE UNIQUE INDEX `name_UNIQUE` ON `OpenIDM`.`ResourceObjectShadows` (`name` ASC) ;

CREATE INDEX `FK_ResourceObjectShadows_Resources` ON `OpenIDM`.`ResourceObjectShadows` (`resource_uuid` ASC) ;

CREATE INDEX `FK_ResourceObjectShadows` ON `OpenIDM`.`ResourceObjectShadows` (`uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`ResourceObjectAttributes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`ResourceObjectAttributes` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`ResourceObjectAttributes` (
  `uuid` VARCHAR(36) NOT NULL ,
  `attrname` VARCHAR(255) NOT NULL ,
  `attrvalue` VARCHAR(255) NOT NULL ,
  CONSTRAINT `FK_ResourceObjectAttributes`
    FOREIGN KEY (`uuid` )
    REFERENCES `OpenIDM`.`ResourceObjectShadows` (`uuid` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `FK_ResourceObjectAttributes` ON `OpenIDM`.`ResourceObjectAttributes` (`uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`ResourcesStates`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`ResourcesStates` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`ResourcesStates` (
  `uuid` VARCHAR(36) NOT NULL ,
  `name` VARCHAR(128) NULL ,
  `state` TEXT(32000) NULL ,
  `resource_uuid` VARCHAR(36) NOT NULL ,
  PRIMARY KEY (`uuid`) ,
  CONSTRAINT `FK_Resources_States`
    FOREIGN KEY (`uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `FK_Resources` ON `OpenIDM`.`ResourcesStates` (`uuid` ASC) ;

CREATE INDEX `FK_Resources_States_Resources` ON `OpenIDM`.`ResourcesStates` (`resource_uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`UserTemplates`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`UserTemplates` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`UserTemplates` (
  `uuid` VARCHAR(36) NOT NULL ,
  `name` VARCHAR(128) NOT NULL ,
  `template` TEXT(32000) NOT NULL ,
  PRIMARY KEY (`uuid`) ,
  CONSTRAINT `FK_objects`
    FOREIGN KEY (`uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `FK_objects` ON `OpenIDM`.`UserTemplates` (`uuid` ASC) ;


-- -----------------------------------------------------
-- Table `openidm`.`objects`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`Objects` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`Objects` (
  `uuid` VARCHAR(36) NOT NULL ,
  `version` INT NULL ,
  `repomod` DATETIME NOT NULL ,
  `dtype` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`uuid`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `OpenIDM`.`Objects_additionalNames`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`Objects_additionalNames` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`Objects_additionalNames` (
  `Objects_uuid` CHAR(36) NOT NULL ,
  `element` VARCHAR(255) NULL DEFAULT NULL ,
  `index_position` INT(11) NULL DEFAULT NULL ,
  CONSTRAINT `FK831D3A16B47C44B`
    FOREIGN KEY (`Objects_uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

CREATE INDEX `FK831D3A16B47C44B` ON `OpenIDM`.`Objects_additionalNames` (`Objects_uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`Objects_EMailAddress`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`Objects_EMailAddress` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`Objects_EMailAddress` (
  `Objects_uuid` CHAR(36) NOT NULL ,
  `element` VARCHAR(255) NULL DEFAULT NULL ,
  `index_position` INT(11) NULL DEFAULT NULL ,
  CONSTRAINT `FK7D414203B47C44B`
    FOREIGN KEY (`Objects_uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

CREATE INDEX `FK7D414203B47C44B` ON `OpenIDM`.`Objects_EMailAddress` (`Objects_uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`Objects_employeeType`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`Objects_employeeType` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`Objects_employeeType` (
  `Objects_uuid` CHAR(36) NOT NULL ,
  `element` VARCHAR(255) NULL DEFAULT NULL ,
  `index_position` INT(11) NULL DEFAULT NULL ,
  CONSTRAINT `FKF2A89C73B47C44B`
    FOREIGN KEY (`Objects_uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

CREATE INDEX `FKF2A89C73B47C44B` ON `OpenIDM`.`Objects_employeeType` (`Objects_uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`Objects_organizationalUnit`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`Objects_organizationalUnit` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`Objects_organizationalUnit` (
  `Objects_uuid` CHAR(36) NOT NULL ,
  `element` VARCHAR(255) NULL DEFAULT NULL ,
  `index_position` INT(11) NULL DEFAULT NULL ,
  CONSTRAINT `FKC356046DB47C44B`
    FOREIGN KEY (`Objects_uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

CREATE INDEX `FKC356046DB47C44B` ON `OpenIDM`.`Objects_organizationalUnit` (`Objects_uuid` ASC) ;


-- -----------------------------------------------------
-- Table `OpenIDM`.`Objects_telephoneNumber`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `OpenIDM`.`Objects_telephoneNumber` ;

CREATE  TABLE IF NOT EXISTS `OpenIDM`.`Objects_telephoneNumber` (
  `Objects_uuid` CHAR(36) NOT NULL ,
  `element` VARCHAR(255) NULL DEFAULT NULL ,
  `index_position` INT(11) NULL DEFAULT NULL ,
  CONSTRAINT `FKC04BB322B47C44B`
    FOREIGN KEY (`Objects_uuid` )
    REFERENCES `OpenIDM`.`Objects` (`uuid` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

CREATE INDEX `FKC04BB322B47C44B` ON `OpenIDM`.`Objects_telephoneNumber` (`Objects_uuid` ASC) ;


;
-- ------------------------------------------------------------
-- Create Administrator account with password 'secret'
-- ------------------------------------------------------------
INSERT INTO `OpenIDM`.`Objects` (uuid,version,repomod,dtype)
	VALUES ("e9a1a3b1-9457-468e-852f-419c272ed69b",0,"2011-02-01 12:48:20","User");
INSERT INTO `OpenIDM`.`Users` (uuid,name,familyName,fullName,givenName)
	VALUES ("e9a1a3b1-9457-468e-852f-419c272ed69b","Administrator","Administrator","OpenIDM Administrator","OpenIDM");
INSERT INTO `OpenIDM`.`StringProperties` (uuid,attrname,value) 
	VALUES ("63d5b0f2-9752-41c2-8e3a-66ebad5002a3","credentials",'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<credentials xmlns="http://openidm.forgerock.com/xml/ns/public/common/common-1.xsd">
    <password>
        <c:hash xmlns:ns3="http://openidm.forgerock.com/xml/ns/public/common/common-1.xsd"
		xmlns:c="http://openidm.forgerock.com/xml/ns/public/common/common-1.xsd"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b</c:hash>
    </password>
    <allowedIdmGuiAccess>true</allowedIdmGuiAccess>
</credentials>
');
INSERT INTO `OpenIDM`.`Objects_Properties` (object_uuid,property_type,property_uuid,property_index) 
	VALUES ("e9a1a3b1-9457-468e-852f-419c272ed69b","S","63d5b0f2-9752-41c2-8e3a-66ebad5002a3",0);


-- --------------------------------------------------------------
-- Create user OPENIDM_PROXY, grant privileges for schema OpenIDM
-- --------------------------------------------------------------
DROP USER 'OPENIDM_PROXY'@'localhost';
DROP USER 'OPENIDM_PROXY'@'%';
CREATE USER 'OPENIDM_PROXY'@'localhost' IDENTIFIED BY 'Egy42-eslaposTalpu_GUMIcsIzMA';
GRANT ALL PRIVILEGES ON `OpenIDM`.* TO 'OPENIDM_PROXY'@'localhost';
CREATE USER 'OPENIDM_PROXY'@'%' IDENTIFIED BY 'Egy42-eslaposTalpu_GUMIcsIzMA';
GRANT ALL PRIVILEGES ON `OpenIDM`.* TO 'OPENIDM_PROXY'@'%';
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
