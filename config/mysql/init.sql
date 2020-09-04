CREATE USER canal IDENTIFIED BY 'canal';  
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%';
FLUSH PRIVILEGES;
ALTER USER 'canal'@'%' IDENTIFIED BY 'canal' PASSWORD EXPIRE NEVER;
ALTER USER 'canal'@'%' IDENTIFIED WITH mysql_native_password BY 'canal';
FLUSH HOSTS;


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
