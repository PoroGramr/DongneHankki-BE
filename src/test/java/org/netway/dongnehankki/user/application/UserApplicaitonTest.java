package org.netway.dongnehankki.user.application;

import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class UserApplicaitonTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void 회원가입(){
        String username = "username";
        String password = "password";

        mockMvc.perform(post("/api/user/create")
            .contentType(MediaType.APPLICATION_JSON)
            // TODO : add request body
            .content()
        ).andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    public void 회원가입시_이미_회원가입된_userName으로_회원가입을_하는경우_에러반환(){
        String username = "username";
        String password = "password";

        // TODO : mocking

        mockMvc.perform(post("/api/user/create")
                .contentType(MediaType.APPLICATION_JSON)
                // TODO : add request body
                .content()
            ).andDo(print())
            .andExpect(status().isConflict());
    }



}
