-- Create Addresses Table (required for the Users table's foreign key)
CREATE TABLE addresses (
       id SERIAL PRIMARY KEY,
       city VARCHAR(50) NOT NULL,
       country VARCHAR(50) NOT NULL,
       zip_code VARCHAR(10) NOT NULL,
       address_line TEXT NOT NULL
);

-- Create Users Table
CREATE TABLE users (
       id SERIAL PRIMARY KEY,
       first_name VARCHAR(50) NOT NULL,
       last_name VARCHAR(50) NOT NULL,
       email VARCHAR(100) UNIQUE NOT NULL,
       password VARCHAR(255) NOT NULL,
       phone_number VARCHAR(20) UNIQUE NOT NULL,
       role VARCHAR(20) NOT NULL CHECK (role IN ('ServiceSeeker', 'ServiceProvider')),
       education VARCHAR(100), -- Nullable for ServiceSeeker
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       address_id INT UNIQUE NOT NULL, -- One-to-One constraint
       FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE CASCADE
);

-- Create Service Categories Table
CREATE TABLE service_categories (
        id SERIAL PRIMARY KEY,
        name VARCHAR(50) UNIQUE NOT NULL
);

-- Create Service Areas Table
CREATE TABLE service_areas (
       id SERIAL PRIMARY KEY,
       category_id INT NOT NULL,
       name VARCHAR(50) NOT NULL,
       FOREIGN KEY (category_id) REFERENCES service_categories(id) ON DELETE CASCADE,
       UNIQUE (category_id, name)
);

-- Create Profiles Table
CREATE TABLE profiles (
      id SERIAL PRIMARY KEY,
      user_id INT NOT NULL,
      service_category_id INT NOT NULL,
      work_experience VARCHAR(50) NOT NULL,
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
      FOREIGN KEY (service_category_id) REFERENCES service_categories(id) ON DELETE CASCADE
);

-- Link Profiles to Service Areas (many-to-many relationship)
CREATE TABLE profile_service_areas (
       profile_id INT NOT NULL,
       service_area_id INT NOT NULL,
       PRIMARY KEY (profile_id, service_area_id),
       FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
       FOREIGN KEY (service_area_id) REFERENCES service_areas(id) ON DELETE CASCADE
);

-- Create Languages Table
CREATE TABLE languages (
       id SERIAL PRIMARY KEY,
       user_id INT NOT NULL,
       language VARCHAR(10) NOT NULL,
       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
       UNIQUE (user_id, language)
);