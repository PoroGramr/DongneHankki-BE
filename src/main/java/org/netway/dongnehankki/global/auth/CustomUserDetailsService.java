package org.netway.dongnehankki.global.auth;

import lombok.RequiredArgsConstructor;
import org.netway.dongnehankki.user.imfrastructure.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.netway.dongnehankki.global.exception.user.UnregisteredUserException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UnregisteredUserException {
        return userRepository.findById(username)
                .map(CustomUserDetails::new)
                .orElseThrow(UnregisteredUserException::new);
    }
}
