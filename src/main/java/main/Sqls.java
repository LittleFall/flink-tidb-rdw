package main;

public class Sqls {
    static String getDDL(String tableName) {
        return getDDL(tableName, tableName);
    }
    static String getDDL(String tableName, String schemaName) {
        String ddl = "";
        ddl += "create table "+tableName+"(\n"; // name part
        ddl += getSchemaPart(schemaName); // schema part
        ddl += ")";
        return ddl;
    }
    static String KafkaSource(String host, String database, String table) {
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
    static String JDBCSink(String host, String database, String table) {
        return " WITH (\n" +
                "\t'connector'  = 'jdbc',\n" +
                "\t'url'        = 'jdbc:mysql://"+host+":4000/"+database+"?rewriteBatchedStatements=true',\n" +
                "\t'table-name' = '"+table+"',\n" +
                "\t'driver'     = 'com.mysql.cj.jdbc.Driver',\n" +
                "\t'username'   = 'root',\n" +
                "\t'password'   = ''\n" +
                ")";
    }
    static String PrintSink() {
        return " WITH ('connector' = 'print')";
    }

    static String getSchemaPart(String tableName) {
        switch (tableName) {
            case "base": return
                    "\tbase_id int primary key,\n" +
                    "\tbase_location varchar(20)\n";
            case "stuff": return
                    "\tstuff_id int primary key,\n" +
                    "\tstuff_base_id int,\n" +
                    "\tstuff_name varchar(20)\n";
            case "wide_stuff": return
                    "\tstuff_id int primary key,\n" +
                    "\tbase_id int,\n" +
                    "\tbase_location varchar(20),\n" +
                    "\tstuff_name varchar(20)\n";

            case "customer": return
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
                    "  `c_data` varchar(500) NULL\n";
            case "district": return
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
                    "  `d_next_o_id` int NULL\n";
            case "history": return
                    "  `h_c_id` int NOT NULL,\n" +
                    "  `h_c_d_id` int NOT NULL,\n" +
                    "  `h_c_w_id` int NOT NULL,\n" +
                    "  `h_d_id` int NOT NULL,\n" +
                    "  `h_w_id` int NOT NULL,\n" +
                    "  `h_date` date NULL,\n" +
                    "  `h_amount` decimal(6, 2) NULL,\n" +
                    "  `h_data` varchar(24) NULL\n";
            case "item": return
                    "  `i_id` int NOT NULL,\n" +
                    "  `i_im_id` int NULL,\n" +
                    "  `i_name` varchar(24) NULL,\n" +
                    "  `i_price` decimal(5, 2) NULL,\n" +
                    "  `i_data` varchar(50) NULL\n";
            case "new_order": return
                    "  `no_o_id` int NOT NULL,\n" +
                    "  `no_d_id` int NOT NULL,\n" +
                    "  `no_w_id` int NOT NULL\n";
            case "order_line": return
                    "  `ol_o_id` int NOT NULL,\n" +
                    "  `ol_d_id` int NOT NULL,\n" +
                    "  `ol_w_id` int NOT NULL,\n" +
                    "  `ol_number` int NOT NULL,\n" +
                    "  `ol_i_id` int NOT NULL,\n" +
                    "  `ol_supply_w_id` int NULL,\n" +
                    "  `ol_delivery_d` date NULL,\n" +
                    "  `ol_quantity` int NULL,\n" +
                    "  `ol_amount` decimal(6, 2) NULL,\n" +
                    "  `ol_dist_info` char(24) NULL\n";
            case "orders": return
                    "  `o_id` int NOT NULL,\n" +
                    "  `o_d_id` int NOT NULL,\n" +
                    "  `o_w_id` int NOT NULL,\n" +
                    "  `o_c_id` int NULL,\n" +
                    "  `o_entry_d` date NULL,\n" +
                    "  `o_carrier_id` int NULL,\n" +
                    "  `o_ol_cnt` int NULL,\n" +
                    "  `o_all_local` int NULL\n";
            case "stock": return
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
                    "  `s_data` varchar(50) NULL\n";
            case "warehouse": return
                    "  `w_id` int NOT NULL,\n" +
                    "  `w_name` varchar(10) NULL,\n" +
                    "  `w_street_1` varchar(20) NULL,\n" +
                    "  `w_street_2` varchar(20) NULL,\n" +
                    "  `w_city` varchar(20) NULL,\n" +
                    "  `w_state` char(2) NULL,\n" +
                    "  `w_zip` char(9) NULL,\n" +
                    "  `w_tax` decimal(4, 4) NULL,\n" +
                    "  `w_ytd` decimal(12, 2) NULL\n";
            case "wide_customer_warehouse": return
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
                    "  PRIMARY KEY (`c_w_id`, `c_d_id`, `c_id`) NOT ENFORCED\n";
            case "wide_new_order": return
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
                    "  PRIMARY KEY (`no_o_id`) NOT ENFORCED\n";
            case "wide_order_line_district": return
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
                    "  PRIMARY KEY (`ol_w_id`, `ol_d_id`, `ol_o_id`, `ol_number`) NOT ENFORCED\n";
        }
        return "";
    }
}
