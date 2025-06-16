package com.example.inventorypoc.repository;

import com.example.inventorypoc.model.CurrentStock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class JdbcCurrentStockRepository implements CurrentStockRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcCurrentStockRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static class CurrentStockRowMapper implements RowMapper<CurrentStock> {
        @Override
        public CurrentStock mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new CurrentStock(
                    rs.getLong("product_id"),
                    rs.getInt("quantity"),
                    rs.getTimestamp("last_updated").toLocalDateTime()
            );
        }
    }

    @Override
    public Optional<CurrentStock> findByProductId(Long productId) {
        String sql = "SELECT * FROM current_stock WHERE product_id = ?";
        try {
            CurrentStock stock = jdbcTemplate.queryForObject(sql, new Object[]{productId}, new CurrentStockRowMapper());
            return Optional.ofNullable(stock);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public CurrentStock save(CurrentStock currentStock) {
        // Attempt to update first, if it fails (returns 0 rows affected), then insert.
        // This is a common way to do upsert with JDBC if MERGE isn't used or available.
        String updateSql = "UPDATE current_stock SET quantity = ?, last_updated = ? WHERE product_id = ?";
        int updatedRows = jdbcTemplate.update(updateSql,
                currentStock.getQuantity(),
                Timestamp.valueOf(currentStock.getLastUpdated()),
                currentStock.getProductId());

        if (updatedRows == 0) {
            // Insert if no rows were updated
            String insertSql = "INSERT INTO current_stock (product_id, quantity, last_updated) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertSql,
                    currentStock.getProductId(),
                    currentStock.getQuantity(),
                    Timestamp.valueOf(currentStock.getLastUpdated()));
        }
        return currentStock; // Or fetch again to ensure consistency
    }
}
