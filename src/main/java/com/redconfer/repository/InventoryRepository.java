package com.redconfer.repository;

import com.redconfer.model.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends MongoRepository<Inventory, String> {
    List<Inventory> findByCategory(String category);
    List<Inventory> findByStockLessThanEqual(int stockThreshold);
}
