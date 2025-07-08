package org.netway.dongnehankki.user.application;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.netway.dongnehankki.global.exception.user.DuplicateUserNameException;
import org.netway.dongnehankki.global.exception.user.UnregisteredUserException;
import org.netway.dongnehankki.user.application.dto.response.UserResponse;
import org.netway.dongnehankki.user.application.dto.login.LoginRequest;
import org.netway.dongnehankki.user.application.dto.singup.CustomerSingUpRequest;
import org.netway.dongnehankki.user.domain.User;
import org.netway.dongnehankki.user.imfrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private  final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse customerJoin(CustomerSingUpRequest customerSingUpRequest){
        userRepository.findById(customerSingUpRequest.getId()).ifPresent(it -> {
            throw new DuplicateUserNameException();
        });

        String encodedPassword = passwordEncoder.encode(customerSingUpRequest.getPassword());
        User user = userRepository.save(User.ofCustomer(customerSingUpRequest.getId(), encodedPassword, customerSingUpRequest.getNickname()));

        return UserResponse.fromEntity(user);
    }

    // TODO : implement
    public String login(LoginRequest loginRequest){
        Optional<User> user = Optional.ofNullable(userRepository.findById(loginRequest.getId())
            .orElseThrow(() -> new UnregisteredUserException()));

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())){
            throw new UnregisteredUserException();
        }

        // TODO : 토큰 생성
        return "";
    }

}
