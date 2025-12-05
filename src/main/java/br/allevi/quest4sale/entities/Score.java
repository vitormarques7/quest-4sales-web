package br.allevi.quest4sale.entities;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"user", "competition", "sale"})
@EqualsAndHashCode(of = "id")
@Table(name = "scores", indexes = {
        @Index(name = "idx_score_user", columnList = "user_id"),
        @Index(name = "idx_score_competition", columnList = "competition_id"),
        @Index(name = "idx_score_user_competition", columnList = "user_id, competition_id")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    // Aqui: Não mostre listas, senha ou roles do usuário dentro do Score (já sabemos quem ele é)
    @JsonIgnoreProperties({"sales", "scores", "rankings", "notifications", "roles", "password", "hibernateLazyInitializer", "handler"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    @NotNull
    // Aqui: Não mostre listas da competição se houver
    @JsonIgnoreProperties({"scores", "rankings", "hibernateLazyInitializer", "handler"})
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    // Aqui é o PULO DO GATO: Mostre a venda, mas NÃO mostre o usuário da venda de novo!
    @JsonIgnoreProperties({"user", "scores", "hibernateLazyInitializer", "handler"})
    private Sale sale;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    private BigDecimal points;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}