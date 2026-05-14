package com.novacart.store.service;

import com.novacart.store.dto.DashboardMetricsResponse;
import com.novacart.store.dto.InventoryWarningResponse;
import java.util.List;

public interface AdminDashboardService {

    DashboardMetricsResponse getMetrics();

    List<InventoryWarningResponse> getInventoryWarnings(int threshold);
}
