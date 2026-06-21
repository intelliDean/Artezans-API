--To create all the tables, extra columns and indices
CREATE TABLE IF NOT EXISTS address
(
    id                  bigserial primary key,
    post_code           varchar(10),
    state               varchar(255),
    street_name         varchar(255),
    street_number       varchar(255),
    suburb              varchar(255),
    unit_number         varchar(10),
    address_with_weight tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', coalesce(suburb, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(post_code, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(state, '')), 'C') ||
        setweight(to_tsvector('english', coalesce(street_name, '')), 'D')
        ) STORED
);


CREATE TABLE IF NOT EXISTS booking_agreement
(
    id               bigserial primary key,
    agreement_status varchar(255),
    agreement_time   timestamp(6),
    message          text
);


CREATE TABLE IF NOT EXISTS category
(
    id            bigserial primary key,
    category_name varchar(255)
);


CREATE TABLE IF NOT EXISTS category_name
(
    id   bigserial primary key,
    name varchar(255)
);


CREATE TABLE IF NOT EXISTS change_password_token
(
    id            bigserial primary key,
    email_address varchar(255),
    expired       boolean not null,
    generated_at  timestamp(6),
    new_password  varchar(255),
    old_password  varchar(255),
    revoked       boolean not null,
    token         varchar(255)
);


CREATE TABLE IF NOT EXISTS service
(
    id           bigserial primary key,
    service_name varchar(255),
    category_id  bigint not null,
    foreign key (category_id) references category (id)
        match simple on update no action on delete cascade
);


CREATE TABLE IF NOT EXISTS task
(
    id                bigserial primary key,
    poster_id         bigint,
    task_service_name varchar(255),
    task_description  text,
    user_address      varchar(255),
    posted_at         timestamp,
    is_active         boolean,
    customer_budget   numeric(38, 2),
    task_image        varchar(255),
    task_with_weight  tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(task_service_name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(task_description, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(user_address, '')), 'C')
        ) STORED
);

CREATE TABLE IF NOT EXISTS task_task_dates
(
    task_id    bigint not null,
    task_dates date,
    primary key (task_id, task_dates),
    foreign key (task_id) references task (id)
        match simple on update no action on delete cascade
);


CREATE TABLE IF NOT EXISTS artezan_verification_token
(
    id            bigserial primary key,
    email_address varchar(255),
    expire_at     timestamp(6),
    expired       boolean not null,
    generated_at  timestamp(6),
    revoked       boolean not null,
    token         varchar(255)
);


CREATE TABLE IF NOT EXISTS user_identity
(
    id                   bigserial primary key,
    id_image             varchar(255),
    id_number            varchar(255) not null unique,
    id_type              varchar(255),
    identity_with_weight tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', coalesce(id_type, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(id_number, '')), 'B')
        ) STORED
);


-- CREATE TABLE IF NOT EXISTS users
-- (
--     id               bigserial primary key,
--     account_state    varchar(255),
--     deactivated_at   date,
--     email_address    varchar(255) not null unique,
--     first_name       varchar(255) not null,
--     is_enabled       boolean      not null,
--     last_name        varchar(255) not null,
--     password         varchar(255) not null,
--     phone_number     varchar(255),
--     profile_image    varchar(255),
--     registered_at    timestamp(6),
--     roles            varchar(255)[],
--     stripe_id        varchar(255),
--     address_id       bigint,
--     foreign key (address_id) references address (id)
--         match simple on update no action on delete set null,
--     user_with_weight tsvector GENERATED ALWAYS AS (
--         setweight(to_tsvector('simple', coalesce(first_name, '')), 'A') ||
--         setweight(to_tsvector('simple', coalesce(last_name, '')), 'B') ||
--         setweight(to_tsvector('simple', coalesce(phone_number, '')), 'C') ||
--         setweight(to_tsvector('simple', coalesce(email_address, '')), 'D') ||
--         setweight(to_tsvector('simple', coalesce(account_state, '')), 'D')
--     ) STORED
-- );


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

-- ✅ New table for roles
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

CREATE TABLE IF NOT EXISTS customer
(
    id      bigserial primary key,
    user_id bigint not null,
    foreign key (user_id) references users (id)
        match simple on update no action on delete cascade
);


CREATE TABLE IF NOT EXISTS password_reset_token
(
    token_id        bigserial primary key,
    expiration_time timestamp(6),
    token           varchar(255),
    user_id         bigint not null,
    foreign key (user_id) references users (id)
        match simple on update no action on delete cascade
);


CREATE TABLE IF NOT EXISTS service_provider
(
    id               bigserial primary key,
    user_id          bigint not null,
    foreign key (user_id) references users (id)
        match simple on update no action on delete cascade,
    user_identity_id bigint,
    foreign key (user_identity_id) references user_identity (id)
        match simple on update no action on delete set null
);


CREATE TABLE IF NOT EXISTS listing
(
    id                  bigserial primary key,
    available           boolean      not null,
    available_days      varchar(255)[],
    available_from      time,
    available_to        time,
    business_name       varchar(255) not null,
    deleted             boolean      not null,
    pricing             numeric(38, 2),
    service_category    varchar(255),
    service_description text,
    service_name        varchar(255),
    stripe_id           varchar(255),
    address_id          bigint,
    foreign key (address_id) references address (id)
        match simple on update no action on delete set null,
    service_provider_id bigint       not null,
    foreign key (service_provider_id) references public.service_provider (id)
        match simple on update no action on delete cascade,
    listing_with_weight tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(service_name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(service_category, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(service_description, '')), 'C') ||
        setweight(to_tsvector('simple', coalesce(business_name, '')), 'D')
        ) STORED
);


CREATE TABLE IF NOT EXISTS booking
(
    id                   bigserial primary key,
    accepted             boolean,
    book_from            time,
    book_state           varchar(255),
    book_to              time,
    booked_at            timestamp(6),
    booking_stage        varchar(255),
    total_cost           numeric(38, 2),
    updated_at           timestamp(6),
    booking_agreement_id bigint,
    foreign key (booking_agreement_id) references booking_agreement (id)
        match simple on update no action on delete set null,
    listing_id           bigint not null,
    foreign key (listing_id) references listing (id)
        match simple on update no action on delete cascade,
    user_id              bigint not null,
    foreign key (user_id) references users (id)
        match simple on update no action on delete cascade
);


CREATE TABLE IF NOT EXISTS booking_book_dates
(
    booking_id bigint not null,
    book_dates date,
    primary key (booking_id, book_dates),
    foreign key (booking_id) references booking (id)
        match simple on update no action on delete cascade
);


CREATE TABLE IF NOT EXISTS listing_business_pictures
(
    listing_id        bigint not null,
    foreign key (listing_id) references listing (id)
        match simple on update no action on delete cascade,
    business_pictures varchar(255)
);


CREATE TABLE IF NOT EXISTS artezan_token (
    id            bigserial primary key,
    access_token  varchar(255),
    refresh_token varchar(255),
    revoked       boolean not null,
    user_id       bigint  not null,
    foreign key (user_id) references users (id)
        match simple on update no action on delete cascade
);


--create the indices where necessary
CREATE INDEX IF NOT EXISTS idx_notification_recipient ON app_notifications (recipient_id);

CREATE INDEX IF NOT EXISTS idx_notification_read ON app_notifications (read);

CREATE INDEX IF NOT EXISTS idx_notification_with_weight ON app_notifications USING gin (notification_with_weight);

CREATE INDEX IF NOT EXISTS idx_user_account_state ON users (account_state);

CREATE INDEX IF NOT EXISTS idx_user_with_weight ON users USING gin (user_with_weight);

CREATE INDEX address_with_weight_idx ON address USING GIN (address_with_weight);

CREATE INDEX user_with_weight_idx ON users USING GIN (user_with_weight);

CREATE INDEX identity_with_weight_idx ON user_identity USING GIN (identity_with_weight);

CREATE INDEX listing_with_weight_idx ON listing USING GIN (listing_with_weight);

CREATE INDEX idx_listing_availability ON listing (available_days);

CREATE INDEX task_with_weight_idx ON task USING GIN (task_with_weight);

CREATE INDEX notification_with_weight_idx ON app_notifications USING GIN (notification_with_weight);

--This is to install the pg_trgm extension to perform more concise search eg, mic for michael
CREATE EXTENSION IF NOT EXISTS pg_trgm;


--Add Address for Admin
INSERT INTO address (id, unit_number, street_number, street_name, suburb, state, post_code)
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
INSERT INTO category_name(id, name)
VALUES (nextVal('category_name_id_seq'), 'HOME SERVICES'),
       (nextVal('category_name_id_seq'), 'PERSONAL SERVICES'),
       (nextVal('category_name_id_seq'), 'EVENTS & ENTERTAINMENTS'),
       (nextVal('category_name_id_seq'), 'EDUCATION & TUTORING'),
       (nextVal('category_name_id_seq'), 'PROFESSIONAL SERVICES'),
       (nextVal('category_name_id_seq'), 'AUTOMOTIVE SERVICES'),
       (nextVal('category_name_id_seq'), 'HEALTH & FITNESS'),
       (nextVal('category_name_id_seq'), 'TECHNOLOGY & ELECTRONICS'),
       (nextVal('category_name_id_seq'), 'HOME IMPROVEMENT'),
       (nextVal('category_name_id_seq'), 'REAL ESTATE SERVICES'),
       (nextVal('category_name_id_seq'), 'DELIVERY & LOGISTICS'),
       (nextVal('category_name_id_seq'), 'ART & CREATIVITY'),
       (nextVal('category_name_id_seq'), 'WEDDING SERVICES'),
       (nextVal('category_name_id_seq'), 'CHILDCARE & BABYSITTING'),
       (nextVal('category_name_id_seq'), 'TRAVEL & ADVENTURE');

--Populates category table
INSERT INTO category(id, category_name)
VALUES (nextVal('category_id_seq'), 'HOME SERVICES'),
       (nextVal('category_id_seq'), 'PERSONAL SERVICES'),
       (nextVal('category_id_seq'), 'EVENTS & ENTERTAINMENTS'),
       (nextVal('category_id_seq'), 'EDUCATION & TUTORING'),
       (nextVal('category_id_seq'), 'PROFESSIONAL SERVICES'),
       (nextVal('category_id_seq'), 'AUTOMOTIVE SERVICES'),
       (nextVal('category_id_seq'), 'HEALTH & FITNESS'),
       (nextVal('category_id_seq'), 'TECHNOLOGY & ELECTRONICS'),
       (nextVal('category_id_seq'), 'HOME IMPROVEMENT'),
       (nextVal('category_id_seq'), 'REAL ESTATE SERVICES'),
       (nextVal('category_id_seq'), 'DELIVERY & LOGISTICS'),
       (nextVal('category_id_seq'), 'ART & CREATIVITY'),
       (nextVal('category_id_seq'), 'WEDDING SERVICES'),
       (nextVal('category_id_seq'), 'CHILDCARE & BABYSITTING'),
       (nextVal('category_id_seq'), 'TRAVEL & ADVENTURE');

--Populates service table
INSERT INTO service(service_name, category_id)
VALUES ('Cleaning', 1),
       ('Plumbing', 1),
       ('Electrician', 1),
       ('Carpentry', 1),
       ('Pest Control', 1),
       ('Landscaping', 1),
       ('HVAC (Heating, Ventilation, and Air Conditioning)', 1),
       ('Beauty & Wellness', 2),
       ('Personal Training', 2),
       ('Massage Therapy', 2),
       ('Yoga & Meditation', 2),
       ('Life Coaching', 2),
       ('Pet Care & Grooming', 2),
       ('Event Planning', 3),
       ('Photography & Videography', 3),
       ('DJ Services', 3),
       ('Catering', 3),
       ('Live Performers (Musicians, Magicians, etc.)', 3),
       ('Academic Tutoring', 4),
       ('Language Lessons', 4),
       ('Music Lessons', 4),
       ('Art Classes', 4),
       ('Test Preparation', 4),
       ('Legal Services', 5),
       ('Financial Planning', 5),
       ('Marketing & Design', 5),
       ('IT Support & Consulting', 5),
       ('Writing & Editing', 5),
       ('Auto Repair', 6),
       ('Car Detailing', 6),
       ('Towing Services', 6),
       ('Tire Services', 6),
       ('Fitness Training', 7),
       ('Nutrition Coaching', 7),
       ('Physical Therapy', 7),
       ('Holistic Healing', 7),
       ('Computer Repair', 8),
       ('Web Development', 8),
       ('App Development', 8),
       ('Graphic Design', 8),
       ('Interior Design/ Decor', 9),
       ('Renovation Services', 9),
       ('Home Maintenance', 9),
       ('Flooring & Tiling', 9),
       ('Property Management', 10),
       ('Home Inspection', 10),
       ('Real Estate Agent Services', 10),
       ('Courier Services', 11),
       ('Grocery Delivery', 11),
       ('Moving Services', 11),
       ('Custom Artwork', 12),
       ('Artist', 12),
       ('Music instructor', 12),
       ('Craftsmanship', 12),
       ('Creative Workshops', 12),
       ('Wedding Planning', 13),
       ('Bridal Makeup & Styling', 13),
       ('Wedding Photography', 13),
       ('Childcare Services', 14),
       ('Babysitting', 14),
       ('Nanny Services', 14),
       ('Tour Guides', 15),
       ('Adventure Excursions', 15),
       ('Travel Planning', 15);
