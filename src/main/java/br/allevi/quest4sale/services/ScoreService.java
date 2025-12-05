package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.*;
import br.allevi.quest4sale.exceptions.ResourceNotFoundException;
import br.allevi.quest4sale.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final SaleRepository saleRepository;
    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;
    private final RankingRepository rankingRepository;

    public ScoreService(ScoreRepository scoreRepository,
                        SaleRepository saleRepository,
                        CompetitionRepository competitionRepository,
                        UserRepository userRepository,
                        RankingRepository rankingRepository) {
        this.scoreRepository = scoreRepository;
        this.saleRepository = saleRepository;
        this.competitionRepository = competitionRepository;
        this.userRepository = userRepository;
        this.rankingRepository = rankingRepository;
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
        Optional<Score> existingScore = scoreRepository.findBySaleId(saleId);
        if (existingScore.isPresent()) {
            return existingScore.get();
        }

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com ID: " + saleId));

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competição não encontrada com ID: " + competitionId));

        BigDecimal points = sale.getAmount().multiply(new BigDecimal("0.10"));

        Score score = Score.builder()
                .user(sale.getUser())
                .competition(competition)
                .sale(sale)
                .points(points)
                .build();

        Score savedScore = scoreRepository.save(score);

        recalculateRanking(competitionId);

        return savedScore;
    }

    @Transactional(readOnly = true)
    public List<Score> getUserScores(UUID userId, UUID competitionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competição não encontrada: " + competitionId));

        return scoreRepository.findByUserAndCompetition(user, competition);
    }

    @Transactional(readOnly = true)
    public Double getUserTotalScore(UUID userId, UUID competitionId) {
        List<Score> scores = getUserScores(userId, competitionId);
        return scores.stream()
                .map(Score::getPoints)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
    }

    @Transactional
    public void recalculateRanking(UUID competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competição não encontrada"));

        // 1. Buscar todos os scores dessa competição
        List<Score> allScores = scoreRepository.findAll().stream()
                .filter(s -> s.getCompetition().getId().equals(competitionId))
                .toList();

        // 2. Agrupar por usuário e somar pontos
        Map<User, BigDecimal> userTotalScores = new HashMap<>();
        for (Score s : allScores) {
            userTotalScores.merge(s.getUser(), s.getPoints(), BigDecimal::add);
        }

        // 3. Atualizar ou Criar Ranking para cada usuário
        List<Ranking> rankingsToSave = new ArrayList<>();

        for (Map.Entry<User, BigDecimal> entry : userTotalScores.entrySet()) {
            User user = entry.getKey();
            BigDecimal total = entry.getValue();

            Ranking ranking = rankingRepository.findByCompetitionIdAndUserId(competitionId, user.getId())
                    .orElse(Ranking.builder()
                            .competition(competition)
                            .user(user)
                            .rank(0)
                            .totalScore(BigDecimal.ZERO)
                            .build());

            ranking.setTotalScore(total);
            rankingsToSave.add(ranking);
        }

        // 4. Ordenar por pontuação (Maior primeiro)
        rankingsToSave.sort((r1, r2) -> r2.getTotalScore().compareTo(r1.getTotalScore()));

        // 5. Definir as posições (1º, 2º, 3º...)
        int currentRank = 1;
        for (Ranking r : rankingsToSave) {
            r.setRank(currentRank++);
        }

        rankingRepository.saveAll(rankingsToSave);
    }
}