package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.*;
import br.allevi.quest4sale.exceptions.ResourceNotFoundException;
import br.allevi.quest4sale.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final SaleRepository saleRepository;
    private final CompetitionRepository competitionRepository;
    // private final RuleRepository ruleRepository;
    private final RankingRepository rankingRepository;
    private final NotificationService notificationService;

    public ScoreService(
            ScoreRepository scoreRepository,
            SaleRepository saleRepository,
            CompetitionRepository competitionRepository,
            // RuleRepository ruleRepository,
            RankingRepository rankingRepository,
            NotificationService notificationService) {
        this.scoreRepository = scoreRepository;
        this.saleRepository = saleRepository;
        this.competitionRepository = competitionRepository;
        // this.ruleRepository = ruleRepository;
        this.rankingRepository = rankingRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Score create(Score score) {
        return scoreRepository.save(score);
    }

    @Transactional(readOnly = true)
    public Score getById(UUID id) {
        return scoreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Score não encontrado com ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Score> getUserScoresInCompetition(User user, Competition competition) {
        return scoreRepository.findByUserAndCompetition(user, competition);
    }

    @Transactional
    public Score calculateAndSaveScore(UUID saleId, UUID competitionId) {
        log.info("Calculando pontuação para venda ID: {} na competição ID: {}", saleId, competitionId);


        Optional<Score> existingScore = scoreRepository.findBySaleId(saleId);
        if (existingScore.isPresent()) {
            return existingScore.get();
        }

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com ID: " + saleId));

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competição não encontrada com ID: " + competitionId));

        BigDecimal totalPoints = sale.getAmount().multiply(new BigDecimal("0.10"));

        log.info("Pontos calculados (Regra 10%): {}", totalPoints);

        Score score = Score.builder()
                .user(sale.getUser())
                .competition(competition)
                .sale(sale)
                .points(totalPoints)
                .build();

        Score savedScore = scoreRepository.save(score);

        // 6. Recalcular ranking (Versão Remota com Notificações)
        recalculateRanking(competitionId);

        return savedScore;
    }

    @Transactional(readOnly = true)
    public List<Score> getUserScores(UUID userId, UUID competitionId) {
        // Adaptado para garantir compatibilidade
        User user = new User(); user.setId(userId);
        Competition comp = new Competition(); comp.setId(competitionId);
        return scoreRepository.findByUserAndCompetition(user, comp);
    }

    @Transactional(readOnly = true)
    public BigDecimal getUserTotalScore(UUID userId, UUID competitionId) {
        // Cálculo simples via stream
        List<Score> scores = getUserScores(userId, competitionId);
        return scores.stream()
                .map(Score::getPoints)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void recalculateRanking(UUID competitionId) {
        log.info("Recalculando ranking para competição ID: {}", competitionId);

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competição não encontrada com ID: " + competitionId));

        // Buscar todos os scores e somar
        Map<UUID, BigDecimal> userTotalScores = new HashMap<>();
        scoreRepository.findAll().stream()
                .filter(score -> score.getCompetition().getId().equals(competitionId))
                .forEach(score -> {
                    UUID userId = score.getUser().getId();
                    userTotalScores.merge(userId, score.getPoints(), BigDecimal::add);
                });

        if (userTotalScores.isEmpty()) return;

        List<Map.Entry<UUID, BigDecimal>> sortedScores = new ArrayList<>(userTotalScores.entrySet());
        sortedScores.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        Map<UUID, Integer> oldRanks = new HashMap<>();
        rankingRepository.findByCompetitionIdOrderByRankAsc(competitionId)
                .forEach(r -> oldRanks.put(r.getUser().getId(), r.getRank()));

        AtomicInteger rank = new AtomicInteger(1);

        sortedScores.forEach(entry -> {
            UUID userId = entry.getKey();
            BigDecimal totalScore = entry.getValue();
            int currentRank = rank.getAndIncrement();

            Optional<Ranking> existingRanking = rankingRepository.findByCompetitionIdAndUserId(competitionId, userId);

            if (existingRanking.isPresent()) {
                Ranking ranking = existingRanking.get();
                Integer oldRank = ranking.getRank();

                ranking.setRank(currentRank);
                ranking.setTotalScore(totalScore);
                rankingRepository.save(ranking);

                checkAndNotifyRankChange(userId, competitionId, oldRank, currentRank);
            } else {
                Ranking newRanking = Ranking.builder()
                        .competition(competition)
                        .user(User.builder().id(userId).build())
                        .rank(currentRank)
                        .totalScore(totalScore)
                        .build();
                rankingRepository.save(newRanking);

                notificationService.notifyRankingEntry(userId, competitionId, currentRank);
            }
        });
    }

    private void checkAndNotifyRankChange(UUID userId, UUID competitionId, Integer oldRank, Integer newRank) {
        if (oldRank == null || oldRank.equals(newRank)) return;

        if (newRank < oldRank) {
            notificationService.notifyRankImprovement(userId, competitionId, oldRank, newRank);
        } else if (newRank > oldRank) {
            notificationService.notifyRankDrop(userId, competitionId, oldRank, newRank);
        }
    }
}