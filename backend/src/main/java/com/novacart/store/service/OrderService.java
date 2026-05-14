package com.novacart.store.service;

import com.novacart.store.dto.CheckoutRequest;
import com.novacart.store.dto.OrderResponse;
import com.novacart.store.entity.OrderStatus;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CheckoutRequest request);

    OrderResponse findOrder(Long id);

    List<OrderResponse> findAdminOrders();

    OrderResponse updateStatus(Long id, OrderStatus status);
}
