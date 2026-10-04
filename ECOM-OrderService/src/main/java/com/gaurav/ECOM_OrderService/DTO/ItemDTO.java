package com.gaurav.ECOM_OrderService.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Service

public class ItemDTO {

    private String itemName;
    private String itemDescription;
    private double price;

}
