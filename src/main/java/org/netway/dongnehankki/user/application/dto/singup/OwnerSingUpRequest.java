package org.netway.dongnehankki.user.application.dto.singup;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OwnerSingUpRequest {
    private String id;
    private String password;
    private String nickname;
    private Long storeId;
}
