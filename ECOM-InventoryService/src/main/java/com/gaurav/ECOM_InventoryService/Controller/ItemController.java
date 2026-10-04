package com.gaurav.ECOM_InventoryService.Controller;

import com.gaurav.ECOM_InventoryService.Model.Item;
import com.gaurav.ECOM_InventoryService.Service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory-service")
public class ItemController {

    private final InventoryService inventoryService;

    public ItemController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerItem(@RequestBody Item item) {

        String response = this.inventoryService.registerItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> findItemById(@PathVariable("id") int id) {

        Item item = this.inventoryService.findItemById(id);
        return ResponseEntity.ok().body(item);

    }
}
