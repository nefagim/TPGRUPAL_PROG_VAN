package com.empresa.demostockapp.controller.export;

import com.empresa.demostockapp.dto.export.SalesDataExportDTO;
import com.empresa.demostockapp.service.export.DataExportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Added import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/data-export")
public class DataExportController {

    private final DataExportService dataExportService;

    public DataExportController(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @GetMapping("/sales-orders")
    @PreAuthorize("hasRole('ADMIN')") // Added annotation
    public ResponseEntity<List<SalesDataExportDTO>> exportSalesData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam Optional<Long> productId) {

        if (startDate.isAfter(endDate)) {
            // Returning a ResponseEntity with a bad request status and potentially an error message body.
            // For simplicity, returning an empty list or a specific error DTO might be options.
            // Here, using Collections.emptyList() with badRequest status.
            // A more robust solution would be a custom error object or using ProblemDetail.
            // return ResponseEntity.badRequest().body(Collections.singletonList("Start date cannot be after end date.")); // Example error message
            return ResponseEntity.badRequest().build(); // Simple bad request
        }

        List<SalesDataExportDTO> data = dataExportService.getSalesDataForExport(startDate, endDate, productId);
        return ResponseEntity.ok(data);
    }
}
