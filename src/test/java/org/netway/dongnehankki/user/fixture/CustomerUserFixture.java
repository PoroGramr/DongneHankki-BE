package org.netway.dongnehankki.user.fixture;

import java.lang.reflect.Field;
import java.time.LocalDate;
import org.netway.dongnehankki.user.domain.User;

public class CustomerUserFixture {

    public static User get(Long userId, String loginId, String password, String nickname, String name, String phoneNumber, LocalDate birth) {
        User user = User.ofCustomer(loginId, password, nickname, name, phoneNumber, birth);
        if (userId != null) {
            try {
                Field field = User.class.getDeclaredField("userId");
                field.setAccessible(true);
                field.set(user, userId);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return user;
    }

    public static User get(String loginId, String password, String nickname, String name, String phoneNumber, LocalDate birth) {
        return get(null, loginId, password, nickname, name, phoneNumber, birth);
    }
}
