package com.empresa.demostockapp.controller.sales;

import com.empresa.demostockapp.dto.sales.SalesOrderRequestDTO;
import com.empresa.demostockapp.dto.sales.SalesOrderResponseDTO;
import com.empresa.demostockapp.service.sales.SalesOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salesorders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @PostMapping
    public ResponseEntity<SalesOrderResponseDTO> recordSale(@Valid @RequestBody SalesOrderRequestDTO requestDTO) {
        SalesOrderResponseDTO responseDTO = salesOrderService.recordSale(requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<SalesOrderResponseDTO>> getSalesForProduct(@PathVariable Long productId) {
        List<SalesOrderResponseDTO> responseDTOs = salesOrderService.getSalesForProduct(productId);
        return ResponseEntity.ok(responseDTOs);
    }
}
