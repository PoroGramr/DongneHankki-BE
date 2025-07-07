package org.netway.dongnehankki.global.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    // Swagger UI 접근을 허용하기 위한 URL 배열
    private static final String[] SWAGGER_URLS = {
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. 요청에 대한 인가 규칙 설정
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(SWAGGER_URLS).permitAll()
                .requestMatchers("/", "/login").permitAll() // 루트(/)와 /login 경로는 누구나 접근 허용
                .anyRequest().authenticated() // 그 외 모든 요청은 인증된 사용자만 접근 가능
            ).formLogin(form -> form
                .defaultSuccessUrl("/", true)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
