Setting Up MySQL
================

1. Create cloudraid user: CREATE USER 'cloudraid'@'localhost' IDENTIFIED BY 'cloudraid';
2. Create cloudraid database: CREATE DATABASE cloudraid;
3. Grant permissions to user: GRANT ALL PRIVILEGES ON cloudraid.* TO 'cloudraid'@'localhost' WITH GRANT OPTION;

Building Server
===============

1. Run mvn clean install.
2. Run ant buildServer.

Starting Server (In Linux)
==========================

2. In PROJECT/build, run sudo ./runsrv.sh.

