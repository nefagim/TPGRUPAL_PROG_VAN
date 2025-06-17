package com.empresa.demostockapp.service.sales;

import com.empresa.demostockapp.dto.sales.SalesOrderRequestDTO;
import com.empresa.demostockapp.dto.sales.SalesOrderResponseDTO;
import com.empresa.demostockapp.dto.stock.UpdateStockQuantityRequestDTO;
import com.empresa.demostockapp.exception.InsufficientStockException;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.Product;
import com.empresa.demostockapp.model.SalesOrder;
import com.empresa.demostockapp.model.StockItem;
import com.empresa.demostockapp.repository.ProductRepository;
import com.empresa.demostockapp.repository.SalesOrderRepository;
import com.empresa.demostockapp.repository.StockItemRepository;
import com.empresa.demostockapp.service.stock.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final StockItemRepository stockItemRepository; // Added for direct stock access

    public SalesOrderService(SalesOrderRepository salesOrderRepository,
                             ProductRepository productRepository,
                             StockService stockService,
                             StockItemRepository stockItemRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.productRepository = productRepository;
        this.stockService = stockService;
        this.stockItemRepository = stockItemRepository;
    }

    @Transactional
    public SalesOrderResponseDTO recordSale(SalesOrderRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));

        // Check current stock
        StockItem stockItem = stockItemRepository.findByProductId(dto.getProductId())
                .orElse(new StockItem(product, 0)); // Assume 0 stock if no record

        if (stockItem.getQuantity() < dto.getQuantitySold()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName() +
                    ". Available: " + stockItem.getQuantity() + ", Requested: " + dto.getQuantitySold());
        }

        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setProduct(product);
        salesOrder.setQuantitySold(dto.getQuantitySold());
        salesOrder.setSellingPrice(dto.getSellingPrice());
        // orderDate is set by @PrePersist

        SalesOrder savedSalesOrder = salesOrderRepository.save(salesOrder);

        // Update stock quantity
        int newQuantity = stockItem.getQuantity() - dto.getQuantitySold();
        UpdateStockQuantityRequestDTO stockUpdateRequest = new UpdateStockQuantityRequestDTO(newQuantity);
        stockService.updateStockQuantity(dto.getProductId(), stockUpdateRequest);

        return convertToDTO(savedSalesOrder);
    }

    @Transactional(readOnly = true)
    public List<SalesOrderResponseDTO> getSalesForProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        List<SalesOrder> salesOrders = salesOrderRepository.findByProductId(productId);
        return salesOrders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SalesOrderResponseDTO convertToDTO(SalesOrder salesOrder) {
        return new SalesOrderResponseDTO(
                salesOrder.getId(),
                salesOrder.getProduct().getId(),
                salesOrder.getProduct().getName(),
                salesOrder.getQuantitySold(),
                salesOrder.getSellingPrice(),
                salesOrder.getOrderDate()
        );
    }
}
