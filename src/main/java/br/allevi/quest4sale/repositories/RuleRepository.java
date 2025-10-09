package br.allevi.quest4sale.repositories;

import br.allevi.quest4sale.entities.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RuleRepository extends JpaRepository<Rule, UUID> {
    Optional<Rule> findByCompetitionId(UUID competitionId);
}