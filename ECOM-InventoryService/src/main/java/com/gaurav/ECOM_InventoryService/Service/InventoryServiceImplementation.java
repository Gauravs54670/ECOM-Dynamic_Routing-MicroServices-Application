package com.gaurav.ECOM_InventoryService.Service;

import com.gaurav.ECOM_InventoryService.Model.Item;
import com.gaurav.ECOM_InventoryService.Repository.InventoryRepository;
import org.springframework.stereotype.Service;


@Service
public class InventoryServiceImplementation implements InventoryService {


    private final InventoryRepository inventoryRepository;

    public InventoryServiceImplementation(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public String registerItem(Item item) {
        this.inventoryRepository.save(item);
        return "Item registered successfully";
    }

    @Override
    public Item findItemById(int id) {
        return this.inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }

}
