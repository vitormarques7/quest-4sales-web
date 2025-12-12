package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Competition;
import br.allevi.quest4sale.entities.Rule;
import br.allevi.quest4sale.exceptions.ResourceNotFoundException;
import br.allevi.quest4sale.repositories.CompetitionRepository;
import br.allevi.quest4sale.repositories.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;
    private final CompetitionRepository competitionRepository;

    @Transactional(readOnly = true)
    public Rule getByCompetitionId(UUID competitionId) {
        return ruleRepository.findByCompetitionId(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Regra não encontrada para a competição ID: " + competitionId));
    }

    // Adicionado método para compatibilidade com a chamada no ScoreService que espera uma lista
    @Transactional(readOnly = true)
    public List<Rule> findByCompetitionId(UUID competitionId) {
        return ruleRepository.findByCompetitionId(competitionId)
                .map(List::of) // Se encontrar, retorna uma lista com a regra única
                .orElseGet(List::of); // Se não encontrar, retorna lista vazia
    }

    @Transactional
    public Rule createOrUpdate(UUID competitionId, Rule rule) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competição não encontrada com ID: " + competitionId));

        return ruleRepository.findByCompetitionId(competitionId)
                .map(existingRule -> {
                    existingRule.setValueWeight(rule.getValueWeight());
                    existingRule.setItemsWeight(rule.getItemsWeight());
                    existingRule.setPositivationWeight(rule.getPositivationWeight());
                    existingRule.setTripWeight(rule.getTripWeight());
                    log.info("Regra atualizada para competição ID: {}", competitionId);
                    return ruleRepository.save(existingRule);
                })
                .orElseGet(() -> {
                    rule.setCompetition(competition);
                    log.info("Regra criada para competição ID: {}", competitionId);
                    return ruleRepository.save(rule);
                });
    }

    @Transactional
    public void delete(UUID competitionId) {
        Rule rule = getByCompetitionId(competitionId);
        ruleRepository.delete(rule);
        log.info("Regra deletada para competição ID: {}", competitionId);
    }

    /**
     * Retorna regra padrão caso não exista uma configurada
     */
    @Transactional(readOnly = true)
    public Rule getOrDefault(UUID competitionId) {
        return ruleRepository.findByCompetitionId(competitionId)
                .orElseGet(() -> {
                    log.warn("Nenhuma regra encontrada para competição ID: {}. Usando regra padrão.", competitionId);
                    return Rule.builder()
                            .valueWeight(new BigDecimal("1.0"))
                            .itemsWeight(new BigDecimal("1.0"))
                            .positivationWeight(new BigDecimal("1.0"))
                            .tripWeight(new BigDecimal("1.0"))
                            .build();
                });
    }
}