package com.example.inventorypoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement // Enable Spring's annotation-driven transaction management
public class InventoryPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryPocApplication.class, args);
    }

}
