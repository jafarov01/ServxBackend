-- Create Addresses Table (required for the Users table's foreign key)
CREATE TABLE addresses
(
    id           SERIAL PRIMARY KEY,
    city         VARCHAR(50) NOT NULL,
    country      VARCHAR(50) NOT NULL,
    zip_code     VARCHAR(10) NOT NULL,
    address_line TEXT        NOT NULL
);

-- Create Users Table
CREATE TABLE users
(
    id                SERIAL PRIMARY KEY,
    first_name        VARCHAR(50)                                        NOT NULL,
    last_name         VARCHAR(50)                                        NOT NULL,
    email             VARCHAR(100) UNIQUE                                NOT NULL,
    password          VARCHAR(255)                                       NOT NULL,
    phone_number      VARCHAR(20) UNIQUE                                 NOT NULL,
    profile_photo_url VARCHAR(255) DEFAULT '/images/default-profile.jpg' NOT NULL,
    is_verified       BOOLEAN      DEFAULT FALSE                         NOT NULL,
    role              VARCHAR(20)  DEFAULT 'SERVICE_SEEKER'              NOT NULL CHECK (role IN ('SERVICE_SEEKER', 'SERVICE_PROVIDER')),
    education         VARCHAR(100),                                                -- Nullable for ServiceSeeker
    created_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    address_id        INT UNIQUE                                         NOT NULL, -- One-to-One constraint
    FOREIGN KEY (address_id) REFERENCES addresses (id) ON DELETE CASCADE
);

-- Create Service Categories Table
CREATE TABLE service_categories
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Create Service Areas Table
CREATE TABLE service_areas
(
    id          SERIAL PRIMARY KEY,
    category_id INT         NOT NULL,
    name        VARCHAR(50) NOT NULL,
    FOREIGN KEY (category_id) REFERENCES service_categories (id) ON DELETE CASCADE,
    UNIQUE (category_id, name)
);

-- Create Services Table
CREATE TABLE services
(
    id                  SERIAL PRIMARY KEY,
    user_id             INT              NOT NULL,
    service_category_id INT              NOT NULL,
    service_area_id     INT              NOT NULL,
    work_experience     VARCHAR(50)      NOT NULL,
    rating              DOUBLE PRECISION DEFAULT 0,
    review_count        INT              DEFAULT 0,
    price               DOUBLE PRECISION NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (service_category_id) REFERENCES service_categories (id) ON DELETE CASCADE,
    FOREIGN KEY (service_area_id) REFERENCES service_areas (id) ON DELETE CASCADE
);

-- Create Reviews Table
CREATE TABLE reviews
(
    id         SERIAL PRIMARY KEY,
    service_id INT              NOT NULL,
    user_id    INT              NOT NULL,
    rating     DOUBLE PRECISION NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES services (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create Languages Table
CREATE TABLE languages
(
    id       SERIAL PRIMARY KEY,
    user_id  INT         NOT NULL,
    language VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE (user_id, language)
);

-- Create the 'verification_tokens' table
CREATE TABLE verification_tokens
(
    id          SERIAL PRIMARY KEY,
    token       VARCHAR(255) UNIQUE NOT NULL,
    user_id     INT                 NOT NULL,
    expiry_date TIMESTAMP           NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Insert Service Categories
INSERT INTO service_categories (id, name)
VALUES (1, 'Cleaning'),
       (2, 'Repairing'),
       (3, 'Laundry'),
       (4, 'Painting'),
       (5, 'Appliance'),
       (6, 'Plumbing'),
       (7, 'Shifting');

-- Insert Service Areas (Subcategories)
INSERT INTO service_areas (id, category_id, name)
VALUES
    -- Cleaning
    (1, 1, 'Deep Cleaning'),
    (2, 1, 'Carpet Cleaning'),
    (3, 1, 'Window Cleaning'),
    (4, 1, 'Office Cleaning'),
    (5, 1, 'Industrial Cleaning'),
    (6, 1, 'Upholstery Cleaning'),
    (7, 1, 'Floor Cleaning'),

    -- Repairing
    (8, 2, 'Electronics Repair'),
    (9, 2, 'Appliance Repair'),
    (10, 2, 'Plumbing Repair'),
    (11, 2, 'HVAC Repair'),
    (12, 2, 'Car Repair'),
    (13, 2, 'Computer Repair'),
    (14, 2, 'Furniture Repair'),

    -- Laundry
    (15, 3, 'Dry Cleaning'),
    (16, 3, 'Wash and Fold'),
    (17, 3, 'Ironing Service'),
    (18, 3, 'Clothing Alterations'),
    (19, 3, 'Shoe Repair'),

    -- Painting
    (20, 4, 'Interior Painting'),
    (21, 4, 'Exterior Painting'),
    (22, 4, 'Wall Murals'),
    (23, 4, 'Furniture Painting'),
    (24, 4, 'Fence Painting'),

    -- Appliance
    (25, 5, 'Air Conditioner Repair'),
    (26, 5, 'Refrigerator Repair'),
    (27, 5, 'Washing Machine Repair'),
    (28, 5, 'Water Heater Repair'),
    (29, 5, 'TV Repair'),
    (30, 5, 'Microwave Repair'),
    (31, 5, 'Chimney Repair'),
    (32, 5, 'Dishwasher Repair'),
    (33, 5, 'Others'),

    -- Plumbing
    (34, 6, 'Leak Repair'),
    (35, 6, 'Pipe Installation'),
    (36, 6, 'Drain Cleaning'),
    (37, 6, 'Toilet Repair'),
    (38, 6, 'Water Heater Installation'),
    (39, 6, 'Faucet Repair'),

    -- Shifting
    (40, 7, 'Local Moving'),
    (41, 7, 'Long-Distance Moving'),
    (42, 7, 'Office Moving'),
    (43, 7, 'Furniture Moving'),
    (44, 7, 'Packing Services'),
    (45, 7, 'Storage Services');

CREATE TABLE service_requests
(
    id                   SERIAL PRIMARY KEY,
    description          TEXT        NOT NULL,
    severity             VARCHAR(20) NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    request_address_line TEXT        NOT NULL,
    request_city         VARCHAR(50) NOT NULL,
    request_zip_code     VARCHAR(10) NOT NULL,
    request_country      VARCHAR(3)  NOT NULL,
    service_id           INT         NOT NULL REFERENCES services (id),
    seeker_id            INT         NOT NULL REFERENCES users (id),
    provider_id          INT         NOT NULL REFERENCES users (id),
    created_at           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE notification_type AS ENUM (
    'NEW_REQUEST',
    'REQUEST_ACCEPTED',
    'REQUEST_DECLINED',
    'BOOKING_CONFIRMED',
    'SERVICE_COMPLETED',
    'SYSTEM_ALERT'
);

-- Create Notifications Table
CREATE TABLE notifications
(
    id           SERIAL PRIMARY KEY,
    type         notification_type NOT NULL,
    recipient_id INT               NOT NULL REFERENCES users (id),
    payload      JSONB             NOT NULL,
    is_read      BOOLEAN           NOT NULL DEFAULT false,
    created_at   TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Indexes for Optimized Queries
CREATE INDEX idx_notifications_recipient ON notifications (recipient_id);
CREATE INDEX idx_notifications_type ON notifications (type);
CREATE INDEX idx_notifications_created_at ON notifications (created_at);
CREATE INDEX idx_notifications_is_read ON notifications (is_read);