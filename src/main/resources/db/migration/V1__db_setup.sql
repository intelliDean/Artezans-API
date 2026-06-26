--To create all the tables, extra columns and indices
CREATE TABLE IF NOT EXISTS address
(
    id                  bigserial primary key,
    post_code           varchar(10),
    state               varchar(255),
    street_name         varchar(255),
    street_number       varchar(255),
    city                varchar(255),
    unit_number         varchar(10),
    address_with_weight tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', coalesce(city, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(post_code, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(state, '')), 'C') ||
        setweight(to_tsvector('english', coalesce(street_name, '')), 'D')
        ) STORED
);


CREATE TABLE IF NOT EXISTS categories
(
    id            bigserial primary key,
    category_name varchar(255) not null unique,
    created_at    timestamp    not null,
    updated_at    timestamp
);

CREATE TABLE IF NOT EXISTS category_names
(
    id         bigserial primary key,
    name       varchar(255) not null unique,
    created_at timestamp    not null,
    updated_at timestamp
);



CREATE TABLE IF NOT EXISTS change_password_token
(
    id                bigserial primary key,
    email_address     varchar(255) not null,
    token             varchar(512) not null unique,
    new_password_hash varchar(512) not null,
    expired           boolean      not null default false,
    revoked           boolean      not null default false,
    generated_at      timestamp    not null,
    expire_at         timestamp    not null
);



CREATE TABLE IF NOT EXISTS artezan_services
(
    id           bigserial primary key,
    service_name varchar(255) not null,
    category_id  bigint       not null,
    created_at   timestamp    not null,
    updated_at   timestamp,
    foreign key (category_id) references categories (id)
        match simple on update no action on delete cascade,
    constraint uq_service_category unique (service_name, category_id)
);

CREATE TABLE IF NOT EXISTS artezan_verification_token
(
    id            bigserial primary key,
    token         varchar(512) not null unique,
    email_address varchar(255) not null,
    revoked       boolean      not null default false,
    expired       boolean      not null default false,
    generated_at  timestamp    not null,
    expire_at     timestamp    not null
);

CREATE TABLE IF NOT EXISTS user_identity
(
    id                   bigserial primary key,
    id_number            varchar(255) not null unique,
    id_image_url         varchar(500) not null,
    id_type              varchar(50)  not null,
    verification_status  varchar(50)  not null default 'PENDING',
    submitted_at         timestamp    not null,
    updated_at           timestamp,
    identity_with_weight tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', coalesce(id_type, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(id_number, '')), 'B')
        ) STORED
);



CREATE TABLE IF NOT EXISTS users
(
    id               bigserial primary key,
    account_state    varchar(255),
    deactivated_at   date,
    email_address    varchar(255) not null unique,
    first_name       varchar(255) not null,
    enabled          boolean      not null default false,
    last_name        varchar(255) not null,
    password         varchar(255) not null,
    phone_number     varchar(255) unique,
    profile_image    varchar(255),
    registered_at    timestamp(6) not null,
    stripe_id        varchar(255),
    address_id       bigint,
    foreign key (address_id) references address (id)
        match simple on update no action on delete set null,
    user_with_weight tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', coalesce(first_name, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(last_name, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(phone_number, '')), 'C') ||
        setweight(to_tsvector('simple', coalesce(email_address, '')), 'D') ||
        setweight(to_tsvector('simple', coalesce(account_state, '')), 'D')
        ) STORED
);


CREATE TABLE IF NOT EXISTS user_roles
(
    user_id bigint       not null,
    role    varchar(255) not null,
    primary key (user_id, role),
    foreign key (user_id) references users (id) on delete cascade
);

CREATE TABLE IF NOT EXISTS app_notifications
(
    id                       bigserial primary key,
    message                  text         not null,
    type                     varchar(255) not null,
    read                     boolean      not null default false,
    notification_time        timestamp(6) not null,
    recipient_id             bigint       not null,
    foreign key (recipient_id) references users (id)
        match simple on update no action on delete cascade,
    notification_with_weight tsvector GENERATED ALWAYS AS (
        to_tsvector('english', coalesce(message, ''))
        ) STORED
);

CREATE TABLE IF NOT EXISTS task
(
    id                bigserial primary key,
    poster_id         bigint         not null,
    task_service_name varchar(255)   not null,
    task_description  text,
    user_address      varchar(255)   not null,
    posted_at         timestamp      not null,
    updated_at        timestamp,
    is_active         boolean        not null default true,
    customer_budget   numeric(38, 2) not null check (customer_budget >= 0),
    task_image_url    varchar(500),
    foreign key (poster_id) references users (id)
        match simple on update no action on delete cascade,
    task_with_weight  tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(task_service_name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(task_description, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(user_address, '')), 'C')
        ) STORED
);

-- ElementCollection table
CREATE TABLE IF NOT EXISTS task_dates
(
    task_id   bigint not null,
    task_date date   not null,
    foreign key (task_id) references task (id)
        match simple on update no action on delete cascade
);

CREATE TABLE IF NOT EXISTS customers
(
    id      bigserial primary key,
    user_id bigint not null unique,
    foreign key (user_id) references users (id)
        match simple on update no action on delete restrict
);

CREATE TABLE IF NOT EXISTS forgot_password_token
(
    id            bigserial primary key,
    token         varchar(512) not null unique,
    email_address varchar(255) not null,
    revoked       boolean      not null default false,
    expired       boolean      not null default false,
    generated_at  timestamp    not null,
    expire_at     timestamp    not null,
    user_id       bigint       not null unique,
    foreign key (user_id) references users (id)
        match simple on update no action on delete cascade
);


CREATE TABLE IF NOT EXISTS service_providers
(
    id               bigserial primary key,
    user_id          bigint not null unique,
    user_identity_id bigint,
    foreign key (user_id) references users (id)
        match simple on update no action on delete restrict,
    foreign key (user_identity_id) references user_identity (id)
        match simple on update no action on delete set null
);


CREATE TABLE IF NOT EXISTS listings
(
    id                  bigserial primary key,
    available           boolean        not null,
    available_from      time,
    available_to        time,
    business_name       varchar(255)   not null unique,
    deleted             boolean        not null default false,
    pricing             numeric(38, 2) not null check (pricing >= 5.00),
    service_category    varchar(255)   not null,
    service_description text,
    service_name        varchar(255)   not null,
    stripe_id           varchar(255),
    address_id          bigint,
    service_provider_id bigint         not null,
    created_at          timestamp      not null,
    updated_at          timestamp,
    foreign key (address_id) references address (id)
        match simple on update no action on delete set null,
    foreign key (service_provider_id) references service_providers (id)
        match simple on update no action on delete restrict,
    listing_with_weight tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(service_name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(service_category, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(service_description, '')), 'C') ||
        setweight(to_tsvector('simple', coalesce(business_name, '')), 'D')
        ) STORED
);


CREATE TABLE IF NOT EXISTS listing_available_days
(
    listing_id    bigint      not null,
    available_day varchar(50) not null,
    foreign key (listing_id) references listings (id)
        match simple on update no action on delete cascade
);

CREATE TABLE IF NOT EXISTS listing_pictures
(
    listing_id  bigint       not null,
    picture_url varchar(500) not null,
    foreign key (listing_id) references listings (id)
        match simple on update no action on delete cascade
);

CREATE TABLE IF NOT EXISTS booking_agreement
(
    id               bigserial primary key,
    agreement_status varchar(50) not null,
    message          text,
    agreement_time   timestamp   not null,
    created_at       timestamp   not null,
    updated_at       timestamp
);

CREATE TABLE IF NOT EXISTS bookings
(
    id                   bigserial primary key,
    accepted             boolean        not null default false,
    book_from            time           not null,
    book_to              time           not null,
    book_state           varchar(50)    not null,
    booking_stage        varchar(50)    not null,
    total_cost           numeric(38, 2) not null,
    booked_at            timestamp      not null,
    updated_at           timestamp,
    booking_agreement_id bigint,
    listing_id           bigint         not null,
    user_id              bigint         not null,
    foreign key (booking_agreement_id) references booking_agreement (id)
        match simple on update no action on delete set null,
    foreign key (listing_id) references listings (id)
        match simple on update no action on delete cascade,
    foreign key (user_id) references users (id)
        match simple on update no action on delete cascade
);


CREATE TABLE IF NOT EXISTS booking_dates
(
    booking_id bigint not null,
    book_date  date   not null,
    foreign key (booking_id) references bookings (id)
        match simple on update no action on delete cascade
);

CREATE TABLE IF NOT EXISTS artezan_token
(
    id            bigserial primary key,
    access_token  varchar(512) not null,
    refresh_token varchar(512),
    revoked       boolean      not null default false,
    revoked_at    timestamp,
--     expires_at    timestamp    not null,
    user_id       bigint       not null,
    created_at    timestamp    not null,
    foreign key (user_id) references users (id)
        match simple on update no action on delete cascade
);


--create the indices where necessary
CREATE INDEX idx_task_service_name ON task (task_service_name);

CREATE INDEX idx_task_poster ON task (poster_id);

CREATE INDEX idx_task_active ON task (is_active);

CREATE INDEX idx_task_weight ON task USING GIN (task_with_weight);

CREATE INDEX idx_task_dates ON task_dates (task_id);

CREATE INDEX idx_booking_agreement_status ON booking_agreement (agreement_status);

CREATE INDEX idx_booking_user ON bookings (user_id);

CREATE INDEX idx_booking_listing ON bookings (listing_id);

CREATE INDEX idx_booking_state ON bookings (book_state);

CREATE INDEX idx_booking_stage ON bookings (booking_stage);

CREATE INDEX idx_booking_dates ON booking_dates (booking_id);

CREATE INDEX idx_category_name_name ON category_names (name);

CREATE INDEX idx_artezan_service_name ON artezan_services (service_name);

CREATE INDEX idx_artezan_service_category ON artezan_services (category_id);

CREATE INDEX idx_category_name ON categories (category_name);

CREATE INDEX idx_forgot_password_token ON forgot_password_token (token);

CREATE INDEX idx_forgot_password_user ON forgot_password_token (user_id);

CREATE INDEX idx_change_password_token ON change_password_token (token);

CREATE INDEX idx_change_password_email ON change_password_token (email_address);

CREATE INDEX idx_verification_token ON artezan_verification_token (token);

CREATE INDEX idx_verification_email ON artezan_verification_token (email_address);

CREATE INDEX idx_artezan_token_access ON artezan_token (access_token);

CREATE INDEX idx_artezan_token_refresh ON artezan_token (refresh_token);

CREATE INDEX idx_artezan_token_user ON artezan_token (user_id);

CREATE INDEX idx_listing_service_name ON listings (service_name);

CREATE INDEX idx_listing_service_category ON listings (service_category);

CREATE INDEX idx_listing_available ON listings (available);

CREATE INDEX idx_listing_weight ON listings USING GIN (listing_with_weight);

CREATE INDEX idx_listing_available_days ON listing_available_days (listing_id);

CREATE INDEX idx_listing_pictures ON listing_pictures (listing_id);

CREATE INDEX idx_user_identity_weight ON user_identity USING GIN (identity_with_weight);

CREATE INDEX idx_user_identity_id_number ON user_identity (id_number);

CREATE INDEX IF NOT EXISTS idx_notification_recipient ON app_notifications (recipient_id);

CREATE INDEX IF NOT EXISTS idx_notification_read ON app_notifications (read);

CREATE INDEX IF NOT EXISTS idx_notification_with_weight ON app_notifications USING gin (notification_with_weight);

CREATE INDEX IF NOT EXISTS idx_user_account_state ON users (account_state);

CREATE INDEX IF NOT EXISTS idx_user_with_weight ON users USING gin (user_with_weight);

CREATE INDEX address_with_weight_idx ON address USING GIN (address_with_weight);

CREATE INDEX user_with_weight_idx ON users USING GIN (user_with_weight);

CREATE INDEX notification_with_weight_idx ON app_notifications USING GIN (notification_with_weight);

--This is to install the pg_trgm extension to perform more concise search eg, mic for michael
CREATE EXTENSION IF NOT EXISTS pg_trgm;


--Add Address for Admin
INSERT INTO address (id, unit_number, street_number, street_name, city, state, post_code)
VALUES (nextVal('address_id_seq'), '5', '12', 'Lekki Phase 1', 'Lekki', 'Lagos', '219003');

--Add Admin to the database
INSERT INTO users (id, first_name, last_name, email_address, password, phone_number,
                   enabled, account_state, registered_at, address_id)
VALUES (nextval('users_id_seq'), 'One', 'Block', 'oneblockhq@gmail.com',
        '12345',
        '+2348095729090', true, 'VERIFIED',
        '2026-09-02 12:00:00', currval('address_id_seq'));

-- 3. Assign role
INSERT INTO user_roles (user_id, role)
VALUES (currval('users_id_seq'), 'ADMIN');

--Populates category name table
INSERT INTO category_names(id, name, created_at)
VALUES (nextVal('category_names_id_seq'), 'HOME SERVICES', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'PERSONAL SERVICES', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'EVENTS & ENTERTAINMENTS', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'EDUCATION & TUTORING', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'PROFESSIONAL SERVICES', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'AUTOMOTIVE SERVICES', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'HEALTH & FITNESS', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'TECHNOLOGY & ELECTRONICS', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'HOME IMPROVEMENT', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'REAL ESTATE SERVICES', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'DELIVERY & LOGISTICS', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'ART & CREATIVITY', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'WEDDING SERVICES', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'CHILDCARE & BABYSITTING', CURRENT_TIMESTAMP),
       (nextVal('category_names_id_seq'), 'TRAVEL & ADVENTURE', CURRENT_TIMESTAMP);

--Populates category table
INSERT INTO categories(id, category_name, created_at)
VALUES (nextVal('categories_id_seq'), 'HOME SERVICES', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'PERSONAL SERVICES', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'EVENTS & ENTERTAINMENTS', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'EDUCATION & TUTORING', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'PROFESSIONAL SERVICES', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'AUTOMOTIVE SERVICES', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'HEALTH & FITNESS', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'TECHNOLOGY & ELECTRONICS', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'HOME IMPROVEMENT', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'REAL ESTATE SERVICES', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'DELIVERY & LOGISTICS', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'ART & CREATIVITY', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'WEDDING SERVICES', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'CHILDCARE & BABYSITTING', CURRENT_TIMESTAMP),
       (nextVal('categories_id_seq'), 'TRAVEL & ADVENTURE', CURRENT_TIMESTAMP);

--Populates artezanService table
INSERT INTO artezan_services(service_name, category_id, created_at)
VALUES ('Cleaning', 1, CURRENT_TIMESTAMP),
       ('Plumbing', 1, CURRENT_TIMESTAMP),
       ('Electrician', 1, CURRENT_TIMESTAMP),
       ('Carpentry', 1, CURRENT_TIMESTAMP),
       ('Pest Control', 1, CURRENT_TIMESTAMP),
       ('Landscaping', 1, CURRENT_TIMESTAMP),
       ('HVAC (Heating, Ventilation, and Air Conditioning)', 1, CURRENT_TIMESTAMP),
       ('Beauty & Wellness', 2, CURRENT_TIMESTAMP),
       ('Personal Training', 2, CURRENT_TIMESTAMP),
       ('Massage Therapy', 2, CURRENT_TIMESTAMP),
       ('Yoga & Meditation', 2, CURRENT_TIMESTAMP),
       ('Life Coaching', 2, CURRENT_TIMESTAMP),
       ('Pet Care & Grooming', 2, CURRENT_TIMESTAMP),
       ('Event Planning', 3, CURRENT_TIMESTAMP),
       ('Photography & Videography', 3, CURRENT_TIMESTAMP),
       ('DJ Services', 3, CURRENT_TIMESTAMP),
       ('Catering', 3, CURRENT_TIMESTAMP),
       ('Live Performers (Musicians, Magicians, etc.)', 3, CURRENT_TIMESTAMP),
       ('Academic Tutoring', 4, CURRENT_TIMESTAMP),
       ('Language Lessons', 4, CURRENT_TIMESTAMP),
       ('Music Lessons', 4, CURRENT_TIMESTAMP),
       ('Art Classes', 4, CURRENT_TIMESTAMP),
       ('Test Preparation', 4, CURRENT_TIMESTAMP),
       ('Legal Services', 5, CURRENT_TIMESTAMP),
       ('Financial Planning', 5, CURRENT_TIMESTAMP),
       ('Marketing & Design', 5, CURRENT_TIMESTAMP),
       ('IT Support & Consulting', 5, CURRENT_TIMESTAMP),
       ('Writing & Editing', 5, CURRENT_TIMESTAMP),
       ('Auto Repair', 6, CURRENT_TIMESTAMP),
       ('Car Detailing', 6, CURRENT_TIMESTAMP),
       ('Towing Services', 6, CURRENT_TIMESTAMP),
       ('Tire Services', 6, CURRENT_TIMESTAMP),
       ('Fitness Training', 7, CURRENT_TIMESTAMP),
       ('Nutrition Coaching', 7, CURRENT_TIMESTAMP),
       ('Physical Therapy', 7, CURRENT_TIMESTAMP),
       ('Holistic Healing', 7, CURRENT_TIMESTAMP),
       ('Computer Repair', 8, CURRENT_TIMESTAMP),
       ('Web Development', 8, CURRENT_TIMESTAMP),
       ('App Development', 8, CURRENT_TIMESTAMP),
       ('Graphic Design', 8, CURRENT_TIMESTAMP),
       ('Interior Design/ Decor', 9, CURRENT_TIMESTAMP),
       ('Renovation Services', 9, CURRENT_TIMESTAMP),
       ('Home Maintenance', 9, CURRENT_TIMESTAMP),
       ('Flooring & Tiling', 9, CURRENT_TIMESTAMP),
       ('Property Management', 10, CURRENT_TIMESTAMP),
       ('Home Inspection', 10, CURRENT_TIMESTAMP),
       ('Real Estate Agent Services', 10, CURRENT_TIMESTAMP),
       ('Courier Services', 11, CURRENT_TIMESTAMP),
       ('Grocery Delivery', 11, CURRENT_TIMESTAMP),
       ('Moving Services', 11, CURRENT_TIMESTAMP),
       ('Custom Artwork', 12, CURRENT_TIMESTAMP),
       ('Artist', 12, CURRENT_TIMESTAMP),
       ('Music instructor', 12, CURRENT_TIMESTAMP),
       ('Craftsmanship', 12, CURRENT_TIMESTAMP),
       ('Creative Workshops', 12, CURRENT_TIMESTAMP),
       ('Wedding Planning', 13, CURRENT_TIMESTAMP),
       ('Bridal Makeup & Styling', 13, CURRENT_TIMESTAMP),
       ('Wedding Photography', 13, CURRENT_TIMESTAMP),
       ('Childcare Services', 14, CURRENT_TIMESTAMP),
       ('Babysitting', 14, CURRENT_TIMESTAMP),
       ('Nanny Services', 14, CURRENT_TIMESTAMP),
       ('Tour Guides', 15, CURRENT_TIMESTAMP),
       ('Adventure Excursions', 15, CURRENT_TIMESTAMP),
       ('Travel Planning', 15, CURRENT_TIMESTAMP);
