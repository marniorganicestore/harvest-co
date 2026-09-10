package com.harvest.payment.web;

import com.harvest.common.security.UserContextResolver;
import com.harvest.payment.domain.Payment;
import com.harvest.payment.service.PaymentService;
import com.harvest.payment.service.PaymentService.SessionResponse;
import com.harvest.payment.service.PaymentService.WebhookPayload;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/internal/payments/session")
    public SessionResponse createSession(@RequestBody SessionRequest request) {
        return paymentService.createSession(request.orderNumber(), request.amountPaise());
    }

    @PostMapping("/api/webhooks/stripe")
    public void webhook(@RequestBody WebhookPayload payload) {
        paymentService.handleWebhook(payload.eventId(), payload.orderNumber(), payload.status());
    }

    @GetMapping("/api/admin/payments")
    public List<Payment> payments(HttpServletRequest request) {
        if (!UserContextResolver.fromHeaders(request).isAdmin()) {
            throw new IllegalArgumentException("Admin access required");
        }
        return paymentService.list();
    }

    public record SessionRequest(String orderNumber, long amountPaise) {}
}