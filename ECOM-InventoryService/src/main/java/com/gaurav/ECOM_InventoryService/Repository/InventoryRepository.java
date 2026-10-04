package com.gaurav.ECOM_InventoryService.Repository;

import com.gaurav.ECOM_InventoryService.Model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface InventoryRepository extends JpaRepository<Item, Integer> {
}
