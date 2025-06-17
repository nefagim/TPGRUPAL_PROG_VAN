package com.empresa.demostockapp.controller.export;

import com.empresa.demostockapp.dto.export.SalesDataExportDTO;
import com.empresa.demostockapp.security.jwt.JwtUtils;
import com.empresa.demostockapp.security.services.UserDetailsServiceImpl;
import com.empresa.demostockapp.service.export.DataExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;


@WebMvcTest(DataExportController.class)
class DataExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataExportService dataExportService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService; // For security context

    @MockBean
    private JwtUtils jwtUtils; // For security context

    @Autowired
    private ObjectMapper objectMapper;

    private SalesDataExportDTO salesDataExportDTO;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        salesDataExportDTO = new SalesDataExportDTO();
        salesDataExportDTO.setOrderId(1L);
        salesDataExportDTO.setProductId(100L);
        salesDataExportDTO.setProductName("Test Product");
        salesDataExportDTO.setQuantitySold(10);
        salesDataExportDTO.setSellingPrice(BigDecimal.valueOf(19.99));
        salesDataExportDTO.setOrderDate(LocalDateTime.now());

        startDate = LocalDate.of(2023, 1, 1);
        endDate = LocalDate.of(2023, 1, 31);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportSalesData_withAdminRole_withProductId_success() throws Exception {
        when(dataExportService.getSalesDataForExport(eq(startDate), eq(endDate), eq(Optional.of(100L))))
                .thenReturn(List.of(salesDataExportDTO));

        mockMvc.perform(get("/api/data-export/sales-orders")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-01-31")
                        .param("productId", "100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId", is(1)))
                .andExpect(jsonPath("$[0].productId", is(100)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportSalesData_withAdminRole_withoutProductId_success() throws Exception {
        when(dataExportService.getSalesDataForExport(eq(startDate), eq(endDate), eq(Optional.empty())))
                .thenReturn(List.of(salesDataExportDTO));

        mockMvc.perform(get("/api/data-export/sales-orders")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId", is(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportSalesData_withAdminRole_emptyResult() throws Exception {
        when(dataExportService.getSalesDataForExport(eq(startDate), eq(endDate), eq(Optional.empty())))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/data-export/sales-orders")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void exportSalesData_withAdminRole_invalidDateRange() throws Exception {
        // startDate after endDate
        mockMvc.perform(get("/api/data-export/sales-orders")
                        .param("startDate", "2023-01-31")
                        .param("endDate", "2023-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void exportSalesData_withAdminRole_missingStartDateParam() throws Exception {
        mockMvc.perform(get("/api/data-export/sales-orders")
                        .param("endDate", "2023-01-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void exportSalesData_withAdminRole_missingEndDateParam() throws Exception {
        mockMvc.perform(get("/api/data-export/sales-orders")
                        .param("startDate", "2023-01-01"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(roles = "USER") // Non-admin role
    void exportSalesData_withUserRole_forbidden() throws Exception {
        // No need to mock service, as security should block before controller method is hit
        mockMvc.perform(get("/api/data-export/sales-orders")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-01-31"))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportSalesData_withoutAuth_unauthorized() throws Exception {
        // No need to mock service
        mockMvc.perform(get("/api/data-export/sales-orders")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-01-31"))
                .andExpect(status().isUnauthorized()); // Or 401 if AuthEntryPointJwt is configured, which it is
    }
}
