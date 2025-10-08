package br.allevi.quest4sale.repositories;

import br.allevi.quest4sale.entities.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RankingRepository extends JpaRepository<Ranking, UUID> {
    
    List<Ranking> findByCompetitionIdOrderByRankAsc(UUID competitionId);
    
    Optional<Ranking> findByCompetitionIdAndUserId(UUID competitionId, UUID userId);
    
    List<Ranking> findByCompetitionIdAndRankBetween(UUID competitionId, Integer startRank, Integer endRank);
}
