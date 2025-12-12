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
    private final RankingRepository rankingRepository;
    private final NotificationService notificationService;
    private final RuleService ruleService;

    public ScoreService(
            ScoreRepository scoreRepository,
            SaleRepository saleRepository,
            CompetitionRepository competitionRepository,
            RankingRepository rankingRepository,
            NotificationService notificationService,
            RuleService ruleService) {
        this.scoreRepository = scoreRepository;
        this.saleRepository = saleRepository;
        this.competitionRepository = competitionRepository;
        this.rankingRepository = rankingRepository;
        this.notificationService = notificationService;
        this.ruleService = ruleService;
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

        // Busca a regra ativa para esta competição (agora retorna Rule diretamente ou lista dependendo da implementação, ajustado para lista abaixo para manter compatibilidade)
        List<Rule> rules = ruleService.findByCompetitionId(competitionId); 

        // Calcula pontos usando a lista de regras (que conterá apenas 1 regra baseada na sua estrutura atual)
        BigDecimal totalPoints = calculatePoints(sale, rules);

        log.info("Pontos calculados: {} para a venda {}", totalPoints, saleId);

        Score score = Score.builder()
                .user(sale.getUser())
                .competition(competition)
                .sale(sale)
                .points(totalPoints)
                .build();

        Score savedScore = scoreRepository.save(score);

        recalculateRanking(competitionId);

        return savedScore;
    }

    @Transactional(readOnly = true)
    public List<Score> getUserScores(UUID userId, UUID competitionId) {
        User user = new User(); user.setId(userId);
        Competition comp = new Competition(); comp.setId(competitionId);
        return scoreRepository.findByUserAndCompetition(user, comp);
    }

    @Transactional(readOnly = true)
    public BigDecimal getUserTotalScore(UUID userId, UUID competitionId) {
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

    /**
     * Calcula os pontos de uma venda com base nos pesos definidos na regra da competição.
     * A lógica foi ajustada para usar os campos reais da entidade Rule:
     * - valueWeight: Peso sobre o valor monetário da venda
     * - itemsWeight: Peso sobre a quantidade de itens
     * - positivationWeight: Pontos fixos se for positivação (cliente novo)
     * - tripWeight: Peso sobre a quantidade de viagens
     */
    private BigDecimal calculatePoints(Sale sale, List<Rule> rules) {
        BigDecimal totalPoints = BigDecimal.ZERO;

        if (rules == null || rules.isEmpty()) {
            log.warn("Nenhuma regra encontrada para calcular pontos.");
            // Poderia retornar uma pontuação padrão aqui se desejado
            return totalPoints;
        }

        // Assume-se que há apenas uma regra ativa por competição na lista,
        // mas iteramos caso a lógica mude futuramente para múltiplas regras.
        for (Rule rule : rules) {
            
            // 1. Pontos pelo Valor da Venda (Amount * Peso)
            if (rule.getValueWeight() != null && sale.getAmount() != null) {
                BigDecimal valuePoints = sale.getAmount().multiply(rule.getValueWeight());
                totalPoints = totalPoints.add(valuePoints);
            }

            // 2. Pontos pela Quantidade de Itens (Qtd * Peso)
            if (rule.getItemsWeight() != null && sale.getQuantity() != null) {
                BigDecimal itemsPoints = BigDecimal.valueOf(sale.getQuantity()).multiply(rule.getItemsWeight());
                totalPoints = totalPoints.add(itemsPoints);
            }

            // 3. Pontos por Positivação (Fixo se true)
            if (rule.getPositivationWeight() != null && Boolean.TRUE.equals(sale.getPositiveSale())) {
                totalPoints = totalPoints.add(rule.getPositivationWeight());
            }

            // 4. Pontos por Viagens (Qtd Viagens * Peso)
            if (rule.getTripWeight() != null && sale.getTravelQuantity() != null) {
                BigDecimal tripPoints = BigDecimal.valueOf(sale.getTravelQuantity()).multiply(rule.getTripWeight());
                totalPoints = totalPoints.add(tripPoints);
            }
        }

        return totalPoints;
    }
}