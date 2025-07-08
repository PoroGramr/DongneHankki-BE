package org.netway.dongnehankki.user.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.netway.dongnehankki.global.common.ApiResponse;
import org.netway.dongnehankki.user.application.UserService;
import org.netway.dongnehankki.user.application.dto.login.LoginRequest;
import org.netway.dongnehankki.user.application.dto.singUp.CustomerSingUpRequest;
import org.netway.dongnehankki.user.application.dto.singUp.OwnerSingUpRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @Value("${jwt.access-token-expiration-minutes}")
    private long accessTokenExpirationMinutes;

    @PostMapping("/customers")
    public ResponseEntity<ApiResponse<Void>> signUpCustomer(@RequestBody CustomerSingUpRequest customerSingUpRequest) {
        userService.customerJoin(customerSingUpRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/owners")
    public ResponseEntity<ApiResponse<Void>> signUpOwner(@RequestBody OwnerSingUpRequest ownerSingUpRequest) {
        userService.ownerJoin(ownerSingUpRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        String token = userService.login(loginRequest);

        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge((int) (accessTokenExpirationMinutes * 60));

        response.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.success());
    }

}
