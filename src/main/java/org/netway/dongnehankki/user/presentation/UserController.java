package org.netway.dongnehankki.user.presentation;

import lombok.RequiredArgsConstructor;
import org.netway.dongnehankki.global.common.ApiResponse;
import org.netway.dongnehankki.user.application.UserService;
import org.netway.dongnehankki.user.application.dto.login.LoginRequest;
import org.netway.dongnehankki.user.application.dto.singUp.CustomerSingUpRequest;
import org.netway.dongnehankki.user.application.dto.singUp.OwnerSingUpRequest;
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

    @PostMapping("/customers")
    public  ResponseEntity<ApiResponse<CustomerSingUpRequest>> customerJoin(@RequestBody CustomerSingUpRequest customerSingUpRequest){
        userService.customerJoin(customerSingUpRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/owners")
    public  ResponseEntity<ApiResponse<OwnerSingUpRequest>> ownerJoin(@RequestBody OwnerSingUpRequest ownerSingUpRequest){
        userService.ownerJoin(ownerSingUpRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/login")
    public  ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest loginRequest){
        String token = userService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(token));
    }

}
