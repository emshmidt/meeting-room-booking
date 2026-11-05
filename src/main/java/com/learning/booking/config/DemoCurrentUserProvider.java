package com.learning.booking.config;

import com.learning.booking.domain.User;
import com.learning.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DemoCurrentUserProvider implements CurrentUserProvider{
    private final UserRepository userRepository;
    private final UUID demoUserId;
    DemoCurrentUserProvider (UserRepository userRepository,
                             @Value("${app.demo-user}") String demoUser) {
        UUID demoUserId1;

        if(userRepository == null) {
            throw new IllegalArgumentException("User repository must not be null");
        }
        if (demoUser == null) {
            throw new IllegalArgumentException("Text must not be null");
        }


        switch (demoUser) {
            case "anna":
                demoUserId1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
                break;
            case "boris":
                demoUserId1 = UUID.fromString("00000000-0000-0000-0000-000000000002");
                break;
            case "vera":
                demoUserId1 = UUID.fromString("00000000-0000-0000-0000-000000000003");
                break;
            default:
                throw new IllegalArgumentException("Unknown app.demo-user: " + demoUser);
        }

        this.userRepository = userRepository;
        this.demoUserId = demoUserId1;

    }

    @Override
    public User getCurrentUser() {
        return
                userRepository.findById(demoUserId).orElseThrow(
                        () -> new IllegalStateException("User not found")
                );
    }
}
