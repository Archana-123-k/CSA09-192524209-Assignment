CREATE DATABASE IF NOT EXISTS campus_parking;
USE campus_parking;

CREATE TABLE IF NOT EXISTS parking_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  category VARCHAR(20) NOT NULL
);
CREATE TABLE IF NOT EXISTS vehicle (
  registration_number VARCHAR(24) PRIMARY KEY,
  vehicle_type VARCHAR(24) NOT NULL,
  user_id BIGINT NULL,
  FOREIGN KEY (user_id) REFERENCES parking_user(id) ON DELETE SET NULL
);
CREATE TABLE IF NOT EXISTS parking_slot (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  floor_number INT NOT NULL,
  slot_number INT NOT NULL,
  slot_type VARCHAR(24) NOT NULL,
  occupied BOOLEAN NOT NULL DEFAULT FALSE,
  vehicle_registration VARCHAR(24) NULL,
  UNIQUE KEY uq_floor_slot (floor_number, slot_number),
  FOREIGN KEY (vehicle_registration) REFERENCES vehicle(registration_number) ON DELETE SET NULL
);
CREATE TABLE IF NOT EXISTS parking_pass (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  pass_type VARCHAR(20) NOT NULL,
  valid_from TIMESTAMP NOT NULL,
  valid_to TIMESTAMP NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (user_id) REFERENCES parking_user(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS parking_ticket (
  ticket_id VARCHAR(36) PRIMARY KEY,
  registration_number VARCHAR(24) NOT NULL,
  slot_id BIGINT NOT NULL,
  user_id BIGINT NULL,
  entry_time TIMESTAMP NOT NULL,
  exit_time TIMESTAMP NULL,
  duration_minutes BIGINT NULL,
  parking_fee DECIMAL(10,2) NULL,
  charging_fee DECIMAL(10,2) NULL,
  total_fee DECIMAL(10,2) NULL,
  pass_applied BOOLEAN NOT NULL DEFAULT FALSE,
  status VARCHAR(16) NOT NULL,
  FOREIGN KEY (registration_number) REFERENCES vehicle(registration_number),
  FOREIGN KEY (slot_id) REFERENCES parking_slot(id),
  FOREIGN KEY (user_id) REFERENCES parking_user(id) ON DELETE SET NULL,
  INDEX idx_ticket_registration_status (registration_number, status)
);
CREATE TABLE IF NOT EXISTS payment_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ticket_id VARCHAR(36) NOT NULL UNIQUE,
  amount DECIMAL(10,2) NOT NULL,
  payment_method VARCHAR(24) NOT NULL,
  paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (ticket_id) REFERENCES parking_ticket(ticket_id)
);
