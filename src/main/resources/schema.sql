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