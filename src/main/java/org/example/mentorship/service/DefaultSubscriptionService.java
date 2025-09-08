package org.example.mentorship.service;

import org.example.mentorship.dto.PaymentRequest;
import org.example.mentorship.dto.PaymentResponse;
import org.example.mentorship.entity.Mentor;
import org.example.mentorship.entity.SubscriptionPayment;
import org.example.mentorship.repository.MentorRepository;
import org.example.mentorship.repository.SubscriptionPaymentRepository;
import org.example.mentorship.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class DefaultSubscriptionService implements SubscriptionService {

    private final MentorRepository mentorRepository;
    private final SubscriptionPaymentRepository paymentRepository;
    private final CurrentUserProvider currentUserProvider;

    @Autowired
    public DefaultSubscriptionService(
            MentorRepository mentorRepository,
            SubscriptionPaymentRepository paymentRepository,
            CurrentUserProvider currentUserProvider) {
        this.mentorRepository = mentorRepository;
        this.paymentRepository = paymentRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public ResponseEntity<PaymentResponse> processPayment(PaymentRequest paymentRequest) {
        return currentUserProvider.withCurrentUser(currentUser -> {
            // Check if the user is a mentor
            Optional<Mentor> mentorOpt = mentorRepository.findByUser(currentUser);
            if (mentorOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new PaymentResponse(false, "Only mentors can purchase a subscription", null));
            }

            Mentor mentor = mentorOpt.get();

            // Validate the card
            if (!validateCard(paymentRequest)) {
                return ResponseEntity.badRequest().body(
                        new PaymentResponse(false, "Invalid card details", null));
            }

            // Process the payment (simulation)
            String transactionId = processPaymentWithGateway(paymentRequest);

            // Save payment information
            SubscriptionPayment payment = new SubscriptionPayment();
            payment.setMentor(mentor);
            payment.setPaymentAmount(new BigDecimal("3.00")); // 3 hryvnias
            payment.setPaymentStatus("COMPLETED");
            payment.setTransactionId(transactionId);
            paymentRepository.save(payment);

            // Activate the subscription
            mentor.setSubscriptionActive(true);
            // Subscription valid for 30 days
            mentor.setSubscriptionExpiryDate(LocalDateTime.now().plusDays(30));
            mentorRepository.save(mentor);

            return ResponseEntity.ok(new PaymentResponse(
                    true, "Subscription successfully activated", transactionId));
        });
    }

    // Card validation
    private boolean validateCard(PaymentRequest paymentRequest) {
        // Basic validation
        String cardNumber = paymentRequest.cardNumber().replaceAll("\\s", "");

        // Check card number
        if (!cardNumber.matches("\\d{16}")) {
            return false;
        }

        // Check expiry date (MM/YY)
        if (!paymentRequest.expiryDate().matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            return false;
        }

        // Check CVV
        if (!paymentRequest.cvv().matches("\\d{3}")) {
            return false;
        }

        return true;
    }

    // Simulated payment processing through a payment gateway
    private String processPaymentWithGateway(PaymentRequest paymentRequest) {
        // In a real application, this would integrate with a payment gateway
        // Include in pom.xml from https://github.com/liqpay/sdk-java/raw/repository
        // Add public-key, private-key, receiver in application.properties
//        LiqPay liqpay = new LiqPay(liqpayPublicKey, liqpayPrivateKey);
//
//        // Generate a unique transaction ID
//        String orderId = "SUBSCRIPTION_" + mentor.getId() + "_" + UUID.randomUUID().toString().substring(0, 8);
//
//        // Format the sender's card
//        String senderCard = paymentRequest.cardNumber().replaceAll("\\s", "");
//        String[] expiryParts = paymentRequest.expiryDate().split("/");
//        String expiryMonth = expiryParts[0];
//        String expiryYear = "20" + expiryParts[1]; // Convert "YY" to "20YY"
//
//        // Create parameters for the API request
//        Map<String, String> params = new HashMap<>();
//        params.put("action", "p2p");
//        params.put("version", "3");
//        params.put("amount", "3"); // 3 hryvnias
//        params.put("currency", "UAH");
//        params.put("description", "Mentor subscription on the mentoring service");
//        params.put("order_id", orderId);
//        params.put("card", senderCard);
//        params.put("card_exp_month", expiryMonth);
//        params.put("card_exp_year", expiryYear);
//        params.put("card_cvv", paymentRequest.cvv());
//        params.put("receiver_card", receiverCard); // Receiver's card (your card)
//
//        // Execute the API request to LiqPay
//        Map<String, Object> response = liqpay.api("request", params);
//
//        // Check the result
//        if (response.containsKey("result") && "ok".equals(response.get("result"))) {
//            return orderId;
//        } else {
//            String errorMessage = response.containsKey("err_description")
//                    ? response.get("err_description").toString()
//                    : "Unknown payment error";
//            throw new Exception(errorMessage);
//        }
        return UUID.randomUUID().toString();
    }
}
