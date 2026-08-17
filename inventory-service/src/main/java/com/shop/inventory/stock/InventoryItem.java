package com.shop.inventory.stock;

import jakarta.persistence.*;
import lombok.Getter;


@Entity
@Table(name = "inventory")
@Getter
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productCode;
    private int availableQuantity;

    protected InventoryItem(){}

    public InventoryItem(String productCode, int availableQuantity){
        this.productCode= productCode;
        this.availableQuantity=availableQuantity;
    }
    public void reduceStock(int quantity) {
        if (quantity <= 0 || quantity > this.availableQuantity) {
            throw new IllegalArgumentException(
                    "Cannot reduce " + quantity + " from stock of " + this.availableQuantity + " for " + productCode);
        }
        this.availableQuantity -= quantity;
    }

}
