package org.netway.dongnehankki.user.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.netway.dongnehankki.global.exception.user.DuplicateUserNameException;
import org.netway.dongnehankki.global.exception.user.UnregisteredUserException;
import org.netway.dongnehankki.user.application.UserService;
import org.netway.dongnehankki.user.application.dto.response.UserResponse;
import org.netway.dongnehankki.user.application.dto.login.LoginRequest;
import org.netway.dongnehankki.user.application.dto.singup.CustomerSingUpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;


    @Test
    public void 일반회원_회원가입() throws Exception{
        String id = "username";
        String password = "password";
        String nickname = "nickname";

        when(userService.customerJoin(any(CustomerSingUpRequest.class))).thenReturn(mock(
            UserResponse.class));

        mockMvc.perform(post("/api/user/customer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(new CustomerSingUpRequest(id, password,nickname)))
            .with(csrf())
        ).andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    public void 회원가입시_이미_회원가입된_userName으로_회원가입을_하는경우_에러반환() throws Exception{
        String id = "username";
        String password = "password";
        String nickname = "nickname";

        when(userService.customerJoin(any(CustomerSingUpRequest.class))).thenThrow(new DuplicateUserNameException());

        mockMvc.perform(post("/api/user/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new CustomerSingUpRequest(id, password,nickname)))
                .with(csrf())
            ).andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    public void 로그인() throws Exception{
        String id = "username";
        String password = "password";

        when(userService.login(any(LoginRequest.class))).thenReturn("test_token");

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new LoginRequest(id, password)))
                .with(csrf())
            ).andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    public void 로그인시_회원가입이_안된_id를_입력할경우_에러반환() throws Exception{
        String id = "username";
        String password = "password";

        when(userService.login(any(LoginRequest.class))).thenThrow(new UnregisteredUserException());

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new LoginRequest(id, password)))
                .with(csrf())
            ).andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void 로그인시_틀린_PW를_입력할경우_에러반환() throws Exception{
        String id = "username";
        String password = "password";

        when(userService.login(any(LoginRequest.class))).thenThrow(new UnregisteredUserException());

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new LoginRequest(id, password)))
                .with(csrf())
            ).andDo(print())
            .andExpect(status().isUnauthorized());
    }



}
