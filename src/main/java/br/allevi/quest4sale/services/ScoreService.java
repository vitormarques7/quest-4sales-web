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
}


