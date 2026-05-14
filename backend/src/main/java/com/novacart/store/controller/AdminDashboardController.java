package com.novacart.store.controller;

import com.novacart.store.dto.ApiResponse;
import com.novacart.store.dto.DashboardMetricsResponse;
import com.novacart.store.dto.InventoryWarningResponse;
import com.novacart.store.service.AdminDashboardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard/metrics")
    public ApiResponse<DashboardMetricsResponse> getMetrics() {
        return ApiResponse.success("Dashboard metrics loaded successfully.", adminDashboardService.getMetrics());
    }

    @GetMapping("/inventory/warnings")
    public ApiResponse<List<InventoryWarningResponse>> getInventoryWarnings(
            @RequestParam(defaultValue = "5") int threshold
    ) {
        return ApiResponse.success("Inventory warnings loaded successfully.", adminDashboardService.getInventoryWarnings(threshold));
    }
}
