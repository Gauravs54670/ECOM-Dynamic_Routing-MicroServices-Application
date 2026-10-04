package com.gaurav.ECOM_OrderService.Controller;

import com.gaurav.ECOM_OrderService.DTO.ItemDTO;
import com.gaurav.ECOM_OrderService.Service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order-service")
public class OrderServiceController {

    private final OrderService orderService;
    public OrderServiceController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDTO>  getItemDTO(@PathVariable("itemId") Integer itemId) {

        return ResponseEntity.ok(this.orderService.getItemDTO(itemId));

    }
}
