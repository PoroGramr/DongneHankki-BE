package org.netway.dongnehankki.user.application;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.netway.dongnehankki.global.auth.jwt.JwtTokenProvider;
import org.netway.dongnehankki.global.exception.user.DuplicateUserNameException;
import org.netway.dongnehankki.global.exception.user.UnregisteredUserException;
import org.netway.dongnehankki.user.application.dto.response.UserResponse;
import org.netway.dongnehankki.user.application.dto.login.LoginRequest;
import org.netway.dongnehankki.user.application.dto.singup.CustomerSingUpRequest;
import org.netway.dongnehankki.user.domain.User;
import org.netway.dongnehankki.user.imfrastructure.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class UserService {

    private  final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserResponse customerJoin(CustomerSingUpRequest customerSingUpRequest){
        userRepository.findById(customerSingUpRequest.getId()).ifPresent(it -> {
            throw new DuplicateUserNameException();
        });

        String encodedPassword = passwordEncoder.encode(customerSingUpRequest.getPassword());
        User user = userRepository.save(User.ofCustomer(customerSingUpRequest.getId(), encodedPassword, customerSingUpRequest.getNickname()));

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
