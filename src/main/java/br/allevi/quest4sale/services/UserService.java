package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.User;
import br.allevi.quest4sale.entities.dtos.CreateUserDTO;
import br.allevi.quest4sale.exceptions.ConflictException;
import br.allevi.quest4sale.exceptions.ResourceNotFoundException;
import br.allevi.quest4sale.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public User create(CreateUserDTO createUserDTO) {
        log.info("Criando usuário: {}", createUserDTO.getEmail());

        if (userRepository.existsByEmail(createUserDTO.getEmail())) {
            throw new ConflictException("Email já cadastrado: " + createUserDTO.getEmail());
        }

        User user = User.builder()
                .username(createUserDTO.getUsername())
                .email(createUserDTO.getEmail())
                .password(createUserDTO.getPassword()) // Note: should be hashed in real implementation
                .avatarUrl(createUserDTO.getAvatarUrl())
                .build();

        return userRepository.save(user);
    }

    public User create(User user) {
        log.info("Criando usuário: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Email já cadastrado: " + user.getEmail());
        }

        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
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

    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado com ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public List<User> findSellers() {
        return userRepository.findAll();
    }
}
