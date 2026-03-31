DROP DATABASE IF EXISTS product_management_db;
DROP DATABASE IF EXISTS mini_dish_db;


DROP USER IF EXISTS product_manager_user;
DROP USER IF EXISTS mini_dish_db_manager;

CREATE DATABASE product_management_db;

CREATE USER product_manager_user WITH PASSWORD '123456';
GRANT ALL PRIVILEGES ON DATABASE product_management_db TO product_manager_user;


\c product_management_db;
GRANT ALL ON SCHEMA public TO product_manager_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO product_manager_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO product_manager_user;


CREATE DATABASE mini_dish_db;

CREATE USER mini_dish_db_manager WITH PASSWORD '123456';
GRANT ALL PRIVILEGES ON DATABASE mini_dish_db TO mini_dish_db_manager;


\c mini_dish_db;
GRANT ALL ON SCHEMA public TO mini_dish_db_manager;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO mini_dish_db_manager;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO mini_dish_db_manager;

CREATE TYPE dish_type_enum AS ENUM ('START', 'MAIN', 'DESSERT');
CREATE TYPE ingredient_category_enum AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');

GRANT USAGE ON TYPE dish_type_enum TO mini_dish_db_manager;
GRANT USAGE ON TYPE ingredient_category_enum TO mini_dish_db_manager;

\c postgres;