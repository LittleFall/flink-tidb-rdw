package main;

public class Sqls {
    static String getCreateTableSql(String tableName) {
        switch (tableName) {
            case "customer": return TPCC_CreateCustomer;
            case "district": return TPCC_CreateDistrict;
            case "history": return TPCC_CreateHistory;
            case "item": return TPCC_CreateItem;
            case "new_order": return TPCC_CreateNewOrder;
            case "order_line": return TPCC_CreateOrderLine;
            case "orders": return TPCC_CreateOrders;
            case "stock": return TPCC_CreateStock;
            case "warehouse": return TPCC_CreateWarehouse;

            case "wide_customer_warehouse": return TPCCWideCustomerWarehouse;
            case "wide_new_order": return TPCCWideNewOrder;
            case "wide_order_line_district": return TPCCWideOrderLineDistrict;

            case "base": return testCreateBase;
            case "stuff": return testCreateStuff;
            case "wide_stuff": return testCreateWideStuff;

        }
        return "";
    }
    static String getSourceWith(String host, String database, String table) {
        return " WITH (\n" +
                "\t'connector' = 'kafka',\n" +
                "\t'topic' = '"+database+"-"+table+"',\n" +
                "\t'properties.group.id' = 'testGroup',\n" +
                "\t'scan.startup.mode' = 'latest-offset',\n" +
                "\t'properties.bootstrap.servers' = '"+host+":9092',\n" +
                "\t'format' = 'canal-json',\n" +
                "\t'canal-json.ignore-parse-errors'='true'\n" +
                ")";
    }
    static String getSinkWith(String host, String database, String table) {
        return " WITH (\n" +
                "\t'connector'  = 'jdbc',\n" +
                "\t'url'        = 'jdbc:mysql://"+host+":4000/"+database+"',\n" +
                "\t'table-name' = '"+table+"',\n" +
                "\t'driver'     = 'com.mysql.cj.jdbc.Driver',\n" +
                "\t'username'   = 'root',\n" +
                "\t'password'   = ''\n" +
                ")";
    }

    static String testCreateBase = "create table base (\n" +
                "\tbase_id int primary key,\n" +
                "\tbase_location varchar(20)\n" +
                ")";
    static String testCreateStuff = "create table stuff(\n" +
                "\tstuff_id int primary key,\n" +
                "\tstuff_base_id int,\n" +
                "\tstuff_name varchar(20)\n" +
                ")";
    static String testCreateWideStuff = "create table wide_stuff(\n" +
                "\tstuff_id int primary key,\n" +
                "\tbase_id int,\n" +
                "\tbase_location varchar(20),\n" +
                "\tstuff_name varchar(20)\n" +
                ")";

    static String TPCC_CreateCustomer = "CREATE TABLE `customer`  (\n" +
            "  `c_id` int NOT NULL,\n" +
            "  `c_d_id` int NOT NULL,\n" +
            "  `c_w_id` int NOT NULL,\n" +
            "  `c_first` varchar(16) NULL,\n" +
            "  `c_middle` char(2) NULL,\n" +
            "  `c_last` varchar(16) NULL,\n" +
            "  `c_street_1` varchar(20) NULL,\n" +
            "  `c_street_2` varchar(20) NULL,\n" +
            "  `c_city` varchar(20) NULL,\n" +
            "  `c_state` char(2) NULL,\n" +
            "  `c_zip` char(9) NULL,\n" +
            "  `c_phone` char(16) NULL,\n" +
            "  `c_since` date NULL,\n" +
            "  `c_credit` char(2) NULL,\n" +
            "  `c_credit_lim` decimal(12, 2) NULL,\n" +
            "  `c_discount` decimal(4, 4) NULL,\n" +
            "  `c_balance` decimal(12, 2) NULL,\n" +
            "  `c_ytd_payment` decimal(12, 2) NULL,\n" +
            "  `c_payment_cnt` int NULL,\n" +
            "  `c_delivery_cnt` int NULL,\n" +
            "  `c_data` varchar(500) NULL\n" +
            ")";
    static String TPCC_CreateDistrict = "CREATE TABLE `district`  (\n" +
            "  `d_id` int NOT NULL,\n" +
            "  `d_w_id` int NOT NULL,\n" +
            "  `d_name` varchar(10) NULL,\n" +
            "  `d_street_1` varchar(20) NULL,\n" +
            "  `d_street_2` varchar(20) NULL,\n" +
            "  `d_city` varchar(20) NULL,\n" +
            "  `d_state` char(2) NULL,\n" +
            "  `d_zip` char(9) NULL,\n" +
            "  `d_tax` decimal(4, 4) NULL,\n" +
            "  `d_ytd` decimal(12, 2) NULL,\n" +
            "  `d_next_o_id` int NULL\n" +
            ")";
    static String TPCC_CreateHistory = "CREATE TABLE `history`  (\n" +
            "  `h_c_id` int NOT NULL,\n" +
            "  `h_c_d_id` int NOT NULL,\n" +
            "  `h_c_w_id` int NOT NULL,\n" +
            "  `h_d_id` int NOT NULL,\n" +
            "  `h_w_id` int NOT NULL,\n" +
            "  `h_date` date NULL,\n" +
            "  `h_amount` decimal(6, 2) NULL,\n" +
            "  `h_data` varchar(24) NULL\n" +
            ")";
    static String TPCC_CreateItem = "CREATE TABLE `item`  (\n" +
            "  `i_id` int NOT NULL,\n" +
            "  `i_im_id` int NULL,\n" +
            "  `i_name` varchar(24) NULL,\n" +
            "  `i_price` decimal(5, 2) NULL,\n" +
            "  `i_data` varchar(50) NULL\n" +
            ")";
    static String TPCC_CreateNewOrder = "CREATE TABLE `new_order`  (\n" +
            "  `no_o_id` int NOT NULL,\n" +
            "  `no_d_id` int NOT NULL,\n" +
            "  `no_w_id` int NOT NULL\n" +
            ")";
    static String TPCC_CreateOrderLine = "CREATE TABLE `order_line`  (\n" +
            "  `ol_o_id` int NOT NULL,\n" +
            "  `ol_d_id` int NOT NULL,\n" +
            "  `ol_w_id` int NOT NULL,\n" +
            "  `ol_number` int NOT NULL,\n" +
            "  `ol_i_id` int NOT NULL,\n" +
            "  `ol_supply_w_id` int NULL,\n" +
            "  `ol_delivery_d` date NULL,\n" +
            "  `ol_quantity` int NULL,\n" +
            "  `ol_amount` decimal(6, 2) NULL,\n" +
            "  `ol_dist_info` char(24) NULL\n" +
            ")";
    static String TPCC_CreateOrders = "CREATE TABLE `orders`  (\n" +
            "  `o_id` int NOT NULL,\n" +
            "  `o_d_id` int NOT NULL,\n" +
            "  `o_w_id` int NOT NULL,\n" +
            "  `o_c_id` int NULL,\n" +
            "  `o_entry_d` date NULL,\n" +
            "  `o_carrier_id` int NULL,\n" +
            "  `o_ol_cnt` int NULL,\n" +
            "  `o_all_local` int NULL\n" +
            ")";
    static String TPCC_CreateStock = "CREATE TABLE `stock`  (\n" +
            "  `s_i_id` int NOT NULL,\n" +
            "  `s_w_id` int NOT NULL,\n" +
            "  `s_quantity` int NULL,\n" +
            "  `s_dist_01` char(24) NULL,\n" +
            "  `s_dist_02` char(24) NULL,\n" +
            "  `s_dist_03` char(24) NULL,\n" +
            "  `s_dist_04` char(24) NULL,\n" +
            "  `s_dist_05` char(24) NULL,\n" +
            "  `s_dist_06` char(24) NULL,\n" +
            "  `s_dist_07` char(24) NULL,\n" +
            "  `s_dist_08` char(24) NULL,\n" +
            "  `s_dist_09` char(24) NULL,\n" +
            "  `s_dist_10` char(24) NULL,\n" +
            "  `s_ytd` int NULL,\n" +
            "  `s_order_cnt` int NULL,\n" +
            "  `s_remote_cnt` int NULL,\n" +
            "  `s_data` varchar(50) NULL\n" +
            ")";
    static String TPCC_CreateWarehouse = "CREATE TABLE `warehouse`  (\n" +
            "  `w_id` int NOT NULL,\n" +
            "  `w_name` varchar(10) NULL,\n" +
            "  `w_street_1` varchar(20) NULL,\n" +
            "  `w_street_2` varchar(20) NULL,\n" +
            "  `w_city` varchar(20) NULL,\n" +
            "  `w_state` char(2) NULL,\n" +
            "  `w_zip` char(9) NULL,\n" +
            "  `w_tax` decimal(4, 4) NULL,\n" +
            "  `w_ytd` decimal(12, 2) NULL\n" +
            ")";
    static String TPCCWideCustomerWarehouse = "CREATE TABLE `wide_customer_warehouse`  (\n" +
            "  `c_id` int NOT NULL,\n" +
            "  `c_d_id` int NOT NULL,\n" +
            "  `c_w_id` int NOT NULL,\n" +
            "  `c_first` varchar(16) NULL,\n" +
            "  `c_middle` char(2) NULL,\n" +
            "  `c_last` varchar(16) NULL,\n" +
            "  `c_street_1` varchar(20) NULL,\n" +
            "  `c_street_2` varchar(20) NULL,\n" +
            "  `c_city` varchar(20) NULL,\n" +
            "  `c_state` char(2) NULL,\n" +
            "  `c_zip` char(9) NULL,\n" +
            "  `c_phone` char(16) NULL,\n" +
            "  `c_since` date NULL,\n" +
            "  `c_credit` char(2) NULL,\n" +
            "  `c_credit_lim` decimal(12, 2) NULL,\n" +
            "  `c_discount` decimal(4, 4) NULL,\n" +
            "  `c_balance` decimal(12, 2) NULL,\n" +
            "  `c_ytd_payment` decimal(12, 2) NULL,\n" +
            "  `c_payment_cnt` int NULL,\n" +
            "  `c_delivery_cnt` int NULL,\n" +
            "  `c_data` varchar(500) NULL,\n" +
            "  `w_name` varchar(10) NULL,\n" +
            "  `w_street_1` varchar(20) NULL,\n" +
            "  `w_street_2` varchar(20) NULL,\n" +
            "  `w_city` varchar(20) NULL,\n" +
            "  `w_state` char(2) NULL,\n" +
            "  `w_zip` char(9) NULL,\n" +
            "  `w_tax` decimal(4, 4) NULL,\n" +
            "  `w_ytd` decimal(12, 2) NULL,\n" +
            "  PRIMARY KEY (`c_w_id`, `c_d_id`, `c_id`) NOT ENFORCED\n" +
            ")";
    static String TPCCWideNewOrder = "CREATE TABLE `wide_new_order`  (\n" +
            "  `no_o_id` int NOT NULL,\n" +
            "  `no_d_id` int NOT NULL,\n" +
            "  `no_w_id` int NOT NULL,\n" +
            "  `d_name` varchar(10) NULL,\n" +
            "  `d_street_1` varchar(20) NULL,\n" +
            "  `d_street_2` varchar(20) NULL,\n" +
            "  `d_city` varchar(20) NULL,\n" +
            "  `d_state` char(2) NULL,\n" +
            "  `d_zip` char(9) NULL,\n" +
            "  `d_tax` decimal(4, 4) NULL,\n" +
            "  `d_ytd` decimal(12, 2) NULL,\n" +
            "  `d_next_o_id` int NULL,\n" +
            "  `w_name` varchar(10) NULL,\n" +
            "  `w_street_1` varchar(20) NULL,\n" +
            "  `w_street_2` varchar(20) NULL,\n" +
            "  `w_city` varchar(20) NULL,\n" +
            "  `w_state` char(2) NULL,\n" +
            "  `w_zip` char(9) NULL,\n" +
            "  `w_tax` decimal(4, 4) NULL,\n" +
            "  `w_ytd` decimal(12, 2) NULL,\n" +
            "  PRIMARY KEY (`no_o_id`) NOT ENFORCED\n" +
            ")";
    static String TPCCWideOrderLineDistrict = "CREATE TABLE `wide_order_line_district`  (\n" +
            "  `ol_o_id` int NOT NULL,\n" +
            "  `ol_d_id` int NOT NULL,\n" +
            "  `ol_w_id` int NOT NULL,\n" +
            "  `ol_number` int NOT NULL,\n" +
            "  `ol_i_id` int NOT NULL,\n" +
            "  `ol_supply_w_id` int NULL,\n" +
            "  `ol_delivery_d` date NULL,\n" +
            "  `ol_quantity` int NULL,\n" +
            "  `ol_amount` decimal(6, 2) NULL,\n" +
            "  `ol_dist_info` char(24) NULL,\n" +
            "  `d_name` varchar(10) NULL,\n" +
            "  `d_street_1` varchar(20) NULL,\n" +
            "  `d_street_2` varchar(20) NULL,\n" +
            "  `d_city` varchar(20) NULL,\n" +
            "  `d_state` char(2) NULL,\n" +
            "  `d_zip` char(9) NULL,\n" +
            "  `d_tax` decimal(4, 4) NULL,\n" +
            "  `d_ytd` decimal(12, 2) NULL,\n" +
            "  `d_next_o_id` int NULL,\n" +
            "  PRIMARY KEY (`ol_w_id`, `ol_d_id`, `ol_o_id`, `ol_number`) NOT ENFORCED\n" +
            ")";
}
