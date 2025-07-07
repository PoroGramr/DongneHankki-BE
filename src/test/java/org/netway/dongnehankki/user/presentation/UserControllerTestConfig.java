package org.netway.dongnehankki.user.presentation;

import static org.mockito.Mockito.mock;

import org.netway.dongnehankki.user.application.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class UserControllerTestConfig {
    @Bean
    public UserService userService() {
        return mock(UserService.class);  // Mockito mock
    }
}
