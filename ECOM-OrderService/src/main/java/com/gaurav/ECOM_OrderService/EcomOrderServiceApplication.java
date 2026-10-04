package com.gaurav.ECOM_OrderService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EcomOrderServiceApplication {

	public static void main(String[] args) {


		SpringApplication.run(EcomOrderServiceApplication.class, args);
		System.out.println("Order Service Application Started");

	}

}
