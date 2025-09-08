package org.example.mentorship.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mentorship.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    @Value("${app.verification.expiration-hours}")
    private int expirationHours;

    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void removeUnverifiedUsers() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(expirationHours);

        int deletedCount = userRepository.deleteByEmailVerifiedFalseAndCreatedAtBefore(cutoffTime);

        if (deletedCount > 0) {
            log.info("Removed {} unverified users", deletedCount);
        }
    }
}
