package org.netway.dongnehankki.user.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.netway.dongnehankki.global.exception.CustomException;
import org.netway.dongnehankki.global.exception.user.DuplicateUserNameException;
import org.netway.dongnehankki.global.exception.user.UnregisteredUserException;
import org.netway.dongnehankki.user.application.dto.login.LoginRequest;
import org.netway.dongnehankki.user.application.dto.singup.CustomerSingUpRequest;
import org.netway.dongnehankki.user.domain.User;
import org.netway.dongnehankki.user.fixture.UserFixture;
import org.netway.dongnehankki.user.imfrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void 회원가입이_정상적으로_동작하는경우() {
        String id = "id";
        String password = "password";
        String nickname = "nickname";

        when(userRepository.findById(id)).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(Optional.of(UserFixture.get(id, password)));

        Assertions.assertDoesNotThrow(() -> userService.customerJoin(new CustomerSingUpRequest(id,password,nickname)));
    }

    @Test
    void 회원가입시_id가_이미_존재하는_경우() {
        String id = "id";
        String password = "password";
        String nickname = "nickname";

        User fixture = UserFixture.get(id, password);

        when(userRepository.findById(id)).thenReturn(Optional.of(fixture));

        Assertions.assertThrows(DuplicateUserNameException.class, () -> userService.customerJoin(new CustomerSingUpRequest(id,password,nickname)));
    }

    @Test
    void 로그인이_정상적으로_동작하는_경우() {
        String id = "id";
        String password = "password";

        User fixture = UserFixture.get(id, password);
        when(userRepository.findById(id)).thenReturn(Optional.of(fixture));

        Assertions.assertDoesNotThrow(() -> userService.login(new LoginRequest(id,password)));
    }

    @Test
    void 회원가입하지_않은_정보로_로그인하는_경우() {
        String id = "id";
        String password = "password";

        User fixture = UserFixture.get(id, password);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(
            UnregisteredUserException.class, () -> userService.login(new LoginRequest(id,password)));
    }

}
