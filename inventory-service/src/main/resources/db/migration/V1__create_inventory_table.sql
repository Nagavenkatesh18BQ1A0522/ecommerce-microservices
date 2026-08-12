CREATE TABLE inventory (
  id      BIGINT       NOT NULL AUTO_INCREMENT,
  product_code       VARCHAR(100) NOT NULL,
  available_quantity INT          NOT NULL,
  PRIMARY KEY (id),
 UNIQUE KEY uq_inventory_product_code (product_code)
);