package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Role;
import br.allevi.quest4sale.entities.User;
import br.allevi.quest4sale.entities.dtos.LoginRequestDTO;
import br.allevi.quest4sale.entities.dtos.LoginResponseDTO;
import br.allevi.quest4sale.entities.dtos.RegisterRequestDTO;
import br.allevi.quest4sale.exceptions.BadRequestException;
import br.allevi.quest4sale.exceptions.ResourceNotFoundException;
import br.allevi.quest4sale.exceptions.UnauthorizedException;
import br.allevi.quest4sale.repositories.RoleRepository;
import br.allevi.quest4sale.repositories.UserRepository;
import br.allevi.quest4sale.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Tentativa de login para usuário: {}", loginRequest.getUsername());

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> {
                    log.warn("Falha no login: Usuário não encontrado - {}", loginRequest.getUsername());
                    return new UnauthorizedException("Credenciais inválidas");
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Falha no login: Senha incorreta para usuário - {}", loginRequest.getUsername());
            throw new UnauthorizedException("Credenciais inválidas");
        }

        String roleName = user.getRoles().isEmpty() ? "SELLER" : user.getRoles().iterator().next().getName();
        String token = tokenProvider.generateToken(user.getId(), user.getUsername(), roleName);

        log.info("Login bem-sucedido para usuário: {} (ID: {}, Role: {})", user.getUsername(), user.getId(), roleName);

        return LoginResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(roleName)
                .build();
    }

    @Transactional
    public LoginResponseDTO register(RegisterRequestDTO registerRequest) {
        log.info("Tentativa de registro para usuário: {} (email: {})",
                 registerRequest.getUsername(), registerRequest.getEmail());

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            log.warn("Falha no registro: Username já em uso - {}", registerRequest.getUsername());
            throw new BadRequestException("Username já está em uso");
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            log.warn("Falha no registro: Email já em uso - {}", registerRequest.getEmail());
            throw new BadRequestException("Email já está em uso");
        }

        Role role;
        if (registerRequest.getRoleId() != null) {
            role = roleRepository.findById(registerRequest.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada"));
        } else {
            role = roleRepository.findByName("SELLER")
                    .orElseThrow(() -> new ResourceNotFoundException("Role SELLER não encontrada"));
        }

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .roles(roles)
                .build();

        user = userRepository.save(user);

        String token = tokenProvider.generateToken(user.getId(), user.getUsername(), role.getName());

        log.info("Registro bem-sucedido para usuário: {} (ID: {}, Role: {})",
                 user.getUsername(), user.getId(), role.getName());

        return LoginResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(role.getName())
                .build();
    }
}