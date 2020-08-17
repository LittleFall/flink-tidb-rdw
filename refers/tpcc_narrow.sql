-- test

DROP DATABASE IF EXISTS test;
CREATE DATABASE test; 
USE test;

create table base (
  base_id int primary key,
  base_location varchar(20)
);
create table stuff(
  stuff_id int primary key,
  stuff_base_id int,
  stuff_name varchar(20)
);


-- tpcc

DROP DATABASE IF EXISTS tpcc;
CREATE DATABASE tpcc; 
USE tpcc;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for customer
-- ----------------------------
CREATE TABLE `customer`  (
  `c_id` int NOT NULL,
  `c_d_id` int NOT NULL,
  `c_w_id` int NOT NULL,
  `c_first` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `c_middle` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `c_last` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `c_street_1` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `c_street_2` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `c_city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `c_state` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `c_zip` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `c_phone` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `c_since` datetime(0) NULL DEFAULT NULL,
  `c_credit` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `c_credit_lim` decimal(12, 2) NULL DEFAULT NULL,
  `c_discount` decimal(4, 4) NULL DEFAULT NULL,
  `c_balance` decimal(12, 2) NULL DEFAULT NULL,
  `c_ytd_payment` decimal(12, 2) NULL DEFAULT NULL,
  `c_payment_cnt` int NULL DEFAULT NULL,
  `c_delivery_cnt` int NULL DEFAULT NULL,
  `c_data` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`c_w_id`, `c_d_id`, `c_id`) USING BTREE,
  INDEX `idx_customer`(`c_w_id`, `c_d_id`, `c_last`, `c_first`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for district
-- ----------------------------
CREATE TABLE `district`  (
  `d_id` int NOT NULL,
  `d_w_id` int NOT NULL,
  `d_name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `d_street_1` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `d_street_2` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `d_city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `d_state` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `d_zip` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `d_tax` decimal(4, 4) NULL DEFAULT NULL,
  `d_ytd` decimal(12, 2) NULL DEFAULT NULL,
  `d_next_o_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`d_w_id`, `d_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for history
-- ----------------------------
CREATE TABLE `history`  (
  `h_c_id` int NOT NULL,
  `h_c_d_id` int NOT NULL,
  `h_c_w_id` int NOT NULL,
  `h_d_id` int NOT NULL,
  `h_w_id` int NOT NULL,
  `h_date` datetime(0) NULL DEFAULT NULL,
  `h_amount` decimal(6, 2) NULL DEFAULT NULL,
  `h_data` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  INDEX `idx_h_w_id`(`h_w_id`) USING BTREE,
  INDEX `idx_h_c_w_id`(`h_c_w_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for item
-- ----------------------------
CREATE TABLE `item`  (
  `i_id` int NOT NULL,
  `i_im_id` int NULL DEFAULT NULL,
  `i_name` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `i_price` decimal(5, 2) NULL DEFAULT NULL,
  `i_data` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`i_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for new_order
-- ----------------------------
CREATE TABLE `new_order`  (
  `no_o_id` int NOT NULL,
  `no_d_id` int NOT NULL,
  `no_w_id` int NOT NULL,
  PRIMARY KEY (`no_w_id`, `no_d_id`, `no_o_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for order_line
-- ----------------------------
CREATE TABLE `order_line`  (
  `ol_o_id` int NOT NULL,
  `ol_d_id` int NOT NULL,
  `ol_w_id` int NOT NULL,
  `ol_number` int NOT NULL,
  `ol_i_id` int NOT NULL,
  `ol_supply_w_id` int NULL DEFAULT NULL,
  `ol_delivery_d` datetime(0) NULL DEFAULT NULL,
  `ol_quantity` int NULL DEFAULT NULL,
  `ol_amount` decimal(6, 2) NULL DEFAULT NULL,
  `ol_dist_info` char(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`ol_w_id`, `ol_d_id`, `ol_o_id`, `ol_number`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
CREATE TABLE `orders`  (
  `o_id` int NOT NULL,
  `o_d_id` int NOT NULL,
  `o_w_id` int NOT NULL,
  `o_c_id` int NULL DEFAULT NULL,
  `o_entry_d` datetime(0) NULL DEFAULT NULL,
  `o_carrier_id` int NULL DEFAULT NULL,
  `o_ol_cnt` int NULL DEFAULT NULL,
  `o_all_local` int NULL DEFAULT NULL,
  PRIMARY KEY (`o_w_id`, `o_d_id`, `o_id`) USING BTREE,
  INDEX `idx_order`(`o_w_id`, `o_d_id`, `o_c_id`, `o_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for stock
-- ----------------------------
CREATE TABLE `stock`  (
  `s_i_id` int NOT NULL,
  `s_w_id` int NOT NULL,
  `s_quantity` int NULL DEFAULT NULL,
  `s_dist_01` char(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `s_dist_02` char(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `s_dist_03` char(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `s_dist_04` char(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `s_dist_05` char(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `s_dist_06` char(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `s_dist_07` char(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `s_dist_08` char(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `s_dist_09` char(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `s_dist_10` char(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `s_ytd` int NULL DEFAULT NULL,
  `s_order_cnt` int NULL DEFAULT NULL,
  `s_remote_cnt` int NULL DEFAULT NULL,
  `s_data` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`s_w_id`, `s_i_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for warehouse
-- ----------------------------
CREATE TABLE `warehouse`  (
  `w_id` int NOT NULL,
  `w_name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `w_street_1` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `w_street_2` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `w_city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `w_state` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `w_zip` char(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `w_tax` decimal(4, 4) NULL DEFAULT NULL,
  `w_ytd` decimal(12, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`w_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;
