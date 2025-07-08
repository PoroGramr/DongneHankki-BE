package org.netway.dongnehankki.user.application.dto.singUp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerSingUpRequest {
    private String id;
    private String password;
    private String nickname;
}
