\c mini_dish_db;


ALTER TABLE Dish ADD COLUMN IF NOT EXISTS selling_price NUMERIC(10, 2);

CREATE TABLE IF NOT EXISTS Ingredient_Base (
                                               id SERIAL PRIMARY KEY,
                                               name VARCHAR(255) NOT NULL UNIQUE,
                                               price NUMERIC(10, 2) NOT NULL,
                                               category ingredient_category_enum NOT NULL
);


CREATE INDEX IF NOT EXISTS idx_ingredient_base_name ON Ingredient_Base(name);
CREATE INDEX IF NOT EXISTS idx_ingredient_base_category ON Ingredient_Base(category);


CREATE TABLE IF NOT EXISTS DishIngredient (
                                              id SERIAL PRIMARY KEY,
                                              id_dish INTEGER NOT NULL,
                                              id_ingredient INTEGER NOT NULL,
                                              quantity_required NUMERIC(10, 3) NOT NULL DEFAULT 1.0,
                                              unit VARCHAR(10) NOT NULL DEFAULT 'KG',
                                              FOREIGN KEY (id_dish) REFERENCES Dish(id) ON DELETE CASCADE,
                                              FOREIGN KEY (id_ingredient) REFERENCES Ingredient_Base(id) ON DELETE CASCADE,
                                              UNIQUE(id_dish, id_ingredient)
);


CREATE INDEX IF NOT EXISTS idx_dish_ingredient_id_dish ON DishIngredient(id_dish);
CREATE INDEX IF NOT EXISTS idx_dish_ingredient_id_ingredient ON DishIngredient(id_ingredient);


INSERT INTO Ingredient_Base (name, price, category)
SELECT DISTINCT name, price, category FROM Ingredient
ON CONFLICT (name) DO NOTHING;

INSERT INTO DishIngredient (id_dish, id_ingredient, quantity_required, unit)
SELECT DISTINCT
    i.id_dish,
    ib.id,
    1.0,
    'KG'
FROM Ingredient i
         JOIN Ingredient_Base ib ON i.name = ib.name
WHERE i.id_dish IS NOT NULL
ON CONFLICT (id_dish, id_ingredient) DO NOTHING;


CREATE TABLE IF NOT EXISTS StockMovement (
                                             id SERIAL PRIMARY KEY,
                                             id_ingredient INTEGER NOT NULL,
                                             quantity NUMERIC(10, 3) NOT NULL,
                                             type VARCHAR(10) NOT NULL CHECK (type IN ('IN', 'OUT')),
                                             unit VARCHAR(10) NOT NULL,
                                             creation_datetime TIMESTAMP NOT NULL,
                                             FOREIGN KEY (id_ingredient) REFERENCES Ingredient_Base(id) ON DELETE CASCADE
);


CREATE INDEX IF NOT EXISTS idx_stock_movement_id_ingredient ON StockMovement(id_ingredient);
CREATE INDEX IF NOT EXISTS idx_stock_movement_creation_datetime ON StockMovement(creation_datetime);
CREATE INDEX IF NOT EXISTS idx_stock_movement_type ON StockMovement(type);


UPDATE Dish SET selling_price = 3500.00 WHERE id = 1;
UPDATE Dish SET selling_price = 12000.00 WHERE id = 2;
UPDATE Dish SET selling_price = NULL WHERE id = 3;
UPDATE Dish SET selling_price = 8000.00 WHERE id = 4;
UPDATE Dish SET selling_price = NULL WHERE id = 5;

UPDATE DishIngredient
SET quantity_required = 0.20, unit = 'KG'
WHERE id_dish = 1 AND id_ingredient = (SELECT id FROM Ingredient_Base WHERE name = 'Laitue');

UPDATE DishIngredient
SET quantity_required = 0.15, unit = 'KG'
WHERE id_dish = 1 AND id_ingredient = (SELECT id FROM Ingredient_Base WHERE name = 'Tomate');


UPDATE DishIngredient
SET quantity_required = 1.00, unit = 'KG'
WHERE id_dish = 2 AND id_ingredient = (SELECT id FROM Ingredient_Base WHERE name = 'Poulet');


UPDATE DishIngredient
SET quantity_required = 0.30, unit = 'KG'
WHERE id_dish = 4 AND id_ingredient = (SELECT id FROM Ingredient_Base WHERE name = 'Chocolat');

UPDATE DishIngredient
SET quantity_required = 0.20, unit = 'KG'
WHERE id_dish = 4 AND id_ingredient = (SELECT id FROM Ingredient_Base WHERE name = 'Beurre');



INSERT INTO StockMovement (id_ingredient, quantity, type, unit, creation_datetime)
SELECT id, 0.2, 'OUT', 'KG', '2024-01-06 12:00:00'
FROM Ingredient_Base WHERE name = 'Laitue';


INSERT INTO StockMovement (id_ingredient, quantity, type, unit, creation_datetime)
SELECT id, 0.15, 'OUT', 'KG', '2024-01-06 12:00:00'
FROM Ingredient_Base WHERE name = 'Tomate';


INSERT INTO StockMovement (id_ingredient, quantity, type, unit, creation_datetime)
SELECT id, 1.0, 'OUT', 'KG', '2024-01-06 12:00:00'
FROM Ingredient_Base WHERE name = 'Poulet';


INSERT INTO StockMovement (id_ingredient, quantity, type, unit, creation_datetime)
SELECT id, 0.3, 'OUT', 'KG', '2024-01-06 12:00:00'
FROM Ingredient_Base WHERE name = 'Chocolat';


INSERT INTO StockMovement (id_ingredient, quantity, type, unit, creation_datetime)
SELECT id, 0.2, 'OUT', 'KG', '2024-01-06 12:00:00'
FROM Ingredient_Base WHERE name = 'Beurre';


CREATE TABLE IF NOT EXISTS CurrentStock (
                                            id_ingredient INTEGER PRIMARY KEY,
                                            quantity NUMERIC(10, 3) NOT NULL,
                                            unit VARCHAR(10) NOT NULL,
                                            last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            FOREIGN KEY (id_ingredient) REFERENCES Ingredient_Base(id) ON DELETE CASCADE
);

INSERT INTO CurrentStock (id_ingredient, quantity, unit, last_update) VALUES
                                                                          ((SELECT id FROM Ingredient_Base WHERE name = 'Laitue'), 5.0, 'KG', '2024-01-01 00:00:00'),
                                                                          ((SELECT id FROM Ingredient_Base WHERE name = 'Tomate'), 4.0, 'KG', '2024-01-01 00:00:00'),
                                                                          ((SELECT id FROM Ingredient_Base WHERE name = 'Poulet'), 10.0, 'KG', '2024-01-01 00:00:00'),
                                                                          ((SELECT id FROM Ingredient_Base WHERE name = 'Chocolat'), 3.0, 'KG', '2024-01-01 00:00:00'),
                                                                          ((SELECT id FROM Ingredient_Base WHERE name = 'Beurre'), 2.5, 'KG', '2024-01-01 00:00:00')
ON CONFLICT (id_ingredient) DO UPDATE SET
                                          quantity = EXCLUDED.quantity,
                                          last_update = EXCLUDED.last_update;

CREATE OR REPLACE VIEW StockView AS
SELECT
    ib.id as ingredient_id,
    ib.name as ingredient_name,
    ib.category,
    cs.quantity as current_stock,
    cs.unit as stock_unit,
    cs.last_update,
    COALESCE((
                 SELECT SUM(CASE WHEN type = 'IN' THEN quantity ELSE -quantity END)
                 FROM StockMovement sm
                 WHERE sm.id_ingredient = ib.id
             ), 0) as total_movement
FROM Ingredient_Base ib
         LEFT JOIN CurrentStock cs ON ib.id = cs.id_ingredient;


SELECT '=== DISH COSTS ===' as info;
SELECT
    d.id,
    d.name,
    COALESCE(SUM(ib.price * di.quantity_required), 0) as total_cost,
    d.selling_price
FROM Dish d
         LEFT JOIN DishIngredient di ON d.id = di.id_dish
         LEFT JOIN Ingredient_Base ib ON di.id_ingredient = ib.id
GROUP BY d.id, d.name, d.selling_price
ORDER BY d.id;

SELECT '=== STOCK LEVELS (as of 2024-01-06 12:00) ===' as info;
SELECT
    ib.name,
    5.0 - COALESCE(SUM(CASE WHEN sm.type = 'OUT' THEN sm.quantity ELSE 0 END), 0) as stock_after_movements
FROM Ingredient_Base ib
         LEFT JOIN StockMovement sm ON ib.id = sm.id_ingredient
    AND sm.creation_datetime <= '2024-01-06 12:00:00'
WHERE ib.name IN ('Laitue', 'Tomate', 'Poulet', 'Chocolat', 'Beurre')
GROUP BY ib.name, ib.id
ORDER BY ib.id;


GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mini_dish_db_manager;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mini_dish_db_manager;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO mini_dish_db_manager;

\c postgres;