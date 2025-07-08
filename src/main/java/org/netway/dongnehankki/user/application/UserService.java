package org.netway.dongnehankki.user.application;

import lombok.RequiredArgsConstructor;
import org.netway.dongnehankki.global.auth.jwt.JwtTokenProvider;
import org.netway.dongnehankki.global.auth.jwt.RefreshToken;
import org.netway.dongnehankki.global.auth.jwt.RefreshTokenRepository;
import org.netway.dongnehankki.global.exception.user.DuplicateUserNameException;
import org.netway.dongnehankki.global.exception.user.InvalidPasswordException;
import org.netway.dongnehankki.global.exception.store.UnregisteredStoreException;
import org.netway.dongnehankki.global.exception.user.InvalidRefreshTokenException;
import org.netway.dongnehankki.global.exception.user.UnregisteredUserException;
import org.netway.dongnehankki.user.application.dto.response.UserResponse;
import org.netway.dongnehankki.user.application.dto.login.LoginRequest;
import org.netway.dongnehankki.user.application.dto.login.LoginResponse;
import org.netway.dongnehankki.user.application.dto.singUp.CustomerSingUpRequest;
import org.netway.dongnehankki.user.application.dto.singUp.OwnerSingUpRequest;
import org.netway.dongnehankki.user.domain.User;
import org.netway.dongnehankki.user.infrastructure.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;

import org.netway.dongnehankki.store.domain.Store;
import org.netway.dongnehankki.store.imfrastructure.StoreRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private  final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StoreRepository storeRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserResponse customerJoin(CustomerSingUpRequest customerSingUpRequest){
        userRepository.findById(customerSingUpRequest.getId()).ifPresent(it -> {
            throw new DuplicateUserNameException();
        });

        String encodedPassword = passwordEncoder.encode(customerSingUpRequest.getPassword());
        User user = userRepository.save(User.ofCustomer(customerSingUpRequest.getId(), encodedPassword, customerSingUpRequest.getNickname()));

        return UserResponse.fromEntity(user);
    }

    public UserResponse ownerJoin(OwnerSingUpRequest ownerSingUpRequest) {
        userRepository.findById(ownerSingUpRequest.getId()).ifPresent(it -> {
            throw new DuplicateUserNameException();
        });

        Store store = storeRepository.findByStoreId(ownerSingUpRequest.getStoreId())
                .orElseThrow(() -> new UnregisteredStoreException());

        String encodedPassword = passwordEncoder.encode(ownerSingUpRequest.getPassword());
        User user = userRepository.save(User.ofOwner(ownerSingUpRequest.getId(), encodedPassword, ownerSingUpRequest.getNickname(), store));

        return UserResponse.fromEntity(user);
    }


    public LoginResponse login(LoginRequest loginRequest){
        User user = userRepository.findById(loginRequest.getId())
                .orElseThrow(UnregisteredUserException::new);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getId(), loginRequest.getPassword());

        Authentication authentication;
        try {
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            throw new InvalidPasswordException();
        }

        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getUserId())
                .token(refreshToken)
                .expiration(jwtTokenProvider.getRefreshTokenExpirationMinutes() * 60) // 초 단위로 저장
                .build());

        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse reissueTokens(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        RefreshToken storedRefreshToken = refreshTokenRepository.findById(userId)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!storedRefreshToken.getToken().equals(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        refreshTokenRepository.delete(storedRefreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(UnregisteredUserException::new);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(user.getId(), "", user.getAuthorities()),
                "",
                user.getAuthorities()
        );

        String newAccessToken = jwtTokenProvider.generateToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(userId)
                .token(newRefreshToken)
                .expiration(jwtTokenProvider.getRefreshTokenExpirationMinutes() * 60)
                .build());

        return new LoginResponse(newAccessToken, newRefreshToken);
    }


}
