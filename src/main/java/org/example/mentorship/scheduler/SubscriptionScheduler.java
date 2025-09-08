package org.example.mentorship.scheduler;

import org.example.mentorship.entity.Mentor;
import org.example.mentorship.repository.MentorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SubscriptionScheduler {

    private final MentorRepository mentorRepository;

    @Autowired
    public SubscriptionScheduler(MentorRepository mentorRepository) {
        this.mentorRepository = mentorRepository;
    }

    @Scheduled(cron = "0 0 0 * * *") // Every day at midnight
    @Transactional
    public void checkExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<Mentor> mentors = mentorRepository.findAll();

        for (Mentor mentor : mentors) {
            if (mentor.isSubscriptionActive() &&
                    mentor.getSubscriptionExpiryDate() != null &&
                    mentor.getSubscriptionExpiryDate().isBefore(now)) {

                mentor.setSubscriptionActive(false);
                mentor.setSubscriptionExpiryDate(null);
                mentorRepository.save(mentor);
            }
        }
    }
}
