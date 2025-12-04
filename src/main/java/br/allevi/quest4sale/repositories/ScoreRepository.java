package br.allevi.quest4sale.repositories;

import br.allevi.quest4sale.entities.Competition;
import br.allevi.quest4sale.entities.Score;
import br.allevi.quest4sale.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ScoreRepository extends JpaRepository<Score, UUID> {
    List<Score> findByUserAndCompetition(User user, Competition competition);

    @Query("SELECT COALESCE(SUM(s.points), 0) FROM Score s WHERE s.user.id = :userId AND s.competition.id = :competitionId")
    BigDecimal sumPointsByUserIdAndCompetitionId(@Param("userId") UUID userId, @Param("competitionId") UUID competitionId);

    List<Score> findByUserIdAndCompetitionId(UUID userId, UUID competitionId);
}




