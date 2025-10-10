package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Ranking;
import br.allevi.quest4sale.repositories.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    @Transactional(readOnly = true)
    public List<Ranking> getCompetitionRanking(UUID competitionId) {
        return rankingRepository.findByCompetitionIdOrderByRankAsc(competitionId);
    }

    @Transactional(readOnly = true)
    public List<Ranking> getTopN(UUID competitionId, int limit) {
        return rankingRepository.findByCompetitionIdOrderByRankAsc(competitionId)
                .stream()
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Ranking> getUserRanking(UUID competitionId, UUID userId) {
        return rankingRepository.findByCompetitionIdAndUserId(competitionId, userId);
    }

    @Transactional(readOnly = true)
    public Integer getUserPosition(UUID competitionId, UUID userId) {
        Optional<Ranking> ranking = rankingRepository.findByCompetitionIdAndUserId(competitionId, userId);
        return ranking.map(Ranking::getRank).orElse(-1);
    }
}

