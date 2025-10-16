package org.netway.dongnehankki.global.util;

import lombok.RequiredArgsConstructor;
import org.netway.dongnehankki.global.auth.CustomUserDetails;
import org.netway.dongnehankki.global.auth.jwt.JwtTokenProvider;
import org.netway.dongnehankki.user.domain.User;
import org.netway.dongnehankki.user.infrastructure.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-data")
@RequiredArgsConstructor
public class TestDataController {

    private final TestDataService testDataService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/posts")
    public String generatePosts(@RequestParam(defaultValue = "10000") int count) {
        testDataService.generatePosts(count);
        return count + " posts generated.";
    }

    @GetMapping("/token")
    public ResponseEntity<String> getLikerUserToken() {
        String username = "liker_user";

        User user = userRepository.findByLoginId(username).orElse(null);
        if (user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found: " + username);
        }
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

        String token = jwtTokenProvider.generateToken(authentication);
        return ResponseEntity.ok(token);
    }
}
