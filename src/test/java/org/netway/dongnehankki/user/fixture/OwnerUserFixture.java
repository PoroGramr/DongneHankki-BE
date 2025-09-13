package org.netway.dongnehankki.user.fixture;

import java.lang.reflect.Field;
import java.time.LocalDate;
import org.netway.dongnehankki.store.domain.Store;
import org.netway.dongnehankki.user.domain.User;

public class OwnerUserFixture {

    public static User get(Long userId, String loginId, String password, String nickname, String name, String phoneNumber, Store store, LocalDate birth) {
        User user = User.ofOwner(loginId, password, nickname, name, phoneNumber, store, birth);
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

    public static User get(String loginId, String password, String nickname, String name, String phoneNumber, Store store, LocalDate birth) {
        return get(null, loginId, password, nickname, name, phoneNumber, store, birth);
    }
}
