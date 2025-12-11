package br.allevi.quest4sale.controllers;

import br.allevi.quest4sale.entities.dtos.LoginRequestDTO;
import br.allevi.quest4sale.entities.dtos.RegisterRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterNewUser_Success() throws Exception {
        RegisterRequestDTO registerDTO = new RegisterRequestDTO();
        registerDTO.setUsername("test_user_" + System.currentTimeMillis());
        registerDTO.setEmail("test" + System.currentTimeMillis() + "@test.com");
        registerDTO.setPassword("Test@123");
        registerDTO.setFirstName("Test");
        registerDTO.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(registerDTO.getUsername()));
    }

    @Test
    void testRegisterWithInvalidEmail_Failure() throws Exception {
        RegisterRequestDTO registerDTO = new RegisterRequestDTO();
        registerDTO.setUsername("test_user");
        registerDTO.setEmail("invalid-email");
        registerDTO.setPassword("Test@123");
        registerDTO.setFirstName("Test");
        registerDTO.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginWithInvalidCredentials_Failure() throws Exception {
        LoginRequestDTO loginDTO = new LoginRequestDTO();
        loginDTO.setUsername("nonexistent_user");
        loginDTO.setPassword("WrongPassword123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }
}
