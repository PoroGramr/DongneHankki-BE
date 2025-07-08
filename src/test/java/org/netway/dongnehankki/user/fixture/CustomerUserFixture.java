package org.netway.dongnehankki.user.fixture;

import org.netway.dongnehankki.user.domain.User;

public class CustomerUserFixture {

    // Test시 사용하는 가짜 User Entity
    public static User get(String id, String password){
        User result = new User();
        result.setUserId(1L);
        result.setId(id);
        result.setPassword(password);
        result.setRole(User.Role.CUSTOMER);
        return result;
    }

}
