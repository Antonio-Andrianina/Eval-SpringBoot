\c product_management_db;

CREATE TABLE IF NOT EXISTS Product (
                                       id SERIAL PRIMARY KEY,
                                       name VARCHAR(255) NOT NULL,
                                       price NUMERIC(10, 2) NOT NULL,
                                       creation_datetime TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS Product_category (
                                                id SERIAL PRIMARY KEY,
                                                name VARCHAR(255) NOT NULL,
                                                product_id INTEGER NOT NULL,
                                                FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_product_name ON Product(name);
CREATE INDEX IF NOT EXISTS idx_product_creation_datetime ON Product(creation_datetime);
CREATE INDEX IF NOT EXISTS idx_product_category_product_id ON Product_category(product_id);
CREATE INDEX IF NOT EXISTS idx_product_category_name ON Product_category(name);

\c mini_dish_db;

CREATE TABLE IF NOT EXISTS Dish (
                                    id SERIAL PRIMARY KEY,
                                    name VARCHAR(255) NOT NULL,
                                    dish_type dish_type_enum NOT NULL
);

CREATE TABLE IF NOT EXISTS Ingredient (
                                          id SERIAL PRIMARY KEY,
                                          name VARCHAR(255) NOT NULL,
                                          price NUMERIC(10, 2) NOT NULL,
                                          category ingredient_category_enum NOT NULL,
                                          id_dish INTEGER,
                                          FOREIGN KEY (id_dish) REFERENCES Dish(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_dish_name ON Dish(name);
CREATE INDEX IF NOT EXISTS idx_ingredient_name ON Ingredient(name);
CREATE INDEX IF NOT EXISTS idx_ingredient_id_dish ON Ingredient(id_dish);
CREATE INDEX IF NOT EXISTS idx_ingredient_category ON Ingredient(category);