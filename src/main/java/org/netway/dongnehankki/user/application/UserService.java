package org.netway.dongnehankki.user.application;

import org.netway.dongnehankki.user.application.dto.login.LoginRequest;
import org.netway.dongnehankki.user.application.dto.singup.CustomerSingUpRequest;
import org.netway.dongnehankki.user.domain.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    // TODO : implement
    public User CustomerJoin(CustomerSingUpRequest customerSingUpRequest){
        return new User();
    }

    // TODO : implement
    public String login(LoginRequest loginRequest){
        return "";
    }
}
