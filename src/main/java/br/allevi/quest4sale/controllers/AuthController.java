package br.allevi.quest4sale.controllers;

import br.allevi.quest4sale.entities.dtos.LoginRequestDTO;
import br.allevi.quest4sale.entities.dtos.LoginResponseDTO;
import br.allevi.quest4sale.entities.dtos.RegisterRequestDTO;
import br.allevi.quest4sale.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        LoginResponseDTO response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }
}
