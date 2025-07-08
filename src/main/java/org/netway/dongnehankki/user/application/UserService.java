package org.netway.dongnehankki.user.application;

import lombok.RequiredArgsConstructor;
import org.netway.dongnehankki.global.auth.jwt.JwtTokenProvider;
import org.netway.dongnehankki.global.exception.user.DuplicateUserNameException;
import org.netway.dongnehankki.global.exception.user.UnregisteredStoreException;
import org.netway.dongnehankki.global.exception.user.UnregisteredUserException;
import org.netway.dongnehankki.user.application.dto.response.UserResponse;
import org.netway.dongnehankki.user.application.dto.login.LoginRequest;
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


    public String login(LoginRequest loginRequest){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getId(), loginRequest.getPassword());

        Authentication authentication;
        try {
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            throw new UnregisteredUserException();
        }

        String accessToken = jwtTokenProvider.generateToken(authentication);

        return accessToken;
    }


}
