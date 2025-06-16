package com.example.inventorypoc.repository;

import com.example.inventorypoc.model.InventoryMovement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class JdbcInventoryMovementRepository implements InventoryMovementRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcInventoryMovementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static class InventoryMovementRowMapper implements RowMapper<InventoryMovement> {
        @Override
        public InventoryMovement mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new InventoryMovement(
                    rs.getLong("id"),
                    rs.getLong("product_id"),
                    InventoryMovement.MovementType.valueOf(rs.getString("type")),
                    rs.getInt("quantity"),
                    rs.getTimestamp("timestamp").toLocalDateTime(),
                    rs.getString("notes")
            );
        }
    }

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        // Assuming 'inventory_movements' table with auto-incrementing 'id'
        String sql = "INSERT INTO inventory_movements (product_id, type, quantity, timestamp, notes) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, movement.getProductId());
            ps.setString(2, movement.getType().name());
            ps.setInt(3, movement.getQuantity());
            ps.setTimestamp(4, Timestamp.valueOf(movement.getTimestamp()));
            ps.setString(5, movement.getNotes());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            movement.setId(keyHolder.getKey().longValue());
        } else {
            // Fallback or error handling if key was not generated (e.g. if table not set up for auto-gen keys)
            // For PoC, we might just proceed assuming it worked or an ID was pre-set if not null
            // This part might need to be more robust in a real application
        }
        return movement;
    }

    @Override
    public List<InventoryMovement> findByProductId(Long productId) {
        String sql = "SELECT * FROM inventory_movements WHERE product_id = ?";
        return jdbcTemplate.query(sql, new Object[]{productId}, new InventoryMovementRowMapper());
    }
}
