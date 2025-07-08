package org.netway.dongnehankki.user.application.dto.signUp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerSignUpRequest {
    private String id;
    private String password;
    private String nickname;
}
