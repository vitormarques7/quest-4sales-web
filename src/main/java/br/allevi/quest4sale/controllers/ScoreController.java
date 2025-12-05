package br.allevi.quest4sale.controllers;

import br.allevi.quest4sale.entities.Score;
import br.allevi.quest4sale.services.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
        Score score = scoreService.calculateAndSaveScore(saleId, competitionId);
        return ResponseEntity.ok(score);
    }

    @GetMapping("/user/{userId}/competition/{competitionId}")
    public List<Score> getUserScores(
            @PathVariable UUID userId,
            @PathVariable UUID competitionId) {
        return scoreService.getUserScores(userId, competitionId);
    }

    @GetMapping("/user/{userId}/competition/{competitionId}/total")
    public ResponseEntity<BigDecimal> getUserTotalScore(
            @PathVariable UUID userId,
            @PathVariable UUID competitionId) {
        BigDecimal total = scoreService.getUserTotalScore(userId, competitionId);
        return ResponseEntity.ok(total);
    }

    @PostMapping("/competition/{competitionId}/recalculate-ranking")
    public ResponseEntity<String> recalculateRanking(@PathVariable UUID competitionId) {
        scoreService.recalculateRanking(competitionId);
        return ResponseEntity.ok("Ranking recalculado com sucesso");
    }
}