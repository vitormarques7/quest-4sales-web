package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Role;
import br.allevi.quest4sale.entities.User;
import br.allevi.quest4sale.entities.dtos.CreateUserDTO;
import br.allevi.quest4sale.entities.dtos.UpdateUserDTO;
import br.allevi.quest4sale.exceptions.BadRequestException;
import br.allevi.quest4sale.exceptions.ConflictException;
import br.allevi.quest4sale.exceptions.ResourceNotFoundException;
import br.allevi.quest4sale.repositories.RoleRepository;
import br.allevi.quest4sale.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private Role findRoleByName(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            roleName = "SELLER";
        }

        String input = roleName.trim().toUpperCase();

        Optional<Role> role = roleRepository.findByName(input);
        if (role.isPresent()) {
            log.debug("Role encontrada com nome exato: {}", input);
            return role.get();
        }

        String withPrefix = input.startsWith("ROLE_") ? input : "ROLE_" + input;
        role = roleRepository.findByName(withPrefix);
        if (role.isPresent()) {
            log.debug("Role encontrada com prefixo: {}", withPrefix);
            return role.get();
        }

        String withoutPrefix = input.startsWith("ROLE_") ? input.substring(5) : input;
        role = roleRepository.findByName(withoutPrefix);
        if (role.isPresent()) {
            log.debug("Role encontrada sem prefixo: {}", withoutPrefix);
            return role.get();
        }

        throw new ResourceNotFoundException(
                String.format("Role não encontrada: '%s'. Tentativas: '%s', '%s', '%s'",
                        roleName, input, withPrefix, withoutPrefix)
        );
    }

    public User create(CreateUserDTO createUserDTO) {
        log.info("Criando usuário: {}", createUserDTO.getEmail());

        if (userRepository.existsByEmail(createUserDTO.getEmail())) {
            throw new ConflictException("Email já cadastrado: " + createUserDTO.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(createUserDTO.getPassword());

        Role role = findRoleByName(createUserDTO.getRole());

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .username(createUserDTO.getUsername())
                .email(createUserDTO.getEmail())
                .password(hashedPassword)
                .firstName(createUserDTO.getFirstName())
                .lastName(createUserDTO.getLastName())
                .avatarUrl(createUserDTO.getAvatarUrl())
                .roles(roles)
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
        return userRepository.findByActiveTrue(pageable);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public User update(UUID id, UpdateUserDTO updateUserDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));

        if (updateUserDTO.getEmail() != null && !updateUserDTO.getEmail().trim().isEmpty()) {
            if (!existingUser.getEmail().equals(updateUserDTO.getEmail()) &&
                    userRepository.existsByEmail(updateUserDTO.getEmail())) {
                throw new ConflictException("Email já em uso: " + updateUserDTO.getEmail());
            }
            existingUser.setEmail(updateUserDTO.getEmail());
        }

        if (updateUserDTO.getUsername() != null && !updateUserDTO.getUsername().trim().isEmpty()) {
            existingUser.setUsername(updateUserDTO.getUsername());
        }

        if (updateUserDTO.getFirstName() != null && !updateUserDTO.getFirstName().trim().isEmpty()) {
            existingUser.setFirstName(updateUserDTO.getFirstName());
        }

        if (updateUserDTO.getLastName() != null && !updateUserDTO.getLastName().trim().isEmpty()) {
            existingUser.setLastName(updateUserDTO.getLastName());
        }

        if (updateUserDTO.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(updateUserDTO.getAvatarUrl());
        }

        if (updateUserDTO.getRole() != null && !updateUserDTO.getRole().trim().isEmpty()) {
            Role role = findRoleByName(updateUserDTO.getRole());
            Set<Role> updatedRoles = new HashSet<>();
            updatedRoles.add(role);
            existingUser.setRoles(updatedRoles);
        }

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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));

        user.setActive(false);
        userRepository.save(user);

        log.info("Usuário desativado (soft delete): {}", user.getEmail());
    }

    public void hardDelete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado com ID: " + id);
        }

        try {
            userRepository.deleteById(id);
            log.info("Usuário deletado permanentemente (hard delete): {}", id);
        } catch (Exception e) {
            log.error("Erro ao deletar usuário: {}", e.getMessage());
            throw new BadRequestException(
                    "Não é possível deletar este usuário porque ele possui vendas, pontuações ou outros registros associados. " +
                    "O usuário foi desativado para preservar o histórico."
            );
        }
    }

    public Page<User> findSellers(Pageable pageable) {
        return userRepository.findByActiveTrue(pageable);
    }
}
