CREATE TABLE orders (
 id BIGINT  NOT NULL AUTO_INCREMENT,  -- maps to Long id; AUTO_INCREMENT = the IDENTITY strategy
 customer_id  BIGINT  NOT NULL,                 -- snake_case of customerId
 product_code VARCHAR(100)  NOT NULL,         -- snake_case of productCode
 quantity     INT           NOT NULL,
 amount       DECIMAL(12,2) NOT NULL,          -- money: 12 digits total, 2 after decimal
 status       VARCHAR(30)   NOT NULL,         -- enum stored as text (EnumType.STRING)
 created_at   DATETIME(6)   NOT NULL,                 -- Instant with microsecond precision
 PRIMARY KEY (id)
);