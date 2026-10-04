package com.gaurav.ECOM_InventoryService.Service;


import com.gaurav.ECOM_InventoryService.Model.Item;

public interface InventoryService {

    String registerItem(Item item);
    Item findItemById(int id);

}
