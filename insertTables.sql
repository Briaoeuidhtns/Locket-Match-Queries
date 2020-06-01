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
-- Table `mydb`.`match_table`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`match_table` (
  `match_id` BIGINT(20) NOT NULL,
  `radiant_win` TINYINT NOT NULL,
  `duration` INT NOT NULL,
  `tower_status_dire` INT NOT NULL,
  `tower_status_radiant` INT NOT NULL,
  `barracks_status_dire` INT NOT NULL,
  `barracks_status_radiant` INT NOT NULL,
  `first_blood_time` INT NOT NULL,
  `radiant_score` INT NOT NULL,
  `dire_score` INT NOT NULL,
  `pick_ban_id` INT NULL,
  PRIMARY KEY (`match_id`),
  UNIQUE INDEX `match_id_UNIQUE` (`match_id` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`lobby_info`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`lobby_info` (
  `server` VARCHAR(45) CHARACTER SET 'ascii' NOT NULL,
  `league_id` INT NULL,
  `season` INT NULL,
  `start_time` INT NOT NULL,
  `lobby_type` INT NULL,
  `match_id` BIGINT(20) NOT NULL,
  INDEX `fk_lobby_info_match_table1_idx` (`match_id` ASC),
  PRIMARY KEY (`match_id`),
  CONSTRAINT `fk_lobby_info_match_table1`
    FOREIGN KEY (`match_id`)
    REFERENCES `mydb`.`match_table` (`match_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`player_info`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`player_info` (
  `player_slot` INT NOT NULL,
  `is_radiant` TINYINT NOT NULL,
  `item_0` INT NULL,
  `item_1` INT NULL,
  `item_2` INT NULL,
  `item_3` INT NULL,
  `item_4` INT NULL,
  `item_5` INT NULL,
  `kills` INT NOT NULL,
  `deaths` INT NOT NULL,
  `assists` INT NOT NULL,
  `leaverStatus` INT NOT NULL,
  `last_hits` INT NOT NULL,
  `denies` INT NOT NULL,
  `gold_per_min` DOUBLE NOT NULL,
  `xp_per_min` DOUBLE NOT NULL,
  `player_id` INT NOT NULL,
  `hero_id` INT NOT NULL,
  `backpack_1` INT NULL,
  `backpack_2` INT NULL,
  `backpack_3` INT NULL,
  `backpack_4` INT NULL,
  `match_id` BIGINT(20) NOT NULL,
  PRIMARY KEY (`player_id`, `match_id`),
  INDEX `fk_player_info_match_table1_idx` (`match_id` ASC),
  CONSTRAINT `fk_player_info_match_table1`
    FOREIGN KEY (`match_id`)
    REFERENCES `mydb`.`match_table` (`match_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`hero`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`hero` (
  `hero_id` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`hero_id`),
  UNIQUE INDEX `hero_id_UNIQUE` (`hero_id` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`pick_ban_entry`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`pick_ban_entry` (
  `is_pick` TINYINT NOT NULL,
  `hero_id` INT NOT NULL,
  `is_radiant` TINYINT NOT NULL,
  `pick_ban_order` INT NOT NULL,
  `match_id` BIGINT(20) NOT NULL,
  PRIMARY KEY (`pick_ban_order`, `match_id`),
  INDEX `fk_pick_ban_entry_match_table1_idx` (`match_id` ASC),
  CONSTRAINT `fk_pick_ban_entry_match_table1`
    FOREIGN KEY (`match_id`)
    REFERENCES `mydb`.`match_table` (`match_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`item`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`item` (
  `item_id` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `cost` INT NOT NULL,
  `secret_shop` TINYINT NOT NULL,
  `side_shop` TINYINT NOT NULL,
  `recipe` TINYINT NOT NULL,
  PRIMARY KEY (`item_id`),
  UNIQUE INDEX `item_id_UNIQUE` (`item_id` ASC))
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
