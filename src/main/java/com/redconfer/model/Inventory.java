package com.redconfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    private String id;
    private String name;
    private String sku;
    private String category; // e.g. "Cámaras", "DVRs", "Cables", "Accesorios"
    private int stock;
    private int minStockAlert;
    private double unitCost;
    private double unitPrice;
    private String supplier;
}
