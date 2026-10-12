-- Run this script in MySQL Workbench before launching the app.
CREATE DATABASE IF NOT EXISTS smart_water_distribution CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_water_distribution;
CREATE TABLE IF NOT EXISTS reservoir_state (id VARCHAR(20) PRIMARY KEY, name VARCHAR(120) NOT NULL, capacity INT NOT NULL, remaining_supply INT NOT NULL);
CREATE TABLE IF NOT EXISTS demand_area (id VARCHAR(20) PRIMARY KEY, name VARCHAR(120) NOT NULL, demand_litres INT NOT NULL, priority_label VARCHAR(30) NOT NULL);
CREATE TABLE IF NOT EXISTS pipeline_state (id VARCHAR(20) PRIMARY KEY, from_node VARCHAR(20) NOT NULL, to_node VARCHAR(20) NOT NULL, working BOOLEAN NOT NULL);
CREATE TABLE IF NOT EXISTS water_request (id VARCHAR(40) PRIMARY KEY, area_id VARCHAR(20) NOT NULL, requested_litres INT NOT NULL, allocated_litres INT NOT NULL, unmet_litres INT NOT NULL, mode VARCHAR(80) NOT NULL, created_at VARCHAR(40) NOT NULL);
CREATE TABLE IF NOT EXISTS maintenance_request (id VARCHAR(40) PRIMARY KEY, pipeline_id VARCHAR(20) NOT NULL, description VARCHAR(255) NOT NULL, created_at VARCHAR(40) NOT NULL, resolved BOOLEAN NOT NULL);
CREATE TABLE IF NOT EXISTS allocation_history (entry_id BIGINT AUTO_INCREMENT PRIMARY KEY, entry_text TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS event_history (entry_id BIGINT AUTO_INCREMENT PRIMARY KEY, entry_text TEXT NOT NULL);
