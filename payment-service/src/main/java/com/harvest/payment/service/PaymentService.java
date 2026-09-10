package com.harvest.payment.service;

import com.harvest.payment.domain.Payment;
import com.harvest.payment.domain.ProcessedEvent;
import com.harvest.payment.repo.PaymentRepository;
import com.harvest.payment.repo.ProcessedEventRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final RestClient restClient;
    private final String internalKey;

    @Value("${app.stripe.secret-key:}")
    private String stripeSecret;
    @Value("${app.stripe.success-url:http://localhost:5173/order/success?session_id={CHECKOUT_SESSION_ID}}")
    private String successUrl;
    @Value("${app.stripe.cancel-url:http://localhost:5173/cart}")
    private String cancelUrl;
    @Value("${services.order:http://localhost:8085}")
    private String orderUrl;

    public PaymentService(PaymentRepository paymentRepository,
                          ProcessedEventRepository processedEventRepository,
                          RestClient restClient,
                          @Value("${app.internal-key}") String internalKey) {
        this.paymentRepository = paymentRepository;
        this.processedEventRepository = processedEventRepository;
        this.restClient = restClient;
        this.internalKey = internalKey;
    }

    public SessionResponse createSession(String orderNumber, long amountPaise) {
        Payment payment = paymentRepository.findByOrderNumber(orderNumber).orElseGet(Payment::new);
        payment.setOrderNumber(orderNumber);
        payment.setAmountPaise(amountPaise);
        payment.setStatus("PENDING");
        String checkoutUrl;
        String sessionId;
        if (stripeSecret != null && !stripeSecret.isBlank()) {
            try {
                Stripe.apiKey = stripeSecret;
                SessionCreateParams params = SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(successUrl)
                        .setCancelUrl(cancelUrl)
                        .setClientReferenceId(orderNumber)
                        .addLineItem(SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("inr")
                                        .setUnitAmount(amountPaise)
                                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder().setName("Harvest & Co Order " + orderNumber).build())
                                        .build())
                                .build())
                        .build();
                Session session = Session.create(params);
                checkoutUrl = session.getUrl();
                sessionId = session.getId();
            } catch (StripeException e) {
                throw new IllegalArgumentException("Unable to create Stripe session");
            }
        } else {
            sessionId = "demo_" + UUID.randomUUID();
            checkoutUrl = "http://localhost:5173/order/success?orderNumber=" + orderNumber;
        }
        payment.setStripeSessionId(sessionId);
        payment = paymentRepository.save(payment);
        return new SessionResponse(payment.getId(), checkoutUrl);
    }

    public void handleWebhook(String eventId, String orderNumber, String status) {
        if (processedEventRepository.existsByEventId(eventId)) {
            return;
        }
        ProcessedEvent event = new ProcessedEvent();
        event.setEventId(eventId);
        processedEventRepository.save(event);

        Payment payment = paymentRepository.findByOrderNumber(orderNumber).orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        payment.setStatus(status);
        paymentRepository.save(payment);
        if ("PAID".equals(status)) {
            restClient.post().uri(orderUrl + "/internal/orders/" + orderNumber + "/paid")
                    .header("X-Internal-Key", internalKey).contentType(MediaType.APPLICATION_JSON).retrieve().toBodilessEntity();
        }
    }

    public List<Payment> list() {
        return paymentRepository.findAll();
    }

    public record SessionResponse(String paymentId, String checkoutUrl) {}
    public record WebhookPayload(String eventId, String orderNumber, String status) {}
}