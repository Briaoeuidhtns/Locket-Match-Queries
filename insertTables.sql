-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `mydb` DEFAULT CHARACTER SET utf8 ;
USE `mydb` ;

-- -----------------------------------------------------
-- Table `mydb`.`Match`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Match` (
  `matchID` INT NOT NULL,
  `radiantWin` TINYINT NULL,
  `duration` INT NULL,
  `towerStatusDire` INT NULL,
  `towerStatusRadiant` INT NULL,
  `barracksStatusDire` INT NULL,
  `barracksStatusRadiant` INT NULL,
  `firstBloodTime` INT NULL,
  `radiantScore` INT NULL,
  `direScore` INT NULL,
  `pickBan` INT NULL,
  PRIMARY KEY (`matchID`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`LobbyInfo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`LobbyInfo` (
  `server` VARCHAR(45) NOT NULL,
  `leagueID` INT NULL,
  `Season` INT NULL,
  `startTime` INT NULL,
  `lobbyType` INT NULL,
  `matchID` INT NOT NULL,
  PRIMARY KEY (`server`),
  INDEX `fk_LobbyInfo_Match_idx` (`matchID` ASC),
  CONSTRAINT `fk_LobbyInfo_Match`
    FOREIGN KEY (`matchID`)
    REFERENCES `mydb`.`Match` (`matchID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Player Info`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Player Info` (
  `playerSlot` INT NOT NULL,
  `isRadiant` TINYINT NULL,
  `item0` INT NULL,
  `item1` INT NULL,
  `item2` INT NULL,
  `item3` INT NULL,
  `item4` INT NULL,
  `item5` INT NULL,
  `kills` INT NULL,
  `deaths` INT NULL,
  `assists` INT NULL,
  `leaverStatus` INT NULL,
  `lastHits` INT NULL,
  `denies` INT NULL,
  `goldPerMinute` DOUBLE NULL,
  `xpPerMinute` DOUBLE NULL,
  `playerID` INT NOT NULL,
  `matchID` INT NOT NULL,
  `heroID` INT NULL,
  PRIMARY KEY (`playerID`),
  INDEX `fk_Player Info_Match1_idx` (`matchID` ASC),
  CONSTRAINT `fk_Player Info_Match1`
    FOREIGN KEY (`matchID`)
    REFERENCES `mydb`.`Match` (`matchID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Heros`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Heros` (
  `heroID` INT NOT NULL,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`heroID`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`PickBan`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`PickBan` (
  `matchID` INT NOT NULL,
  INDEX `fk_PickBan_Match1_idx` (`matchID` ASC),
  PRIMARY KEY (`matchID`),
  CONSTRAINT `fk_PickBan_Match1`
    FOREIGN KEY (`matchID`)
    REFERENCES `mydb`.`Match` (`matchID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`PickBanEntry`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`PickBanEntry` (
  `isPick` TINYINT NOT NULL,
  `heroID` INT NULL,
  `isRadiant` TINYINT NULL,
  `order` INT NULL,
  `matchID` INT NOT NULL,
  PRIMARY KEY (`isPick`),
  INDEX `fk_PickBanEntry_PickBan1_idx` (`matchID` ASC),
  CONSTRAINT `fk_PickBanEntry_PickBan1`
    FOREIGN KEY (`matchID`)
    REFERENCES `mydb`.`PickBan` (`matchID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
