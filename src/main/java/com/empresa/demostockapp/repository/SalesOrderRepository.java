package com.empresa.demostockapp.repository;

import com.empresa.demostockapp.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    List<SalesOrder> findByProductId(Long productId);
    List<SalesOrder> findByProductIdAndOrderDateBetween(Long productId, LocalDateTime startDate, LocalDateTime endDate);
}
