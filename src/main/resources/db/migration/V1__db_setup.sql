--To create all the tables, extra columns and indices
CREATE TABLE IF NOT EXISTS address
(
    id            bigserial primary key,
    post_code     varchar(10),
    state         varchar(255),
    street_name   varchar(255),
    street_number varchar(255),
    suburb        varchar(255),
    unit_number   varchar(10)
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


CREATE TABLE IF NOT EXISTS services
(
    id           bigserial primary key,
    service_name varchar(255),
    category_id  bigint not null,
    foreign key (category_id) references category (id)
        match simple on update no action on delete cascade
);


CREATE TABLE IF NOT EXISTS task
(
    id           bigserial primary key,
    poster_id           bigint,
    task_service_name varchar(255),
    task_description  text,
    user_address      varchar(255),
    posted_at         timestamp,
    is_active         boolean,
    customer_budget   numeric(38, 2),
    task_image        varchar(255)
);

CREATE TABLE IF NOT EXISTS task_task_dates
(
    task_id bigint not null,
    foreign key (task_id) references task (id)
    match simple on update no action on delete cascade,
    task_dates date
);




CREATE TABLE IF NOT EXISTS task_hub_verification_token
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
    id        bigserial primary key,
    id_image  varchar(255),
    id_number varchar(255) not null unique,
    id_type   varchar(255)
);


CREATE TABLE IF NOT EXISTS users
(
    id             bigserial primary key,
    account_state  varchar(255),
    deactivated_at date,
    email_address  varchar(255) not null unique,
    first_name     varchar(255) not null,
    is_enabled     boolean      not null,
    last_name      varchar(255) not null,
    password       varchar(255) not null,
    phone_number   varchar(255),
    profile_image  varchar(255),
    registered_at  timestamp(6),
    roles          varchar(255)[],
    stripe_id      varchar(255),
    address_id     bigint,
    foreign key (address_id) references address (id)
        match simple on update no action on delete set null
);


CREATE TABLE IF NOT EXISTS app_notification
(
    id                bigserial primary key,
    message           text   not null,
    notification_time timestamp(6),
    recipient_id      bigint,
    foreign key (recipient_id) references users (id)
        match simple on update no action on delete cascade
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
    service_provider_id bigint not null,
    foreign key (service_provider_id) references public.service_provider (id)
    match simple on update no action on delete cascade
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
    foreign key (booking_id) references booking (id)
        match simple on update no action on delete cascade,
    book_dates date
);


CREATE TABLE IF NOT EXISTS listing_business_pictures
(
    listing_id        bigint not null,
    foreign key (listing_id) references listing (id)
        match simple on update no action on delete cascade,
    business_pictures varchar(255)
);


CREATE TABLE IF NOT EXISTS task_hub_token
(
    id            bigserial primary key,
    access_token  varchar(255),
    refresh_token varchar(255),
    revoked       boolean not null,
    user_id       bigint not null,
    foreign key (user_id) references users (id)
    match simple on update no action on delete cascade
);


--create the tsvector columns where necessary
ALTER TABLE address
    ADD COLUMN address_with_weight tsvector;

ALTER TABLE users
    ADD COLUMN user_with_weight tsvector;

ALTER TABLE user_identity
    ADD COLUMN identity_with_weight tsvector;

ALTER TABLE listing
    ADD COLUMN listing_with_weight tsvector;

ALTER TABLE task
    ADD COLUMN task_with_weight tsvector;

ALTER TABLE app_notification
    ADD COLUMN notification_with_weight tsvector;


--create the indices where necessary
CREATE INDEX address_with_weight_idx ON address USING GIN (address_with_weight);

CREATE INDEX user_with_weight_idx ON users USING GIN (user_with_weight);

CREATE INDEX identity_with_weight_idx ON user_identity USING GIN (identity_with_weight);

CREATE INDEX listing_with_weight_idx ON listing USING GIN (listing_with_weight);

CREATE INDEX idx_listing_availability ON listing (available_days);

CREATE INDEX task_with_weight_idx ON task USING GIN (task_with_weight);

CREATE INDEX notification_with_weight_idx ON app_notification USING GIN (notification_with_weight);

--This is to install the pg_trgm extension to perform more concise search eg, mic for michael
CREATE EXTENSION IF NOT EXISTS pg_trgm;


--To create the ts_vector columns updates and their triggers
CREATE FUNCTION user_tsvector_trigger()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.user_with_weight :=
                            setweight(to_tsvector('simple', coalesce(NEW.first_name, '')), 'A') ||
                            setweight(to_tsvector('simple', coalesce(NEW.last_name, '')), 'B') ||
                            setweight(to_tsvector('simple', coalesce(NEW.phone_number, '')), 'C') ||
                            setweight(to_tsvector('simple', coalesce(NEW.email_address, '')), 'D') ||
                            setweight(to_tsvector('simple', coalesce(NEW.account_state, '')), 'D');

    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER user_tsvectorupdate
    BEFORE INSERT OR UPDATE
    ON users
    FOR EACH ROW
EXECUTE FUNCTION user_tsvector_trigger();

CREATE FUNCTION address_tsvector_trigger()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.address_with_weight :=
                        setweight(to_tsvector('simple', coalesce(NEW.suburb, '')), 'A') ||
                        setweight(to_tsvector('simple', coalesce(NEW.post_code, '')), 'B') ||
                        setweight(to_tsvector('english', coalesce(NEW.state, '')), 'C') ||
                        setweight(to_tsvector('english', coalesce(NEW.street_name, '')), 'D');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER address_tsvectorupdate
    BEFORE INSERT OR UPDATE
    ON address
    FOR EACH ROW
EXECUTE FUNCTION address_tsvector_trigger();

CREATE FUNCTION identity_tsvector_trigger()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.identity_with_weight :=
                setweight(to_tsvector('simple', coalesce(NEW.id_type, '')), 'A') ||
                setweight(to_tsvector('simple', coalesce(NEW.id_number, '')), 'B');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER identity_tsvectorupdate
    BEFORE INSERT OR UPDATE
    ON user_identity
    FOR EACH ROW
EXECUTE FUNCTION identity_tsvector_trigger();

CREATE FUNCTION listing_tsvector_trigger()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.listing_with_weight :=
                        setweight(to_tsvector('english', coalesce(NEW.service_name, '')), 'A') ||
                        setweight(to_tsvector('english', coalesce(NEW.service_category, '')), 'B') ||
                        setweight(to_tsvector('english', coalesce(NEW.service_description, '')), 'C') ||
                        setweight(to_tsvector('simple', coalesce(NEW.business_name, '')), 'D');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER listing_tsvectorupdate
    BEFORE INSERT OR UPDATE
    ON listing
    FOR EACH ROW
EXECUTE FUNCTION listing_tsvector_trigger();

CREATE FUNCTION task_tsvector_trigger()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.task_with_weight :=
                    setweight(to_tsvector('english', coalesce(NEW.task_service_name, '')), 'A') ||
                    setweight(to_tsvector('english', coalesce(NEW.task_description, '')), 'B') ||
                    setweight(to_tsvector('english', coalesce(NEW.user_address, '')), 'C');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER task_tsvectorupdate
    BEFORE INSERT OR UPDATE
    ON task
    FOR EACH ROW
EXECUTE FUNCTION task_tsvector_trigger();

CREATE FUNCTION notification_tsvector_trigger()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.notification_with_weight := to_tsvector('english', coalesce(NEW.message, ''));
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER notification_tsvectorupdate
    BEFORE INSERT OR UPDATE
    ON app_notification
    FOR EACH ROW
EXECUTE FUNCTION notification_tsvector_trigger();


--Add Admin to the database
INSERT INTO users (id, first_name, last_name, email_address, password, phone_number,
                   is_enabled, account_state, registered_at, roles)
VALUES (nextVal('users_id_seq'), 'Task', 'Hub', 'info@taskhub.com',
        '12345', '+61414332523', true, 'VERIFIED',
        '2023-09-02 12:00:00', ARRAY ['ADMIN']);

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
-- I decided not to have id in this but give the responsibility to hibernate to do it for me
INSERT INTO services(service_name, category_id)
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


