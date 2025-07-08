package org.netway.dongnehankki.user.application.dto.login;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshTokenRequest {
    private String refreshToken;
}