package br.allevi.quest4sale.controllers;

import br.allevi.quest4sale.entities.Ranking;
import br.allevi.quest4sale.exceptions.ResourceNotFoundException;
import br.allevi.quest4sale.services.RankingService;
import br.allevi.quest4sale.services.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;
    private final ScoreService scoreService;

    @GetMapping("/competition/{competitionId}")
    public List<Ranking> getCompetitionRanking(@PathVariable UUID competitionId) {
        return rankingService.getCompetitionRanking(competitionId);
    }

    @GetMapping("/competition/{competitionId}/top")
    public List<Ranking> getTopRankings(
            @PathVariable UUID competitionId,
            @RequestParam(defaultValue = "10") int limit) {
        return rankingService.getTopN(competitionId, limit);
    }

    @GetMapping("/competition/{competitionId}/user/{userId}")
    public Ranking getUserRanking(
            @PathVariable UUID competitionId,
            @PathVariable UUID userId) {
        return rankingService.getUserRanking(competitionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Ranking não encontrado para este usuário."));
    }

    @GetMapping("/competition/{competitionId}/user/{userId}/position")
    public Integer getUserPosition(
            @PathVariable UUID competitionId,
            @PathVariable UUID userId) {
        Integer position = rankingService.getUserPosition(competitionId, userId);
        if (position == -1) {
            throw new ResourceNotFoundException("Posição do usuário não encontrada.");
        }
        return position;
    }

    @GetMapping("/competition/{competitionId}/around-user/{userId}")
    public List<Ranking> getRankingsAroundUser(
            @PathVariable UUID competitionId,
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "2") int range) {

        Integer userPosition = rankingService.getUserPosition(competitionId, userId);
        if (userPosition == -1) {
            throw new ResourceNotFoundException("Usuário não encontrado no ranking.");
        }

        int start = Math.max(1, userPosition - range);
        int end = userPosition + range;

        List<Ranking> rankings = rankingService.getCompetitionRanking(competitionId);

        return rankings.stream()
                .filter(r -> r.getRank() >= start && r.getRank() <= end)
                .toList();
    }

    @PostMapping("/competition/{competitionId}/recalculate")
    public ResponseEntity<Void> recalculateRanking(@PathVariable UUID competitionId) {
        scoreService.recalculateRanking(competitionId);
        return ResponseEntity.ok().build();
    }
}
