package org.netway.dongnehankki.user.application;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.netway.dongnehankki.global.exception.user.UnregisteredUserException;
import org.netway.dongnehankki.user.application.dto.login.LoginRequest;
import org.netway.dongnehankki.user.application.dto.singup.CustomerSingUpRequest;
import org.netway.dongnehankki.user.domain.User;
import org.netway.dongnehankki.user.imfrastructure.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private  final UserRepository userRepository;

    // TODO : implement
    public User customerJoin(CustomerSingUpRequest customerSingUpRequest){
        Optional<User> user =  userRepository.findById(customerSingUpRequest.getId());

        return new User();
    }

    // TODO : implement
    public String login(LoginRequest loginRequest){
        Optional<User> user = Optional.ofNullable(userRepository.findById(loginRequest.getId())
            .orElseThrow(() -> new UnregisteredUserException()));

        if(!user.get().getPassword().equals(loginRequest.getPassword())){
            throw new UnregisteredUserException();
        }
        // TODO : 토큰 생성
        return "";
    }

}
