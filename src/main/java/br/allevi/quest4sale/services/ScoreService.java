package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.*;
import br.allevi.quest4sale.exceptions.BusinessException;
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
    private final RuleRepository ruleRepository;
    private final RankingRepository rankingRepository;
    private final NotificationService notificationService;

    public ScoreService(
            ScoreRepository scoreRepository,
            SaleRepository saleRepository,
            CompetitionRepository competitionRepository,
            RuleRepository ruleRepository,
            RankingRepository rankingRepository,
            NotificationService notificationService) {
        this.scoreRepository = scoreRepository;
        this.saleRepository = saleRepository;
        this.competitionRepository = competitionRepository;
        this.ruleRepository = ruleRepository;
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

        // 1. Buscar Sale
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com ID: " + saleId));

        // 2. Buscar Competition
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competição não encontrada com ID: " + competitionId));

        // 3. Buscar Rule da Competition
        Rule rule = ruleRepository.findByCompetitionId(competitionId)
                .orElseThrow(() -> new BusinessException("Competição não possui regra de pontuação configurada. ID: " + competitionId));

        // 4. Calcular pontos usando a fórmula:
        // pontos = (amount × valueWeight) + (quantity × itemsWeight) + (positiveSale ? positivationWeight : 0) + (travelQuantity × tripWeight)

        BigDecimal amountPoints = sale.getAmount().multiply(rule.getValueWeight());
        BigDecimal quantityPoints = BigDecimal.valueOf(sale.getQuantity()).multiply(rule.getItemsWeight());
        BigDecimal positivationPoints = Boolean.TRUE.equals(sale.getPositiveSale())
                ? rule.getPositivationWeight()
                : BigDecimal.ZERO;
        BigDecimal travelPoints = BigDecimal.valueOf(sale.getTravelQuantity()).multiply(rule.getTripWeight());

        BigDecimal totalPoints = amountPoints
                .add(quantityPoints)
                .add(positivationPoints)
                .add(travelPoints);

        log.info("Pontos calculados: amount={}, quantity={}, positivation={}, travel={}, TOTAL={}",
                amountPoints, quantityPoints, positivationPoints, travelPoints, totalPoints);

        // 5. Criar Score
        Score score = Score.builder()
                .user(sale.getUser())
                .competition(competition)
                .sale(sale)
                .points(totalPoints)
                .build();

        // 6. Salvar
        Score savedScore = scoreRepository.save(score);
        log.info("Score salvo com sucesso. ID: {}, Pontos: {}", savedScore.getId(), savedScore.getPoints());

        // 7. Recalcular ranking
        recalculateRanking(competitionId);

        return savedScore;
    }

    @Transactional(readOnly = true)
    public List<Score> getUserScores(UUID userId, UUID competitionId) {
        return scoreRepository.findByUserIdAndCompetitionId(userId, competitionId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getUserTotalScore(UUID userId, UUID competitionId) {
        return scoreRepository.sumPointsByUserIdAndCompetitionId(userId, competitionId);
    }

    @Transactional
    public void recalculateRanking(UUID competitionId) {
        log.info("Recalculando ranking para competição ID: {}", competitionId);

        // 1. Buscar Competition
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competição não encontrada com ID: " + competitionId));

        // 2. Buscar todos os scores da competição agrupados por usuário
        Map<UUID, BigDecimal> userTotalScores = new HashMap<>();

        scoreRepository.findAll().stream()
                .filter(score -> score.getCompetition().getId().equals(competitionId))
                .forEach(score -> {
                    UUID userId = score.getUser().getId();
                    userTotalScores.merge(userId, score.getPoints(), BigDecimal::add);
                });

        if (userTotalScores.isEmpty()) {
            log.info("Nenhum score encontrado para a competição. ID: {}", competitionId);
            return;
        }

        // 3. Ordenar por pontos (decrescente)
        List<Map.Entry<UUID, BigDecimal>> sortedScores = new ArrayList<>(userTotalScores.entrySet());
        sortedScores.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // 4. Atualizar ou criar rankings
        AtomicInteger rank = new AtomicInteger(1);
        Map<UUID, Integer> oldRanks = new HashMap<>();

        // Primeiro, obter ranks antigos
        rankingRepository.findByCompetitionIdOrderByRankAsc(competitionId)
                .forEach(ranking -> oldRanks.put(ranking.getUser().getId(), ranking.getRank()));

        sortedScores.forEach(entry -> {
            UUID userId = entry.getKey();
            BigDecimal totalScore = entry.getValue();
            int currentRank = rank.getAndIncrement();

            Optional<Ranking> existingRanking = rankingRepository.findByCompetitionIdAndUserId(competitionId, userId);

            if (existingRanking.isPresent()) {
                // Atualizar ranking existente
                Ranking ranking = existingRanking.get();
                Integer oldRank = ranking.getRank();
                ranking.setRank(currentRank);
                ranking.atualizarScore(totalScore);
                rankingRepository.save(ranking);

                log.info("Ranking atualizado: User ID={}, Rank={} (anterior: {}), Score={}",
                        userId, currentRank, oldRank, totalScore);

                // Verificar se houve mudança de posição e notificar
                checkAndNotifyRankChange(userId, competitionId, oldRank, currentRank);
            } else {
                // Criar novo ranking
                User user = new User();
                user.setId(userId);

                Ranking newRanking = Ranking.builder()
                        .competition(competition)
                        .user(user)
                        .rank(currentRank)
                        .totalScore(totalScore)
                        .build();

                rankingRepository.save(newRanking);

                log.info("Novo ranking criado: User ID={}, Rank={}, Score={}",
                        userId, currentRank, totalScore);

                // Notificar entrada no ranking
                notificationService.notifyRankingEntry(userId, competitionId, currentRank);
            }
        });

        log.info("Ranking recalculado com sucesso. Total de participantes: {}", sortedScores.size());
    }

    private void checkAndNotifyRankChange(UUID userId, UUID competitionId, Integer oldRank, Integer newRank) {
        if (oldRank == null || oldRank.equals(newRank)) {
            return;
        }

        if (newRank < oldRank) {
            // Subiu no ranking
            notificationService.notifyRankImprovement(userId, competitionId, oldRank, newRank);
        } else if (newRank > oldRank) {
            // Caiu no ranking
            notificationService.notifyRankDrop(userId, competitionId, oldRank, newRank);
        }
    }
}
