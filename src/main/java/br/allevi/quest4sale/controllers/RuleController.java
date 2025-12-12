package br.allevi.quest4sale.controllers;

import br.allevi.quest4sale.entities.Rule;
import br.allevi.quest4sale.entities.dtos.RuleDTO;
import br.allevi.quest4sale.services.RuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping("/competition/{competitionId}")
    public ResponseEntity<Rule> getRuleByCompetition(@PathVariable UUID competitionId) {
        Rule rule = ruleService.getByCompetitionId(competitionId);
        return ResponseEntity.ok(rule);
    }

    @PutMapping("/competition/{competitionId}")
    public ResponseEntity<Rule> createOrUpdateRule(
            @PathVariable UUID competitionId,
            @Valid @RequestBody RuleDTO ruleDTO) {

        Rule rule = Rule.builder()
                .valueWeight(ruleDTO.getValueWeight())
                .itemsWeight(ruleDTO.getItemsWeight())
                .positivationWeight(ruleDTO.getPositivationWeight())
                .tripWeight(ruleDTO.getTripWeight())
                .build();

        Rule saved = ruleService.createOrUpdate(competitionId, rule);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/competition/{competitionId}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID competitionId) {
        ruleService.delete(competitionId);
        return ResponseEntity.noContent().build();
    }
}
