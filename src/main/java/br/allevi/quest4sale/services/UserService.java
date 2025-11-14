package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.User;
import br.allevi.quest4sale.entities.dtos.CreateUserDTO;
import br.allevi.quest4sale.exceptions.ConflictException;
import br.allevi.quest4sale.exceptions.ResourceNotFoundException;
import br.allevi.quest4sale.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User create(CreateUserDTO createUserDTO) {
        log.info("Criando usuário: {}", createUserDTO.getEmail());

        if (userRepository.existsByEmail(createUserDTO.getEmail())) {
            throw new ConflictException("Email já cadastrado: " + createUserDTO.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(createUserDTO.getPassword());

        User user = User.builder()
                .username(createUserDTO.getUsername())
                .email(createUserDTO.getEmail())
                .password(hashedPassword)
                .avatarUrl(createUserDTO.getAvatarUrl())
                .build();

        return userRepository.save(user);
    }

    public User create(User user) {
        log.info("Criando usuário: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Email já cadastrado: " + user.getEmail());
        }

        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public User update(UUID id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));

        if (!existingUser.getEmail().equals(userDetails.getEmail()) &&
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new ConflictException("Email já em uso: " + userDetails.getEmail());
        }

        existingUser.setUsername(userDetails.getUsername());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setAvatarUrl(userDetails.getAvatarUrl());

        return userRepository.save(existingUser);
    }

    public void updatePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ConflictException("Senha atual incorreta");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Senha atualizada para usuário: {}", user.getEmail());
    }

    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado com ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public Page<User> findSellers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
