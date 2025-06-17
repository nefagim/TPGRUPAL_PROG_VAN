package com.empresa.demostockapp.service.stock;

import com.empresa.demostockapp.dto.stock.StockItemResponseDTO;
import com.empresa.demostockapp.dto.stock.UpdateStockQuantityRequestDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.Product;
import com.empresa.demostockapp.model.StockItem;
import com.empresa.demostockapp.repository.ProductRepository;
import com.empresa.demostockapp.repository.StockItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StockService {

    private final StockItemRepository stockItemRepository;
    private final ProductRepository productRepository;

    public StockService(StockItemRepository stockItemRepository, ProductRepository productRepository) {
        this.stockItemRepository = stockItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public StockItemResponseDTO getStockByProductId(Long productId) {
        Optional<StockItem> stockItemOpt = stockItemRepository.findByProductId(productId);

        if (stockItemOpt.isPresent()) {
            StockItem stockItem = stockItemOpt.get();
            return convertToDTO(stockItem);
        } else {
            // Check if product exists
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
            // Product exists, but no stock item, so return with quantity 0
            return new StockItemResponseDTO(productId, product.getName(), 0, null); // lastUpdated is null as no stock record
        }
    }

    @Transactional
    public StockItemResponseDTO updateStockQuantity(Long productId, UpdateStockQuantityRequestDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        StockItem stockItem = stockItemRepository.findByProductId(productId)
                .orElseGet(() -> {
                    StockItem newStockItem = new StockItem();
                    newStockItem.setProduct(product);
                    // lastUpdated will be set by @PrePersist
                    return newStockItem;
                });

        stockItem.setQuantity(dto.getQuantity());
        // lastUpdated will be set by @PrePersist or @PreUpdate

        StockItem savedStockItem = stockItemRepository.save(stockItem);
        return convertToDTO(savedStockItem);
    }

    private StockItemResponseDTO convertToDTO(StockItem stockItem) {
        return new StockItemResponseDTO(
                stockItem.getProduct().getId(),
                stockItem.getProduct().getName(),
                stockItem.getQuantity(),
                stockItem.getLastUpdated()
        );
    }
}
