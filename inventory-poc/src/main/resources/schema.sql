DROP TABLE IF EXISTS current_stock;
DROP TABLE IF EXISTS inventory_movements;
DROP TABLE IF EXISTS products;
-- For H2, use IDENTITY for auto-increment. For SQL Server, it's IDENTITY(1,1)
CREATE TABLE products (
    id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(255),
    price DOUBLE PRECISION
);

CREATE TABLE inventory_movements (
    id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'IN', 'OUT'
    quantity INT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    notes VARCHAR(1000),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE current_stock (
    product_id BIGINT NOT NULL PRIMARY KEY,
    quantity INT NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id)
);
