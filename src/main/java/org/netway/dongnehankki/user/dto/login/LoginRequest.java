package org.netway.dongnehankki.user.dto.login;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequest {
    private String id;
    private String password;
}
