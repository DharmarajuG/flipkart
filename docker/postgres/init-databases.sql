-- Creates one database per service on first container start.
-- Mounted into /docker-entrypoint-initdb.d, so it only runs when the
-- postgres data volume is empty (first boot). Drop the volume to re-init.
CREATE DATABASE user_db;
CREATE DATABASE product_db;
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE inventory_db;
