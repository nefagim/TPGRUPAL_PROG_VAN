package com.empresa.demostockapp.service.export;

import com.empresa.demostockapp.dto.export.SalesDataExportDTO;
import com.empresa.demostockapp.model.SalesOrder;
import com.empresa.demostockapp.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DataExportService {

    private final SalesOrderRepository salesOrderRepository;

    public DataExportService(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = salesOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<SalesDataExportDTO> getSalesDataForExport(LocalDate startDate, LocalDate endDate, Optional<Long> optionalProductId) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<SalesOrder> salesOrders;

        if (optionalProductId.isPresent()) {
            salesOrders = salesOrderRepository.findByProductIdAndOrderDateBetween(
                    optionalProductId.get(), startDateTime, endDateTime);
        } else {
            // This method (findAllByOrderDateBetween) will be added to SalesOrderRepository in the next step
            salesOrders = salesOrderRepository.findAllByOrderDateBetween(startDateTime, endDateTime);
        }

        return salesOrders.stream()
                .map(SalesDataExportDTO::new) // Assumes SalesDataExportDTO has a constructor SalesDataExportDTO(SalesOrder salesOrder)
                .collect(Collectors.toList());
    }
}
