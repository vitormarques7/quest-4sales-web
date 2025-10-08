package br.allevi.quest4sale.controllers;

import br.allevi.quest4sale.entities.Score;
import br.allevi.quest4sale.services.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping("/calculate")
    public ResponseEntity<Score> calculateScore(
            @RequestParam UUID saleId,
            @RequestParam UUID competitionId) {
        try {
            Score score = scoreService.calculateAndSaveScore(saleId, competitionId);
            return ResponseEntity.ok(score);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}/competition/{competitionId}")
    public List<Score> getUserScores(
            @PathVariable UUID userId,
            @PathVariable UUID competitionId) {
        return scoreService.getUserScores(userId, competitionId);
    }

    @GetMapping("/user/{userId}/competition/{competitionId}/total")
    public ResponseEntity<Double> getUserTotalScore(
            @PathVariable UUID userId,
            @PathVariable UUID competitionId) {
        try {
            Double total = scoreService.getUserTotalScore(userId, competitionId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/competition/{competitionId}/recalculate-ranking")
    public ResponseEntity<String> recalculateRanking(@PathVariable UUID competitionId) {
        try {
            scoreService.recalculateRanking(competitionId);
            return ResponseEntity.ok("Ranking recalculado com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao recalcular ranking");
        }
    }
}