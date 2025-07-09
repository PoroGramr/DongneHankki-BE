package org.netway.dongnehankki.user.fixture;

import org.netway.dongnehankki.user.domain.User;

public class CustomerUserFixture {

    // Test시 사용하는 가짜 User Entity
    public static User get(String id, String password){
        return User.builder()
                .userId(1L)
                .id(id)
                .password(password)
                .role(User.Role.CUSTOMER)
                .build();
    }

}
