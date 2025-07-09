package org.netway.dongnehankki.user.dto.signUp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerSignUpRequest {
    private String id;
    private String password;
    private String nickname;
}
