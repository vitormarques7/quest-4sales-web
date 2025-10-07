package br.allevi.quest4sale.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Ranking {
    private UUID id;
    private Competition competition;
    private User user;
    private Integer rank;
    private BigDecimal totalScore;
    private LocalDateTime lastUpdate;
    private LocalDateTime createdAt;

    public void atualizarScore(BigDecimal newScore) {
        if (newScore != null && newScore.compareTo(BigDecimal.ZERO) >= 0) {
            this.totalScore = newScore;
        }
    }
}
