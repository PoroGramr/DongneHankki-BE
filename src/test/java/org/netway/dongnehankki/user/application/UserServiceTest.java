package org.netway.dongnehankki.user.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.netway.dongnehankki.global.auth.jwt.JwtTokenProvider;
import org.netway.dongnehankki.global.auth.jwt.RefreshToken;
import org.netway.dongnehankki.global.auth.jwt.RefreshTokenRepository;
import org.netway.dongnehankki.user.exception.DuplicateUserNameException;
import org.netway.dongnehankki.user.exception.InvalidPasswordException;
import org.netway.dongnehankki.user.exception.InvalidRefreshTokenException;
import org.netway.dongnehankki.user.exception.UnregisteredUserException;
import org.netway.dongnehankki.store.domain.Store;
import org.netway.dongnehankki.store.infrastructure.StoreRepository;
import org.netway.dongnehankki.user.dto.login.LoginRequest;
import org.netway.dongnehankki.user.dto.login.LoginResponse;
import org.netway.dongnehankki.user.dto.signUp.CustomerSignUpRequest;
import org.netway.dongnehankki.user.dto.signUp.OwnerSignUpRequest;
import org.netway.dongnehankki.user.domain.User;
import org.netway.dongnehankki.user.fixture.CustomerUserFixture;
import org.netway.dongnehankki.user.fixture.OwnerUserFixture;
import org.netway.dongnehankki.user.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void 일반회원_회원가입이_정상적으로_동작하는경우() {
        String id = "id";
        String password = "password";
        String nickname = "nickname";

        when(userRepository.findById(id)).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(CustomerUserFixture.get(id, password));
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        Assertions.assertDoesNotThrow(() -> userService.customerJoin(new CustomerSignUpRequest(id,password,nickname)));
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

        Assertions.assertDoesNotThrow(() -> userService.ownerJoin(new OwnerSignUpRequest(id,password,nickname,storeId)));
    }

    @Test
    void 일반회원_회원가입시_id가_이미_존재하는_경우() {
        String id = "id";
        String password = "password";
        String nickname = "nickname";

        User fixture = CustomerUserFixture.get(id, password);

        when(userRepository.findById(id)).thenReturn(Optional.of(fixture));

        Assertions.assertThrows(DuplicateUserNameException.class, () -> userService.customerJoin(new CustomerSignUpRequest(id,password,nickname)));
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

        Assertions.assertThrows(DuplicateUserNameException.class, () -> userService.ownerJoin(new OwnerSignUpRequest(id,password,nickname,storeId)));
    }

    @Test
    void 로그인이_정상적으로_동작하는_경우() {
        String id = "id";
        String password = "password";
        Long userId = 1L;

        User fixture = CustomerUserFixture.get(id, password);
        when(userRepository.findById(id)).thenReturn(Optional.of(fixture));

        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(jwtTokenProvider.generateToken(authentication)).thenReturn("dummy_access_token");
        when(jwtTokenProvider.generateRefreshToken(userId)).thenReturn("dummy_refresh_token");
        when(jwtTokenProvider.getRefreshTokenExpirationMinutes()).thenReturn(1440L); // 24시간

        Assertions.assertDoesNotThrow(() -> userService.login(new LoginRequest(id,password)));
    }

    @Test
    void 회원가입하지_않은_정보로_로그인하는_경우() {
        String id = "id";
        String password = "password";

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(
            UnregisteredUserException.class, () -> userService.login(new LoginRequest(id,password)));
    }

    @Test
    void 로그인시_비밀번호가_틀린_경우() {
        String id = "id";
        String password = "password";
        String wrongPassword = "wrong_password";

        User fixture = CustomerUserFixture.get(id, password);
        when(userRepository.findById(id)).thenReturn(Optional.of(fixture));

        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException(""));

        Assertions.assertThrows(
            InvalidPasswordException.class, () -> userService.login(new LoginRequest(id, wrongPassword)));
    }

    @Test
    void 리프레시_토큰_재발급이_정상적으로_동작하는_경우() {
        Long userId = 1L;
        String oldRefreshToken = "old_refresh_token";
        String newAccessToken = "new_access_token";
        String newRefreshToken = "new_refresh_token";

        User userFixture = CustomerUserFixture.get("id", "password");

        RefreshToken storedRefreshToken = RefreshToken.builder()
                .userId(userId)
                .token(oldRefreshToken)
                .expiration(1440L * 60)
                .build();

        when(jwtTokenProvider.validateToken(oldRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(oldRefreshToken)).thenReturn(userId);
        when(refreshTokenRepository.findById(userId)).thenReturn(Optional.of(storedRefreshToken));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userFixture));
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn(newAccessToken);
        when(jwtTokenProvider.generateRefreshToken(userId)).thenReturn(newRefreshToken);
        when(jwtTokenProvider.getRefreshTokenExpirationMinutes()).thenReturn(1440L);

        LoginResponse response = userService.reissueTokens(oldRefreshToken);

        Assertions.assertEquals(newAccessToken, response.getAccessToken());
        Assertions.assertEquals(newRefreshToken, response.getRefreshToken());
    }

    @Test
    void 유효하지_않은_리프레시_토큰으로_재발급을_요청하는_경우() {
        String invalidRefreshToken = "invalid_refresh_token";

        // 시나리오 1: 토큰 유효성 검증 실패
        when(jwtTokenProvider.validateToken(invalidRefreshToken)).thenReturn(false);
        Assertions.assertThrows(InvalidRefreshTokenException.class, () -> userService.reissueTokens(invalidRefreshToken));

        // 시나리오 2: Redis에 토큰이 없는 경우
        when(jwtTokenProvider.validateToken(invalidRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(invalidRefreshToken)).thenReturn(1L);
        when(refreshTokenRepository.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(InvalidRefreshTokenException.class, () -> userService.reissueTokens(invalidRefreshToken));

        // 시나리오 3: Redis에 저장된 토큰과 요청된 토큰이 일치하지 않는 경우
        String mismatchedRefreshToken = "mismatched_refresh_token";
        RefreshToken storedRefreshToken = RefreshToken.builder()
                .userId(1L)
                .token("actual_stored_token")
                .expiration(1440L * 60)
                .build();
        when(jwtTokenProvider.validateToken(mismatchedRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(mismatchedRefreshToken)).thenReturn(1L);
        when(refreshTokenRepository.findById(1L)).thenReturn(Optional.of(storedRefreshToken));
        Assertions.assertThrows(InvalidRefreshTokenException.class, () -> userService.reissueTokens(mismatchedRefreshToken));
    }
}
