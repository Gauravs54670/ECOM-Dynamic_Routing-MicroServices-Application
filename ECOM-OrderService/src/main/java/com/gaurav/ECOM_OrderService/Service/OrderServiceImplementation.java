package com.gaurav.ECOM_OrderService.Service;

import com.gaurav.ECOM_OrderService.DTO.ItemDTO;
import com.gaurav.ECOM_OrderService.Service.Client.InventoryClient;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImplementation implements OrderService{


    private final InventoryClient inventoryClient;
    public OrderServiceImplementation(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    @Override
    public ItemDTO getItemDTO(Integer itemId) {
        return this.inventoryClient.getItemById(itemId);
    }

}
