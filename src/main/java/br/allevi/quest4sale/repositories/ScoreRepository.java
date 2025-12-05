package br.allevi.quest4sale.repositories;

import br.allevi.quest4sale.entities.Competition;
import br.allevi.quest4sale.entities.Score;
import br.allevi.quest4sale.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScoreRepository extends JpaRepository<Score, UUID> {

    // Método usado para listar pontos do usuário
    List<Score> findByUserAndCompetition(User user, Competition competition);

    // --- O MÉTODO QUE FALTA ---
    // Essencial para o ScoreService verificar se a venda já foi pontuada
    Optional<Score> findBySaleId(UUID saleId);
}