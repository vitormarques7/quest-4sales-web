package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Ranking;
import br.allevi.quest4sale.repositories.RankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    @Transactional(readOnly = true)
    public List<Ranking> getCompetitionRanking(UUID competitionId) {
        return rankingRepository.findByCompetitionIdOrderByTotalScoreDesc(competitionId);
    }

    @Transactional(readOnly = true)
    public List<Ranking> getTopN(UUID competitionId, int limit) {
        return rankingRepository.findByCompetitionIdOrderByTotalScoreDesc(competitionId)
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
        List<Ranking> rankings = rankingRepository.findByCompetitionIdOrderByTotalScoreDesc(competitionId);

        for (int i = 0; i < rankings.size(); i++) {
            if (rankings.get(i).getUser().getId().equals(userId)) {
                return i + 1; 
            }
        }

        return -1; 
    }

 
    @Transactional
    public void recalculateRankings(UUID competitionId) {
        log.info("Recalculando rankings para competição ID={}", competitionId);

        List<Ranking> rankings = rankingRepository.findByCompetitionIdOrderByTotalScoreDesc(competitionId);

        int position = 1;
        for (Ranking ranking : rankings) {
            ranking.setRank(position);
            position++;
        }

        rankingRepository.saveAll(rankings);

        log.info("Rankings recalculados com sucesso. Total de participantes: {}", rankings.size());
    }
}

