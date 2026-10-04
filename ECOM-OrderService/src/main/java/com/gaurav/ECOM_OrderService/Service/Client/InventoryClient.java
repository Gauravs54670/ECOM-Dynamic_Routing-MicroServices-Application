package com.gaurav.ECOM_OrderService.Service.Client;

import com.gaurav.ECOM_OrderService.DTO.ItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "ECOM-InventoryService",
        configuration = InventoryClientConfiguration.class
)
public interface InventoryClient {

    @GetMapping("/api/inventory-service/{itemId}")
    ItemDTO getItemById(@PathVariable("itemId") Integer itemId);

}
