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
  `winner` CHAR(1) NULL,
  `duration` INT NULL,
  `towerStatusDire` INT NULL,
  `towerStatusRadiant` INT NULL,
  `barracksStatusDire` INT NULL,
  `barracksStatusRadiant` INT NULL,
  `firstBloodTime` INT NULL,
  `radiantScore` INT NULL,
  `direScore` INT NULL,
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
  `Match_matchID` INT NOT NULL,
  PRIMARY KEY (`server`),
  INDEX `fk_LobbyInfo_Match_idx` (`Match_matchID` ASC),
  CONSTRAINT `fk_LobbyInfo_Match`
    FOREIGN KEY (`Match_matchID`)
    REFERENCES `mydb`.`Match` (`matchID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`playerInfo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`playerInfo` (
  `playerID` INT NOT NULL,
  `Match_matchID` INT NOT NULL,
  `side` CHAR(1) NULL,
  PRIMARY KEY (`playerID`),
  INDEX `fk_playerInfo_Match1_idx` (`Match_matchID` ASC),
  CONSTRAINT `fk_playerInfo_Match1`
    FOREIGN KEY (`Match_matchID`)
    REFERENCES `mydb`.`Match` (`matchID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`playerMatch`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`playerMatch` (
  `playerInfo_playerID` INT NOT NULL,
  `Match_matchID` INT NOT NULL,
  INDEX `fk_playerMatch_playerInfo1_idx` (`playerInfo_playerID` ASC),
  INDEX `fk_playerMatch_Match1_idx` (`Match_matchID` ASC),
  CONSTRAINT `fk_playerMatch_playerInfo1`
    FOREIGN KEY (`playerInfo_playerID`)
    REFERENCES `mydb`.`playerInfo` (`playerID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_playerMatch_Match1`
    FOREIGN KEY (`Match_matchID`)
    REFERENCES `mydb`.`Match` (`matchID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
