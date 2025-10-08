package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Competition;
import br.allevi.quest4sale.entities.Score;
import br.allevi.quest4sale.entities.User;
import br.allevi.quest4sale.repositories.ScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;

    public ScoreService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    @Transactional
    public Score create(Score score) {
        return scoreRepository.save(score);
    }

    @Transactional(readOnly = true)
    public Score getById(UUID id) {
        return scoreRepository.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<Score> getUserScoresInCompetition(User user, Competition competition) {
        return scoreRepository.findByUserAndCompetition(user, competition);
    }

    @Transactional
    public Score calculateAndSaveScore(UUID saleId, UUID competitionId) {
        // This is a placeholder implementation
        // In a real implementation, you would:
        // 1. Get the sale by ID
        // 2. Get the competition by ID
        // 3. Calculate the score based on sale amount and competition rules
        // 4. Save the score
        
        Score score = new Score(); // You'll need to set proper values
        return scoreRepository.save(score);
    }

    @Transactional(readOnly = true)
    public List<Score> getUserScores(UUID userId, UUID competitionId) {
        // This is a placeholder implementation
        // You'll need to implement this based on your repository methods
        return List.of(); // Return empty list for now
    }

    @Transactional(readOnly = true)
    public Double getUserTotalScore(UUID userId, UUID competitionId) {
        // This is a placeholder implementation
        // You'll need to implement this based on your repository methods
        return 0.0; // Return 0 for now
    }

    @Transactional
    public void recalculateRanking(UUID competitionId) {
        // This is a placeholder implementation
        // You'll need to implement ranking calculation logic
    }
}



