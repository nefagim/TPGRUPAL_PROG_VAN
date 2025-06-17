package com.empresa.demostockapp.repository;

import com.empresa.demostockapp.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    List<SalesOrder> findByProductId(Long productId);
}
