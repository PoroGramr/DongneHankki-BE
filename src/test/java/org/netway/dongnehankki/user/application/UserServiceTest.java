package org.netway.dongnehankki.user.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.netway.dongnehankki.global.auth.jwt.JwtTokenProvider;
import org.netway.dongnehankki.global.auth.jwt.RefreshToken;
import org.netway.dongnehankki.global.auth.jwt.RefreshTokenRepository;
import org.netway.dongnehankki.notification.dto.request.FCMTokenRequest;
import org.netway.dongnehankki.global.util.S3Service;
import org.netway.dongnehankki.store.exception.UnregisteredStoreException;
import org.netway.dongnehankki.store.infrastructure.repository.ReviewRepository;
import org.netway.dongnehankki.user.dto.request.UpdateUserRequest;
import org.netway.dongnehankki.user.dto.response.UserResponse;
import org.netway.dongnehankki.user.exception.DuplicateNickNameException;
import org.netway.dongnehankki.user.exception.DuplicateLoginIdException;
import org.netway.dongnehankki.user.exception.InvalidPasswordException;
import org.netway.dongnehankki.user.exception.InvalidRefreshTokenException;
import org.netway.dongnehankki.user.exception.UnregisteredUserException;
import org.netway.dongnehankki.store.domain.Store;
import org.netway.dongnehankki.store.infrastructure.repository.StoreRepository;
import org.netway.dongnehankki.user.dto.request.LoginRequest;
import org.netway.dongnehankki.user.dto.request.LoginResponse;
import org.netway.dongnehankki.user.dto.request.CustomerSignUpRequest;
import org.netway.dongnehankki.user.dto.request.OwnerSignUpRequest;
import org.netway.dongnehankki.user.domain.User;
import org.netway.dongnehankki.user.fixture.CustomerUserFixture;
import org.netway.dongnehankki.user.fixture.OwnerUserFixture;
import org.netway.dongnehankki.user.infrastructure.UserRepository;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private S3Service s3Service;

    @Test
    void 일반회원_회원가입이_정상적으로_동작하는경우() {
        // given
        String loginId = "id";
        String password = "password";
        String nickname = "nickname";
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";
        LocalDate birth =  LocalDate.of(2025,8,22);

        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(CustomerUserFixture.get(loginId, password,nickname, name, phoneNumber, birth));
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        // when & then
        Assertions.assertDoesNotThrow(() -> userService.customerSignUp(new CustomerSignUpRequest(loginId,password,nickname,name,phoneNumber,birth)));
    }

    @Test
    void 사장회원_회원가입이_정상적으로_동작하는경우() {
        // given
        String loginId = "id";
        String password = "password";
        String nickname = "nickname";
        String name = "김사장";
        String phoneNumber = "010-9876-5432";
        Long storeId = 1L;
        LocalDate birth = LocalDate.of(2025,8,22);
        Store mockStore = mock(Store.class);

        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(OwnerUserFixture.get(loginId, password,nickname ,name, phoneNumber, mockStore, birth));
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(mockStore.getStoreId()).thenReturn(storeId);
        when(storeRepository.findByStoreId(storeId)).thenReturn(Optional.of(mockStore));

        // when & then
        Assertions.assertDoesNotThrow(() -> userService.ownerSignUp(new OwnerSignUpRequest(loginId,password,name,phoneNumber,storeId,birth)));
    }

    @Test
    void 사장회원_회원가입이_등록되지않은_storeId로_가입하는경우_익셉션() {
        // given
        String loginId = "id";
        String password = "password";
        String nickname = "nickname";
        String name = "김사장";
        String phoneNumber = "010-9876-5432";
        Long storeId = 1L;
        LocalDate birth = LocalDate.of(2025,8,22);
        Store mockStore = mock(Store.class);

        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.empty());
        when(storeRepository.findByStoreId(storeId)).thenReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(UnregisteredStoreException.class,() -> userService.ownerSignUp(new OwnerSignUpRequest(loginId,password,name,phoneNumber,storeId,birth)));
    }

    @Test
    void 회원가입시_loginId_중복체크에서_중복이_없을경우() {
        // given
        String loginId = "existingId";

        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.empty());

        // when
        boolean isAvailable = userService.checkLoginId(loginId);

        // then
        assertThat(isAvailable).isTrue();
    }

    @Test
    void 회원가입시_loginId_중복체크에서_중복이_있을경우() {
        // given
        String loginId = "existingId";
        User existingUser = CustomerUserFixture.get(loginId, "password", "nickname", "name", "010-1111-1111", LocalDate.of(2025,8,22));

        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.of(existingUser));

        // when
        boolean isAvailable = userService.checkLoginId(loginId);

        // then
        assertThat(isAvailable).isFalse();
    }

    @Test
    void 회원가입시_nickname_중복체크에서_중복이_없을경우() {
        // given
        String nickname = "nickname";

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.empty());

        // when
        boolean isAvailable = userService.checkNickname(nickname);

        // then
        assertThat(isAvailable).isTrue();
    }

    @Test
    void 회원가입시_nickname_중복체크에서_중복이_있을경우() {
        // given
        String nickname = "nickname";
        User existingUser = CustomerUserFixture.get("loginId", "password", nickname, "name", "010-1111-1111", LocalDate.of(2025,8,22));

        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(existingUser));

        // when
        boolean isAvailable = userService.checkNickname(nickname);

        // then
        assertThat(isAvailable).isFalse();
    }

    @Test
    void 일반회원_회원가입시_id가_이미_존재하는_경우() {
        // given
        String loginId = "id";
        String password = "password";
        String nickname = "nickname";
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";
        LocalDate birth = LocalDate.of(2025,8,22);

        User fixture = CustomerUserFixture.get(loginId, password,nickname, name, phoneNumber,birth);

        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.of(fixture));

        // when & then
        Assertions.assertThrows(DuplicateLoginIdException.class, () -> userService.customerSignUp(new CustomerSignUpRequest(loginId,password,nickname,name,phoneNumber,birth)));
    }

    @Test
    void 사장회원_회원가입시_id가_이미_존재하는_경우() {
        // given
        String loginId = "id";
        String password = "password";
        String nickname = "nickname";
        String name = "김사장";
        String phoneNumber = "010-9876-5432";
        Long storeId = 1L;
        LocalDate birth = LocalDate.of(2025,8,22);
        Store mockStore = mock(Store.class);

        User fixture = OwnerUserFixture.get(loginId, password, nickname, name, phoneNumber, mockStore,birth);

        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.of(fixture));

        // when & then
        Assertions.assertThrows(DuplicateLoginIdException.class, () -> userService.ownerSignUp(new OwnerSignUpRequest(loginId,password,name,phoneNumber,storeId,birth)));
    }

    @Test
    void 로그인이_정상적으로_동작하는_경우() {
        // given
        String loginId = "id";
        String password = "password";
        String name = "name";
        String nickname = "nickname";
        String phoneNumber = "010-1111-1111";
        LocalDate birth = LocalDate.of(2025,8,22);
        Long userId = 1L;

        User fixture = CustomerUserFixture.get(userId, loginId, password,nickname, name, phoneNumber,birth);
        
        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.of(fixture));

        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(jwtTokenProvider.generateToken(authentication)).thenReturn("dummy_access_token");
        when(jwtTokenProvider.generateRefreshToken(userId)).thenReturn("dummy_refresh_token");
        when(jwtTokenProvider.getRefreshTokenExpirationMinutes()).thenReturn(1440L); // 24시간

        // when & then
        Assertions.assertDoesNotThrow(() -> userService.login(new LoginRequest(loginId,password)));
    }

    @Test
    void 회원가입하지_않은_정보로_로그인하는_경우() {
        // given
        String loginId = "id";
        String password = "password";

        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(
            UnregisteredUserException.class, () -> userService.login(new LoginRequest(loginId,password)));
    }

    @Test
    void 로그인시_비밀번호가_틀린_경우() {
        // given
        String loginId = "id";
        String password = "password";
        String nickname = "nickname";
        String name = "name";
        String phoneNumber = "010-1111-1111";
        String wrongPassword = "wrong_password";
        LocalDate birth = LocalDate.of(2025,8,22);

        User fixture = CustomerUserFixture.get(loginId, password, nickname, name, phoneNumber,birth);
        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.of(fixture));

        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException(""));

        // when & then
        Assertions.assertThrows(
            InvalidPasswordException.class, () -> userService.login(new LoginRequest(loginId, wrongPassword)));
    }

    @Test
    void 리프레시_토큰_재발급이_정상적으로_동작하는_경우() {
        // given
        Long userId = 1L;
        String oldRefreshToken = "old_refresh_token";
        String newAccessToken = "new_access_token";
        String newRefreshToken = "new_refresh_token";

        User userFixture = CustomerUserFixture.get("loginId", "password", "nickname", "name", "010-1111-1111", LocalDate.of(2025,8,22));

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

        // when
        LoginResponse response = userService.reissueTokens(oldRefreshToken);

        // then
        Assertions.assertEquals(newAccessToken, response.getAccessToken());
        Assertions.assertEquals(newRefreshToken, response.getRefreshToken());
    }

    @Test
    void 유효하지_않은_리프레시_토큰으로_재발급을_요청하는_경우() {
        // given
        String invalidRefreshToken = "invalid_refresh_token";

        // 시나리오 1: 토큰 유효성 검증 실패
        when(jwtTokenProvider.validateToken(invalidRefreshToken)).thenReturn(false);
        // when & then
        Assertions.assertThrows(InvalidRefreshTokenException.class, () -> userService.reissueTokens(invalidRefreshToken));

        // 시나리오 2: Redis에 토큰이 없는 경우
        when(jwtTokenProvider.validateToken(invalidRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(invalidRefreshToken)).thenReturn(1L);
        when(refreshTokenRepository.findById(1L)).thenReturn(Optional.empty());
        // when & then
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
        // when & then
        Assertions.assertThrows(InvalidRefreshTokenException.class, () -> userService.reissueTokens(mismatchedRefreshToken));
    }

    @Test
    void 고객_회원_수정이_성공적으로_동작하는_경우() {

        // given
        User existingUser = CustomerUserFixture.get(null, "loginId", "oldPass", "oldNick" ,"oldName", "010-1111-1111", LocalDate.of(2025,8,22));
        // anyLong() 사용
        given(userRepository.findById(anyLong()))
            .willReturn(Optional.of(existingUser));
        given(userRepository.save(any(User.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
        given(passwordEncoder.encode("newPass")).willReturn("newPass");

        UpdateUserRequest req = new UpdateUserRequest("newPass", "newNick");

        // when
        UserResponse resp = userService.updateUser(999L, req);

        // then
        assertThat(existingUser.getPassword()).isEqualTo("newPass");
        assertThat(existingUser.getNickname()).isEqualTo("newNick");
        assertThat(resp.getNickname()).isEqualTo("newNick");
    }

    @Test
    void 점주_회원_수정이_성공적으로_동작하는_경우() {

        // given
        Store mockStore = mock(Store.class);
                User existingUser = OwnerUserFixture.get(null, "loginId", "oldPass", "oldNick", "oldName", "010-1111-1111", mockStore,LocalDate.of(2025,8,22));
        // anyLong() 사용
        given(userRepository.findById(anyLong()))
            .willReturn(Optional.of(existingUser));
        given(userRepository.save(any(User.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
        given(passwordEncoder.encode("newPass")).willReturn("newPass");

        UpdateUserRequest req = new UpdateUserRequest("newPass", "newNick");

        // when
        UserResponse resp = userService.updateUser(999L, req);

        // then
        assertThat(existingUser.getPassword()).isEqualTo("newPass");
        assertThat(existingUser.getNickname()).isEqualTo("newNick");
        assertThat(existingUser.getStore()).isEqualTo(mockStore);
        assertThat(resp.getNickname()).isEqualTo("newNick");
    }

    @Test
    void 다른_유저가_사용중인_닉네임을_사용하는_경우() {
        // given
                User existingUser = CustomerUserFixture.get(999L, "loginId", "oldPass", "oldNick", "oldName", "010-1111-1111",LocalDate.of(2025,8,22));

                User anotherUser = CustomerUserFixture.get(1000L, "anotherLoginId", "pass", "newNick", "newName", "010-2222-2222",LocalDate.of(2025,8,22));

        given(userRepository.findById(999L)).willReturn(Optional.of(existingUser));
        given(userRepository.findByNickname("newNick")).willReturn(Optional.of(anotherUser));

        UpdateUserRequest req = new UpdateUserRequest("newPass", "newNick");

        // when & then
        Assertions.assertThrows(DuplicateNickNameException.class, () -> userService.updateUser(999L, req));
    }

    @Test
    void 유저가_자신의_닉네임으로_수정하는_경우_성공() {
        // given
        long userId = 999L;
        String nickname = "myNick";
                User existingUser = CustomerUserFixture.get(userId, "loginId", "oldPass", nickname, "oldName", "010-1111-1111",LocalDate.of(2025,8,22));

        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
        given(userRepository.findByNickname(nickname)).willReturn(Optional.of(existingUser));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest req = new UpdateUserRequest("newPass", nickname);

        // when & then
        Assertions.assertDoesNotThrow(() -> userService.updateUser(userId, req));
    }

    @Test
    void 고객_회원_닉네임만_수정이_성공적으로_동작하는_경우() {

        // given
        User existingUser = CustomerUserFixture.get("loginId", "oldPass", "oldNick", "oldName", "010-1111-1111",LocalDate.of(2025,8,22));
        // anyLong() 사용
        given(userRepository.findById(anyLong()))
            .willReturn(Optional.of(existingUser));
        given(userRepository.save(any(User.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest req = new UpdateUserRequest(null, "newNick");

        // when
        UserResponse resp = userService.updateUser(999L, req);

        // then
        assertThat(existingUser.getPassword()).isEqualTo("oldPass");
        assertThat(existingUser.getNickname()).isEqualTo("newNick");
        assertThat(resp.getNickname()).isEqualTo("newNick");
    }

    @Test
    void 고객_회원_패스워드만_수정이_성공적으로_동작하는_경우() {

        // given
        User existingUser = CustomerUserFixture.get("loginId", "oldPass", "oldNick","oldName", "010-1111-1111",LocalDate.of(2025,8,22));
        // anyLong() 사용
        given(userRepository.findById(anyLong()))
            .willReturn(Optional.of(existingUser));
        given(userRepository.save(any(User.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
        given(passwordEncoder.encode("newPass")).willReturn("newPass");

        UpdateUserRequest req = new UpdateUserRequest("newPass", null);

        // when
        UserResponse resp = userService.updateUser(999L, req);

        // then
        assertThat(existingUser.getPassword()).isEqualTo("newPass");
        assertThat(existingUser.getNickname()).isEqualTo("oldNick");
        assertThat(resp.getNickname()).isEqualTo("oldNick");
    }

    @Test
    void 고객_회원_패스워드만_수정하려고_할때_닉네임은_기존_닉네임을_입력하는_경우_성공적으로_동작하는_경우() {

        // given
        User existingUser = CustomerUserFixture.get("loginId", "oldPass", "oldNick","oldName", "010-1111-1111",LocalDate.of(2025,8,22));
        // anyLong() 사용
        given(userRepository.findById(anyLong()))
            .willReturn(Optional.of(existingUser));
        given(userRepository.save(any(User.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
        given(passwordEncoder.encode("newPass")).willReturn("newPass");

        UpdateUserRequest req = new UpdateUserRequest("newPass", "oldNick");

        // when
        UserResponse resp = userService.updateUser(999L, req);

        // then
        assertThat(existingUser.getPassword()).isEqualTo("newPass");
        assertThat(existingUser.getNickname()).isEqualTo("oldNick");
        assertThat(resp.getNickname()).isEqualTo("oldNick");
    }

    @Test
    void 유저_softDelete가_성공적으로_동작하는_경우(){
        // given
        long userId = 1L;
        User existingUser = CustomerUserFixture.get("loginId", "oldPass", "oldNick","oldName", "010-1111-1111",LocalDate.of(2025,8,22));

        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
        given(reviewRepository.findAllByUser(existingUser)).willReturn(Collections.emptyList());

        // when
        userService.deleteUser(userId);

        // then
        assertThat(existingUser.getDeletedAt()).isNotNull();

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        Assertions.assertThrows(UnregisteredUserException.class, () -> {
            userService.findByUserId(userId);
        });
    }

    @Test
    void 가입하지않은유저_softDelete시_실패하는_경우(){
        // given
        given(userRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(UnregisteredUserException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void userId로_유저_정보_찾기_성공하는_경우(){
        // given
        long userId = 1L;
        User existingUser = CustomerUserFixture.get("loginId", "oldPass", "oldNick","oldName", "010-1111-1111",LocalDate.of(2025,8,22));
        given(userRepository.findById(userId))
            .willReturn(Optional.of(existingUser));

        // when
        UserResponse resp = userService.findByUserId(1L);

        // then
        assertThat(resp.getNickname()).isEqualTo("oldNick");
    }

    @Test
    void 존재하지않는_userId로_유저_정보_찾기시_실패하는_경우(){
        // given
        long userId = 1L;
        given(userRepository.findById(userId))
            .willReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(UnregisteredUserException.class, () -> userService.findByUserId(1L));
    }

    @Test
    void 존재하지_않는_유저_정보_수정시_실패하는_경우() {
        // given
        long userId = 1L;
        UpdateUserRequest req = new UpdateUserRequest("newPass", "newNick");

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(UnregisteredUserException.class, () -> {
            userService.updateUser(userId, req);
        });
    }

    @Test
    void 리프레시_토큰_재발급시_존재하지_않는_유저인_경우_실패() {
        // given
        Long userId = 1L;
        String refreshToken = "valid_refresh_token";

        RefreshToken storedRefreshToken = RefreshToken.builder()
                .userId(userId)
                .token(refreshToken)
                .expiration(1440L * 60)
                .build();

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenReturn(userId);
        when(refreshTokenRepository.findById(userId)).thenReturn(Optional.of(storedRefreshToken));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(UnregisteredUserException.class, () -> {
            userService.reissueTokens(refreshToken);
        });
    }

    @Test
    void 기존_프로필_이미지가_있는_유저가_이미지_변경시_기존_이미지_삭제_성공() {
        // given
        long userId = 1L;
        String oldImageUrl = "http://s3.test.url/old-image.jpg";
        String newImageUrl = "http://s3.test.url/new-image.jpg";

        User existingUser = CustomerUserFixture.get(userId, "loginId", "pass", "nick", "name", "phone", LocalDate.now());
        existingUser.updateProfileImage(oldImageUrl);

        MockMultipartFile newProfileImage = new MockMultipartFile(
            "profileImage", "new-image.jpg", "image/jpeg", "new image content".getBytes()
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
        when(s3Service.uploadFile(any(), any())).thenReturn(newImageUrl);

        // when
        userService.updateProfileImage(userId, newProfileImage);

        // then
        verify(s3Service, times(1)).deleteFile(oldImageUrl);
        assertThat(existingUser.getProfileImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    void 기존_프로필_이미지_URL이_공백일때_삭제_호출하지_않음() {
        // given
        long userId = 1L;
        String blankImageUrl = " ";
        String newImageUrl = "http://s3.test.url/new-image.jpg";

        User existingUser = CustomerUserFixture.get(userId, "loginId", "pass", "nick", "name", "phone", LocalDate.now());
        existingUser.updateProfileImage(blankImageUrl);

        MockMultipartFile newProfileImage = new MockMultipartFile(
            "profileImage", "new-image.jpg", "image/jpeg", "new image content".getBytes()
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
        when(s3Service.uploadFile(any(), any())).thenReturn(newImageUrl);

        // when
        userService.updateProfileImage(userId, newProfileImage);

        // then
        verify(s3Service, never()).deleteFile(any());
        assertThat(existingUser.getProfileImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    void FCM_토큰_업데이트_성공() throws Exception {
        // given
        long userId = 1L;
        String fcmToken = "new-fcm-token";
        String json = "{\"token\":\"" + fcmToken + "\"}";
        FCMTokenRequest request = new ObjectMapper().readValue(json, FCMTokenRequest.class);

        User existingUser = CustomerUserFixture.get(userId, "loginId", "pass", "nick", "name", "phone", LocalDate.now());

        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

        // when
        userService.updateFcmToken(userId, request);

        // then
        assertThat(existingUser.getFcmToken()).isEqualTo(fcmToken);
    }

    @Test
    void FCM_토큰_업데이트시_유저가_없으면_예외발생() throws Exception {
        // given
        long userId = 999L; // 존재하지 않는 ID
        String fcmToken = "new-fcm-token";
        String json = "{\"token\":\"" + fcmToken + "\"}";
        FCMTokenRequest request = new ObjectMapper().readValue(json, FCMTokenRequest.class);

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(UnregisteredUserException.class, () -> {
            userService.updateFcmToken(userId, request);
        });
    }

    @Test
    void 유저_프로필_이미지_설정_성공(){
        // given
        long userId = 1L;
        User existingUser = CustomerUserFixture.get(userId, "loginId", "oldPass", "oldNick","oldName", "010-1111-1111",LocalDate.of(2025,8,22));
        MockMultipartFile profileImage = new MockMultipartFile(
            "profileImage",                    // parameter name
            "test-image.jpg",                 // original filename
            "image/jpeg",                     // content type
            "test image content".getBytes()   // file content
        );

        given(userRepository.findById(userId))
            .willReturn(Optional.of(existingUser));
        when(s3Service.uploadFile(any(), any())).thenReturn("http://s3.test.url/image.jpg");


        // when
        userService.updateProfileImage(userId, profileImage);

        // then
        assertThat(existingUser.getProfileImageUrl()).isEqualTo("http://s3.test.url/image.jpg");
    }
}
