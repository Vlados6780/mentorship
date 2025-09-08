package org.example.mentorship.repository;

import org.example.mentorship.entity.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Integer> {
}
