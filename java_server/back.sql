/*
Navicat MySQL Data Transfer

Source Server         : aliyun
Source Server Version : 50718
Source Host           : rm-bp1x3aaq3gtslx8h77o.mysql.rds.aliyuncs.com:3306
Source Database       : db_sandu

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2018-06-28 20:20:05
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for chatmsg
-- ----------------------------
DROP TABLE IF EXISTS `chatmsg`;
CREATE TABLE `chatmsg` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `from_id` varchar(64) CHARACTER SET utf8mb4 NOT NULL,
  `to_id` varchar(64) CHARACTER SET utf8mb4 NOT NULL,
  `msg` text COLLATE utf8mb4_unicode_ci,
  `time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_read` smallint(6) DEFAULT '0',
  `relative_order_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `order_id` varchar(64) NOT NULL,
  `create_user` varchar(64) NOT NULL,
  `city_name` varchar(16) DEFAULT NULL,
  `district_name` varchar(32) DEFAULT NULL,
  `from_addr` varchar(64) NOT NULL,
  `from_addr_detai` varchar(64) DEFAULT NULL,
  `from_addr_longitude` varchar(64) DEFAULT NULL,
  `from_addr_latitude` varchar(64) DEFAULT NULL,
  `to_addr` varchar(64) DEFAULT NULL,
  `to_addr_detai` varchar(64) DEFAULT NULL COMMENT '留言',
  `to_addr_longitude` varchar(64) DEFAULT NULL,
  `to_addr_latitude` varchar(64) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT '0000-00-00 00:00:00.000000',
  `depart_time` datetime(6) DEFAULT '0000-00-00 00:00:00.000000',
  `order_type` smallint(2) DEFAULT '0',
  `order_status` smallint(6) DEFAULT NULL,
  `order_price` float(6,2) DEFAULT NULL,
  `records` text,
  `receive_user` varchar(64) DEFAULT NULL,
  `star_by_passenger` smallint(6) DEFAULT '5',
  `star_by_driver` smallint(6) DEFAULT '5',
  `rcv_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`,`create_user`),
  KEY `create_time_index` (`from_addr_detai`),
  KEY `campus_order` (`from_addr`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` varchar(64) NOT NULL,
  `phone` varchar(64) DEFAULT NULL,
  `type` smallint(6) DEFAULT NULL,
  `create_time` date NOT NULL COMMENT '注册时间',
  `nickname` varchar(255) DEFAULT NULL,
  `img_url` varchar(255) DEFAULT NULL,
  `last_login_date` date DEFAULT NULL,
  `sex` smallint(1) DEFAULT NULL,
  `academy` varchar(50) DEFAULT NULL,
  `san_color` varchar(16) DEFAULT NULL,
  `san_style` varchar(16) DEFAULT NULL,
  `san_feature` varchar(64) DEFAULT NULL,
  `passenger_star` float(6,2) DEFAULT '5.00',
  `driver_star` float(6,2) DEFAULT '5.00',
  `ballance` float(10,2) DEFAULT '0.00',
  `last_city` varchar(16) DEFAULT NULL,
  `last_district` varchar(32) DEFAULT NULL,
  `last_longitude` varchar(64) DEFAULT NULL,
  `last_latitude` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- View structure for msg_user
-- ----------------------------
DROP VIEW IF EXISTS `msg_user`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `msg_user` AS select `chatmsg`.`id` AS `id`,`chatmsg`.`from_id` AS `from_id`,`chatmsg`.`to_id` AS `to_id`,`chatmsg`.`msg` AS `msg`,`chatmsg`.`time` AS `time`,`chatmsg`.`is_read` AS `is_read`,`users`.`img_url` AS `from_head_url`,`tmp`.`img_url` AS `to_head_url`,`chatmsg`.`relative_order_id` AS `relative_order_id` from ((`chatmsg` left join `users` on((`chatmsg`.`from_id` = convert(`users`.`user_id` using utf8mb4)))) left join `users` `tmp` on((`chatmsg`.`to_id` = convert(`tmp`.`user_id` using utf8mb4)))) ;
