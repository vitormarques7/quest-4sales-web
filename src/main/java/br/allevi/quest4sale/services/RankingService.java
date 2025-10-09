package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Competition;
import br.allevi.quest4sale.entities.Ranking;
import br.allevi.quest4sale.entities.User;
import br.allevi.quest4sale.entities.Score;
import br.allevi.quest4sale.repositories.CompetitionRepository;
import br.allevi.quest4sale.repositories.RankingRepository;
import br.allevi.quest4sale.repositories.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;
    private final ScoreRepository scoreRepository;
    private final CompetitionRepository competitionRepository;

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
        return rankingRepository.findByCompetitionIdAndUserId(competitionId, userId)
                .map(Ranking::getRank)
                .orElse(-1);
    }

    @Transactional
    public void recalculateRanking(UUID competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
            .orElseThrow(() -> new RuntimeException("Competition not found"));

        Map<User, BigDecimal> userTotalScores = scoreRepository.findByCompetition(competition)
            .stream()
            .collect(Collectors.groupingBy(
                Score::getUser,
                Collectors.mapping(Score::getPoints, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));

        List<Ranking> updatedRankings = userTotalScores.entrySet().stream()
            .map(entry -> {
                User user = entry.getKey();
                BigDecimal totalScore = entry.getValue();

                Ranking ranking = rankingRepository.findByCompetitionIdAndUserId(competitionId, user.getId())
                        .orElse(new Ranking());

                ranking.setCompetition(competition);
                ranking.setUser(user);
                ranking.setTotalScore(totalScore);
                return ranking;
            })
            .sorted(Comparator.comparing(Ranking::getTotalScore).reversed())
            .toList();

        AtomicInteger rankPosition = new AtomicInteger(1);
        updatedRankings.forEach(ranking -> ranking.setRank(rankPosition.getAndIncrement()));

        rankingRepository.saveAll(updatedRankings);
    }
}
