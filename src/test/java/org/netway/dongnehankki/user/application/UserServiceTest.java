package org.netway.dongnehankki.user.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.netway.dongnehankki.global.auth.jwt.JwtTokenProvider;
import org.netway.dongnehankki.global.exception.user.DuplicateUserNameException;
import org.netway.dongnehankki.global.exception.user.UnregisteredUserException;
import org.netway.dongnehankki.store.domain.Store;
import org.netway.dongnehankki.store.imfrastructure.StoreRepository;
import org.netway.dongnehankki.user.application.dto.login.LoginRequest;
import org.netway.dongnehankki.user.application.dto.singup.CustomerSingUpRequest;
import org.netway.dongnehankki.user.application.dto.singup.OwnerSingUpRequest;
import org.netway.dongnehankki.user.domain.User;
import org.netway.dongnehankki.user.fixture.CustomerUserFixture;
import org.netway.dongnehankki.user.fixture.OwnerUserFixture;
import org.netway.dongnehankki.user.imfrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private StoreRepository storeRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void 일반회원_회원가입이_정상적으로_동작하는경우() {
        String id = "id";
        String password = "password";
        String nickname = "nickname";

        when(userRepository.findById(id)).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(CustomerUserFixture.get(id, password));
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        Assertions.assertDoesNotThrow(() -> userService.customerJoin(new CustomerSingUpRequest(id,password,nickname)));
    }

    @Test
    void 사장회원_회원가입이_정상적으로_동작하는경우() {
        String id = "id";
        String password = "password";
        String nickname = "nickname";
        Long storeId = 1L;
        Store mockStore = mock(Store.class);

        when(userRepository.findById(id)).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(OwnerUserFixture.get(id, password,mockStore));
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(mockStore.getStoreId()).thenReturn(storeId);
        when(storeRepository.findByStoreId(storeId)).thenReturn(Optional.of(mockStore));

        Assertions.assertDoesNotThrow(() -> userService.ownerJoin(new OwnerSingUpRequest(id,password,nickname,storeId)));
    }

    @Test
    void 일반회원_회원가입시_id가_이미_존재하는_경우() {
        String id = "id";
        String password = "password";
        String nickname = "nickname";

        User fixture = CustomerUserFixture.get(id, password);

        when(userRepository.findById(id)).thenReturn(Optional.of(fixture));

        Assertions.assertThrows(DuplicateUserNameException.class, () -> userService.customerJoin(new CustomerSingUpRequest(id,password,nickname)));
    }

    @Test
    void 사장회원_회원가입시_id가_이미_존재하는_경우() {
        String id = "id";
        String password = "password";
        String nickname = "nickname";
        Long storeId = 1L;
        Store mockStore = mock(Store.class);

        User fixture = OwnerUserFixture.get(id, password,mockStore);

        when(userRepository.findById(id)).thenReturn(Optional.of(fixture));
        when(userRepository.save(any())).thenReturn(OwnerUserFixture.get(id, password,mockStore));
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(mockStore.getStoreId()).thenReturn(storeId);
        when(storeRepository.findByStoreId(storeId)).thenReturn(Optional.of(mockStore));

        Assertions.assertThrows(DuplicateUserNameException.class, () -> userService.ownerJoin(new OwnerSingUpRequest(id,password,nickname,storeId)));
    }

    @Test
    void 로그인이_정상적으로_동작하는_경우() {
        String id = "id";
        String password = "password";

        User fixture = CustomerUserFixture.get(id, password);
        when(userRepository.findById(id)).thenReturn(Optional.of(fixture));

        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(jwtTokenProvider.generateToken(authentication)).thenReturn("dummy_jwt_token");

        Assertions.assertDoesNotThrow(() -> userService.login(new LoginRequest(id,password)));
    }

    @Test
    void 회원가입하지_않은_정보로_로그인하는_경우() {
        String id = "id";
        String password = "password";

        User fixture = CustomerUserFixture.get(id, password);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(
            UnregisteredUserException.class, () -> userService.login(new LoginRequest(id,password)));
    }

}
