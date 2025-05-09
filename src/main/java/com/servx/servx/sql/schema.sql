-- Create Addresses Table
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
    education         VARCHAR(100),
    created_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    address_id        INT UNIQUE                                         NOT NULL,
    FOREIGN KEY (address_id) REFERENCES addresses (id) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens
(
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(255) UNIQUE NOT NULL,
    user_id     BIGINT              NOT NULL,
    expiry_date TIMESTAMP           NOT NULL,
    CONSTRAINT fk_password_reset_token_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
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
VALUES (1, 1, 'Deep Cleaning'),
       (2, 1, 'Carpet Cleaning'),
       (3, 1, 'Window Cleaning'),
       (4, 1, 'Office Cleaning'),
       (5, 1, 'Industrial Cleaning'),
       (6, 1, 'Upholstery Cleaning'),
       (7, 1, 'Floor Cleaning'),
       (8, 2, 'Electronics Repair'),
       (9, 2, 'Appliance Repair'),
       (10, 2, 'Plumbing Repair'),
       (11, 2, 'HVAC Repair'),
       (12, 2, 'Car Repair'),
       (13, 2, 'Computer Repair'),
       (14, 2, 'Furniture Repair'),
       (15, 3, 'Dry Cleaning'),
       (16, 3, 'Wash and Fold'),
       (17, 3, 'Ironing Service'),
       (18, 3, 'Clothing Alterations'),
       (19, 3, 'Shoe Repair'),
       (20, 4, 'Interior Painting'),
       (21, 4, 'Exterior Painting'),
       (22, 4, 'Wall Murals'),
       (23, 4, 'Furniture Painting'),
       (24, 4, 'Fence Painting'),
       (25, 5, 'Air Conditioner Repair'),
       (26, 5, 'Refrigerator Repair'),
       (27, 5, 'Washing Machine Repair'),
       (28, 5, 'Water Heater Repair'),
       (29, 5, 'TV Repair'),
       (30, 5, 'Microwave Repair'),
       (31, 5, 'Chimney Repair'),
       (32, 5, 'Dishwasher Repair'),
       (33, 5, 'Others'),
       (34, 6, 'Leak Repair'),
       (35, 6, 'Pipe Installation'),
       (36, 6, 'Drain Cleaning'),
       (37, 6, 'Toilet Repair'),
       (38, 6, 'Water Heater Installation'),
       (39, 6, 'Faucet Repair'),
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

CREATE TABLE chat_messages
(
    id                   SERIAL PRIMARY KEY,
    service_request_id   INT       NOT NULL REFERENCES service_requests (id) ON DELETE CASCADE,
    sender_id            INT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    recipient_id         INT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    content              TEXT      NOT NULL,
    timestamp            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read              BOOLEAN   NOT NULL DEFAULT FALSE,
    booking_payload_json TEXT NULL
);

-- Add indexes for efficient querying
CREATE INDEX idx_chat_messages_service_request_id ON chat_messages (service_request_id);
CREATE INDEX idx_chat_messages_timestamp ON chat_messages (timestamp);

CREATE TABLE bookings
(
    id                       BIGSERIAL PRIMARY KEY,
    booking_number           VARCHAR(255)             NOT NULL UNIQUE,
    scheduled_start_time     TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes         INTEGER                  NOT NULL,
    price_min                NUMERIC(10, 2)           NOT NULL,
    price_max                NUMERIC(10, 2)           NOT NULL,
    notes                    TEXT,
    location_address_line    VARCHAR(255)             NOT NULL,
    location_city            VARCHAR(100)             NOT NULL,
    location_zip_code        VARCHAR(20)              NOT NULL,
    location_country         VARCHAR(100)             NOT NULL,
    status                   VARCHAR(50)              NOT NULL,
    provider_marked_complete BOOLEAN DEFAULT FALSE    NOT NULL,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL,
    service_id               INT                      NOT NULL REFERENCES services (id) ON DELETE CASCADE,
    provider_id              INT                      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    seeker_id                INT                      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    service_request_id       INT                      NOT NULL UNIQUE REFERENCES service_requests (id) ON DELETE CASCADE,
    CONSTRAINT check_booking_status CHECK (status IN
                                           ('UPCOMING', 'COMPLETED', 'CANCELLED_BY_SEEKER', 'CANCELLED_BY_PROVIDER'))
);

CREATE INDEX idx_bookings_provider_status ON bookings (provider_id, status);
CREATE INDEX idx_bookings_seeker_status ON bookings (seeker_id, status);
CREATE INDEX idx_bookings_start_time ON bookings (scheduled_start_time);

-- Index on city column in addresses table
CREATE INDEX idx_addresses_city ON addresses (city);

-- Index on zip_code column in addresses table
CREATE INDEX idx_addresses_zip_code ON addresses (zip_code);

-- Index on the foreign key column user_id in services table
CREATE INDEX idx_services_user_id ON services (user_id);

-- Index on the foreign key column service_category_id in services table
CREATE INDEX idx_services_category_id ON services (service_category_id);

-- Index on the foreign key column service_area_id in services table
CREATE INDEX idx_services_area_id ON services (service_area_id);

CREATE
EXTENSION IF NOT EXISTS pg_trgm;

-- Index for searching service area names (e.g., "Leak Repair")
CREATE INDEX idx_gin_service_areas_name ON service_areas USING gin (name gin_trgm_ops);

-- Index for searching service category names (e.g., "Plumbing") - UNIQUE index already exists, but GIN is better for LIKE
-- You might add this one too if you search categories often with LIKE
CREATE INDEX idx_gin_service_categories_name ON service_categories USING gin (name gin_trgm_ops);

-- Index for searching provider first names
CREATE INDEX idx_gin_users_first_name ON users USING gin (first_name gin_trgm_ops);

-- Index for searching provider last names
CREATE INDEX idx_gin_users_last_name ON users USING gin (last_name gin_trgm_ops);

CREATE INDEX idx_gin_users_fullname ON users USING gin ((first_name || ' ' || last_name) gin_trgm_ops);

------ DROPPING COMMAND ---------

DROP EXTENSION IF EXISTS pg_trgm CASCADE;

DROP TABLE IF EXISTS chat_messages CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS service_requests CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS languages CASCADE;
DROP TABLE IF EXISTS verification_tokens CASCADE;
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS services CASCADE;
DROP TABLE IF EXISTS service_areas CASCADE;
DROP TABLE IF EXISTS service_categories CASCADE;
DROP TABLE IF EXISTS addresses CASCADE;

DROP INDEX IF EXISTS idx_notifications_recipient;
DROP INDEX IF EXISTS idx_notifications_type;
DROP INDEX IF EXISTS idx_notifications_created_at;
DROP INDEX IF EXISTS idx_notifications_is_read;
DROP INDEX IF EXISTS idx_chat_messages_service_request_id;
DROP INDEX IF EXISTS idx_chat_messages_timestamp;
DROP INDEX IF EXISTS idx_bookings_provider_status;
DROP INDEX IF EXISTS idx_bookings_seeker_status;
DROP INDEX IF EXISTS idx_bookings_start_time;
DROP INDEX IF EXISTS idx_addresses_city;
DROP INDEX IF EXISTS idx_addresses_zip_code;
DROP INDEX IF EXISTS idx_services_user_id;
DROP INDEX IF EXISTS idx_services_category_id;
DROP INDEX IF EXISTS idx_services_area_id;
DROP INDEX IF EXISTS idx_gin_service_areas_name;
DROP INDEX IF EXISTS idx_gin_service_categories_name;
DROP INDEX IF EXISTS idx_gin_users_first_name;
DROP INDEX IF EXISTS idx_gin_users_last_name;
DROP INDEX IF EXISTS idx_gin_users_fullname;

---------- CREATE ------------
psql -U servx -d servx -a -f /Users/jafarov/PROJECTS/ServxBackend/src/main/java/com/servx/servx/config/schema.sql

     1. Design Chapter (Conceptual - No Specific Tech Names)

System Architecture: Good. Focus on the Client-Server model, the layers within the backend (Presentation/Service/Data Access), and the communication interfaces conceptually (REST-like API, Real-time channel). Include the high-level diagram here.
Design Patterns: (Add this section) This is crucial for the Design chapter. Include the conceptual descriptions of MVVM, Observer, Singleton, Delegate, Actor (Client) and Layered Arch, DI, Repository, DTO, MVC-like (Backend), OOP (General) that we discussed.
Data Flow: Good. Keep the description conceptual, outlining the general path of data from user interaction to database and back.
Database Design: Good. Focus on why a relational model was chosen, the main entities and their core relationships (conceptual ERD is good here), and the principle of using indexing for performance.
UI and UX Design: Good. Discuss the user-centric approach, core navigation principles (tab bar), key screen design goals, and maybe the rationale behind the visual style.
APIs and Integrations: Good. Describe the concept of the API (stateless request/response, resource-oriented) and the real-time channel (for chat) without detailing specific endpoints or protocols yet.
Scalability and Load Balancing: (Consider revising/removing) Unless you made specific design choices for scalability (e.g., stateless backend design to allow horizontal scaling), this might be too speculative for the current design. You could briefly mention that the client-server architecture allows for future scaling by separating concerns, or move this discussion entirely to Future Work in the Implementation chapter.
(Optional) Security Design: Briefly discuss the conceptual approach – e.g., token-based authentication for APIs, secure communication channels, input validation principles.
2. Implementation Chapter (Specific Tools, Tech, Challenges)

Development Environment: (Add this) Briefly list the OS, IDEs used (Xcode, IntelliJ IDEA).
Technology Stack: (Add this) Create a table or list summarizing all key technologies, frameworks, languages, and significant libraries with their versions (Java 23, Spring Boot 3.x, Maven, PostgreSQL 14.15, pg_trgm, Swift, SwiftUI, SwiftStomp, etc.). Crucially, include links to their official documentation here.
Backend Implementation: (Combine related items) Discuss the Spring Boot/Java/Maven setup, key modules (Web, Data JPA, Security, WebSocket), project structure (controllers, services, repositories, DTOs, entities), JWT implementation details, email service setup, WebSocket/STOMP configuration.
Database Implementation: Discuss the specific PostgreSQL setup, schema details (reference the detailed ERD here), and the implementation of specific indexes (like pg_trgm).
Frontend Implementation: (Combine related items) Discuss the SwiftUI/Swift setup, key frameworks used (Combine, MapKit), project structure (Views, ViewModels, Services, Models, NavigationManager), SwiftStomp integration, state management approach.
Version Control: (Rename) Call it Version Control (Git & GitHub) and briefly mention its use for tracking changes and collaboration.
Key Implementation Challenges: (Add this) Discuss specific technical hurdles encountered and how they were solved (e.g., WebSocket session management across logins, debugging API mismatches, implementing the specific recommendation logic, handling asynchronous operations). Include relevant (short) code snippets here to illustrate points.
Use of LLM Tools: (Add this) Include the paragraph we drafted about using LLMs and other online resources transparently.
Limitations and Future Work: (Keep this last) Detail the unimplemented features (Social Login, Bookmarks, E-Receipts, Dark Mode, Push Notifications, Change Email/Password backend), known bugs, potential performance bottlenecks not addressed, security considerations for further review, and ideas for future features or architectural improvements (like containerization).
3. Testing Chapter (Variety and Evidence)

Testing Strategy Overview: Briefly state the different types of testing performed.
API Endpoint Testing (Postman): Essential. Show screenshots of requests/responses for key endpoints (happy path and error cases). Explain the test case for each.
Database Verification: Show evidence (psql output or GUI screenshots) of data being correctly created/updated/deleted after specific API calls (e.g., registration, booking creation, profile update). Maybe include an EXPLAIN ANALYZE for a key query.
WebSocket/Chat Testing: Describe manual testing steps (sending/receiving messages, booking proposals), include relevant client and backend logs as evidence.
Manual Test Plan & Execution: Include the detailed checklist/table covering key user flows (Registration, Login, Search, Request Service, Booking Flow, Profile Edit, etc.). Include screenshots from the app as evidence for key steps.
(Optional) Backend Unit/Integration Tests: If you wrote any JUnit tests, include snippets and output.
(Optional) Usability Testing: Mention any informal feedback received from test users.