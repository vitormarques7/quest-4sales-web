package br.allevi.quest4sale.repositories;

import br.allevi.quest4sale.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Page<User> findByActiveTrue(Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.active = true AND (r.name = 'SELLER' OR r.name = 'ROLE_SELLER')")
    Page<User> findActiveSellers(Pageable pageable);
}



