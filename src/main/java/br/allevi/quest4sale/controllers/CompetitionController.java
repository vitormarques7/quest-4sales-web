package br.allevi.quest4sale.controllers;

import br.allevi.quest4sale.entities.Competition;
import br.allevi.quest4sale.entities.Enums.CompetitionStatus;
import br.allevi.quest4sale.services.CompetitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {
    private final CompetitionService competitionService;

    @GetMapping
    public List<Competition> getAllCompetitions() {
        return competitionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Competition> getCompetitionById(@PathVariable UUID id) {
        return competitionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public List<Competition> getActiveCompetitions() {
        return competitionService.findActive();
    }

    @PostMapping
    public ResponseEntity<Competition> createCompetition(@Valid @RequestBody Competition competition) {
        try {
            Competition created = competitionService.create(competition);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<Competition> startCompetition(@PathVariable UUID id) {
        try {
            competitionService.startCompetition(id);
            return competitionService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<Competition> finishCompetition(@PathVariable UUID id) {
        try {
            competitionService.finishCompetition(id);
            return competitionService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Competition> updateCompetitionStatus(
            @PathVariable UUID id,
            @RequestParam CompetitionStatus status) {
        try {
            Competition updated = competitionService.updateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
