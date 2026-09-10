package com.harvest.order.web;

import com.harvest.common.security.UserContextResolver;
import com.harvest.order.domain.Order;
import com.harvest.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/checkout/sessions")
    public OrderService.CheckoutResponse checkout(HttpServletRequest request, @RequestBody CheckoutRequest checkoutRequest) {
        String userId = UserContextResolver.fromHeaders(request).userId();
        return orderService.checkout(userId, checkoutRequest.shippingAddress());
    }

    @GetMapping("/api/orders")
    public List<Order> orders(HttpServletRequest request) {
        String userId = UserContextResolver.fromHeaders(request).userId();
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("Unauthorized");
        return orderService.ordersByUser(userId);
    }

    @GetMapping("/api/orders/{orderNumber}")
    public Order order(HttpServletRequest request, @PathVariable String orderNumber) {
        String userId = UserContextResolver.fromHeaders(request).userId();
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("Unauthorized");
        return orderService.orderByNumber(userId, orderNumber);
    }

    @PostMapping("/internal/orders/{orderNumber}/paid")
    public Order markPaid(@PathVariable String orderNumber) {
        return orderService.markPaid(orderNumber);
    }

    @GetMapping("/internal/orders/{userId}/purchased/{productId}")
    public PurchaseResponse purchased(@PathVariable String userId, @PathVariable String productId) {
        return new PurchaseResponse(orderService.userPurchasedProduct(userId, productId));
    }

    @GetMapping("/api/admin/orders")
    public List<Order> adminOrders(HttpServletRequest request) {
        if (!UserContextResolver.fromHeaders(request).isAdmin()) throw new IllegalArgumentException("Admin access required");
        return orderService.allOrders();
    }

    @PatchMapping("/api/admin/orders/{orderNumber}")
    public Order adminUpdate(HttpServletRequest request, @PathVariable String orderNumber, @RequestBody StatusRequest statusRequest) {
        if (!UserContextResolver.fromHeaders(request).isAdmin()) throw new IllegalArgumentException("Admin access required");
        return orderService.updateStatus(orderNumber, statusRequest.status());
    }

    public record CheckoutRequest(String shippingAddress) {}
    public record PurchaseResponse(boolean purchased) {}
    public record StatusRequest(String status) {}
}