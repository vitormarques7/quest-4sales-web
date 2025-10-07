package br.allevi.quest4sale.repositories;

import br.allevi.quest4sale.entities.Competition;
import br.allevi.quest4sale.entities.Score;
import br.allevi.quest4sale.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScoreRepository extends JpaRepository<Score, UUID> {
    List<Score> findByUserAndCompetition(User user, Competition competition);
}


