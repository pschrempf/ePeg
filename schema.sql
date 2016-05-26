CREATE TABLE IF NOT EXISTS `studies` 
( 
`_id` INTEGER PRIMARY KEY AUTO_INCREMENT,
`study_results` TEXT NOT NULL, 
`symmetric_key` TEXT NOT NULL,
`initialisation_vector` TEXT NOT NULL,
`time_recorded` TIMESTAMP,
`device_identifier` TEXT NOT NULL,
`trial_conductor` TEXT NOT NULL,
`time_synchronised` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
