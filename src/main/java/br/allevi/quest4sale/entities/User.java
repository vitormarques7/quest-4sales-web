package br.allevi.quest4sale.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(name = "avatar_url")
    private String avatarUrl;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_papel",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "papel_id")
    )
    private Set<Role> roles = new Set<Role>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Sale> sales = new Set<Sale>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Score> Scores = new Set<Score>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Ranking> rankings = new Set<Ranking>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Notification> notifications = new Set<Notification>();
}
