/*
MySQL Backup
Source Server Version: 5.7.20
Source Database: db_aidu_info
Date: 2018/2/1 星期四 19:20:35
*/

SET FOREIGN_KEY_CHECKS=0;


-- ----------------------------
--  Table structure for `users`
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` varchar(64) NOT NULL,
  `password` varchar(50) DEFAULT NULL,
  `type` smallint(6) DEFAULT NULL,
  `create_time` date NOT NULL COMMENT '注册时间',
  `nickname` varchar(255) DEFAULT NULL,
  `img_url` varchar(255) DEFAULT NULL,
  `last_login_date` date DEFAULT NULL,
  `token` varchar(256) DEFAULT NULL,
  `sex` smallint(1) DEFAULT NULL,
  `academy` varchar(50) DEFAULT NULL,
  `qq` varchar(50) DEFAULT NULL,
  `weixin` varchar(50) DEFAULT NULL,
  `mail` varchar(255) DEFAULT NULL,
  `campus_id` int(11) DEFAULT NULL,
  `last_campus` int(11) DEFAULT '1',
  `phone` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `mail` (`mail`),
  UNIQUE KEY `mail_2` (`mail`),
  KEY `campus_index` (`campus_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

