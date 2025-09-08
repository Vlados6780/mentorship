package org.example.mentorship.security;

import org.example.mentorship.entity.User;
import org.example.mentorship.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.transaction.annotation.Transactional;


@Component
public class CurrentUserProvider {

    private final UserRepository userRepository;

    @Autowired
    public CurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() instanceof String) {
            return Optional.empty();
        }

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer userId = userDetails.getUser().getId();
            // Reboot the user to avoid LazyInitializationException
            return userRepository.findById(userId);
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public <T> ResponseEntity<T> withCurrentUser(Function<User, ResponseEntity<T>> action) {
        return getCurrentUser()
                .map(action)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body((T) "Unauthorized access"));
    }
}
