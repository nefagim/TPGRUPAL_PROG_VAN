package com.example.inventorypoc.repository;

import com.example.inventorypoc.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
    int update(Product product);
    int deleteById(Long id);
    // Optional: deleteAll for cleanup, or findByName, etc. for more specific queries
}
