create table IF NOT EXISTS payment_method(
    id serial primary key,
    name varchar not null,
    price_modifier_from DECIMAL(10,2) not null,
    price_modifier_to DECIMAL(10,2) not null,
    points_modifier DECIMAL(10,2) not null,
    UNIQUE (name)

);
create table IF NOT EXISTS sale(
     id serial primary key,
     final_price DECIMAL(10,2) not null,
     points DECIMAL(10,2) not null,
     payment_method_id int not null,
     datetime timestamptz null,
     FOREIGN KEY (payment_method_id) REFERENCES payment_method(id)
);


CREATE TABLE IF NOT EXISTS users
(
    id         UUID DEFAULT (overlay(overlay(md5(random()::text || ':' || random()::text) placing '4' from 13) placing to_hex(floor(random()*(11-8+1) + 8)::int)::text from 17)::uuid),
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL DEFAULT 'password',
    created_at TIMESTAMP,
    version    INTEGER
);

CREATE TABLE IF NOT EXISTS posts
(
    id         UUID DEFAULT (overlay(overlay(md5(random()::text || ':' || random()::text) placing '4' from 13) placing to_hex(floor(random()*(11-8+1) + 8)::int)::text from 17)::uuid),
    title      VARCHAR(255),
    content    VARCHAR(255),
    status     VARCHAR(255)       DEFAULT 'DRAFT',
    author_id  UUID,
    created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at TIMESTAMP,
    version    INTEGER
);

CREATE TABLE IF NOT EXISTS comments
(
    id         UUID DEFAULT (overlay(overlay(md5(random()::text || ':' || random()::text) placing '4' from 13) placing to_hex(floor(random()*(11-8+1) + 8)::int)::text from 17)::uuid),
    content    VARCHAR(255),
    post_id    UUID,
    created_at TIMESTAMP,
    version    INTEGER
);

-- drop foreign key constraint
ALTER TABLE comments
    DROP CONSTRAINT IF EXISTS fk_comments_post_id;
ALTER TABLE posts
    DROP CONSTRAINT IF EXISTS fk_posts_author_id;
-- drop primary key constraint
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS pk_users;
ALTER TABLE posts
    DROP CONSTRAINT IF EXISTS pk_posts;
ALTER TABLE comments
    DROP CONSTRAINT IF EXISTS pk_comments;


-- add primary key constraint
ALTER TABLE users
    ADD CONSTRAINT pk_users PRIMARY KEY (id);
ALTER TABLE posts
    ADD CONSTRAINT pk_posts PRIMARY KEY (id);
ALTER TABLE comments
    ADD CONSTRAINT pk_comments PRIMARY KEY (id);
-- add foreign key constraint
ALTER TABLE posts
    ADD CONSTRAINT fk_posts_author_id FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE comments
    ADD CONSTRAINT fk_comments_post_id FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE;