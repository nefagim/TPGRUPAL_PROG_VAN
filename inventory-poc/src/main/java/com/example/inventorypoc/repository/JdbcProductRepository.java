package com.example.inventorypoc.repository;

import com.example.inventorypoc.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcProductRepository implements ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static class ProductRowMapper implements RowMapper<Product> {
        @Override
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Product(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getDouble("price")
            );
        }
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null || product.getId() == 0) {
            // For simplicity, this example assumes ID is managed elsewhere or pre-set
            // A real implementation would use GeneratedKeyHolder for auto-generated keys
            // or a sequence if not auto-increment.
            // This is a placeholder for actual insert logic that handles ID generation.
            // For now, let's assume an ID is provided for save to mean insert OR update.
            // This part needs refinement for a real application.
            jdbcTemplate.update("INSERT INTO products (id, name, description, category, price) VALUES (?, ?, ?, ?, ?)",
                    product.getId(), product.getName(), product.getDescription(), product.getCategory(), product.getPrice());
        } else {
            update(product);
        }
        return product; // Or fetch again if ID was generated
    }

    @Override
    public Optional<Product> findById(Long id) {
        try {
            Product product = jdbcTemplate.queryForObject("SELECT * FROM products WHERE id = ?",
                    new Object[]{id},
                    new ProductRowMapper());
            return Optional.ofNullable(product);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findAll() {
        return jdbcTemplate.query("SELECT * FROM products", new ProductRowMapper());
    }

    @Override
    public int update(Product product) {
        return jdbcTemplate.update("UPDATE products SET name = ?, description = ?, category = ?, price = ? WHERE id = ?",
                product.getName(), product.getDescription(), product.getCategory(), product.getPrice(), product.getId());
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM products WHERE id = ?", id);
    }
}
