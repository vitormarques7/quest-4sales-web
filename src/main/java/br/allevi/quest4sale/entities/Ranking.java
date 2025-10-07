package br.allevi.quest4sale.entities;

import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"user", "competition"})
@EqualsAndHashCode(of = "id")
public class Ranking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    @NotNull
    private Competition competition;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;
    @Column(nullable = false)
    private Integer rank;
    @Column(name = "total_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalScore;
    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void atualizarScore(BigDecimal newScore) {
        if (newScore != null && newScore.compareTo(BigDecimal.ZERO) >= 0) {
            this.totalScore = newScore;
        }
    }
}
