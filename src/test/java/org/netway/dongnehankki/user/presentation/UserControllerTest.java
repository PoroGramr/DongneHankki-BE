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
import org.netway.dongnehankki.user.exception.DuplicateUserNameException;
import org.netway.dongnehankki.user.exception.InvalidPasswordException;
import org.netway.dongnehankki.user.exception.UnregisteredUserException;
import org.netway.dongnehankki.user.application.UserService;
import org.netway.dongnehankki.user.dto.login.LoginResponse;
import org.netway.dongnehankki.user.dto.response.UserResponse;
import org.netway.dongnehankki.user.dto.login.LoginRequest;
import org.netway.dongnehankki.user.dto.signUp.CustomerSignUpRequest;
import org.netway.dongnehankki.user.dto.signUp.OwnerSignUpRequest;
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

        when(userService.customerJoin(any(CustomerSignUpRequest.class))).thenReturn(mock(
            UserResponse.class));

        mockMvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(new CustomerSignUpRequest(id, password,nickname)))
            .with(csrf())
        ).andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    public void 사장회원_회원가입() throws Exception{
        String id = "username";
        String password = "password";
        String nickname = "nickname";
        Long storeId = 1L;

        mockMvc.perform(post("/api/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new OwnerSignUpRequest(id,password,nickname,storeId)))
                .with(csrf())
            ).andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    public void 회원가입시_이미_회원가입된_userName으로_회원가입을_하는경우_에러반환() throws Exception{
        String id = "username";
        String password = "password";
        String nickname = "nickname";

        when(userService.customerJoin(any(CustomerSignUpRequest.class))).thenThrow(new DuplicateUserNameException());

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new CustomerSignUpRequest(id, password,nickname)))
                .with(csrf())
            ).andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    public void 로그인() throws Exception{
        String id = "username";
        String password = "password";

        when(userService.login(any(LoginRequest.class))).thenReturn(mock(LoginResponse.class));

        mockMvc.perform(post("/api/login")
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

        mockMvc.perform(post("/api/login")
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

        when(userService.login(any(LoginRequest.class))).thenThrow(new InvalidPasswordException());

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new LoginRequest(id, password)))
                .with(csrf())
            ).andDo(print())
            .andExpect(status().isUnauthorized());
    }



}
