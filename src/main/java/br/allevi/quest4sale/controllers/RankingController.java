package br.allevi.quest4sale.controllers;

import br.allevi.quest4sale.entities.Ranking;
import br.allevi.quest4sale.services.RankingService;
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
    public ResponseEntity<Ranking> getUserRanking(
            @PathVariable UUID competitionId,
            @PathVariable UUID userId) {
        return rankingService.getUserRanking(competitionId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/competition/{competitionId}/user/{userId}/position")
    public ResponseEntity<Integer> getUserPosition(
            @PathVariable UUID competitionId,
            @PathVariable UUID userId) {
        Integer position = rankingService.getUserPosition(competitionId, userId);
        return position != -1 ? ResponseEntity.ok(position) : ResponseEntity.notFound().build();
    }

    @GetMapping("/competition/{competitionId}/around-user/{userId}")
    public ResponseEntity<List<Ranking>> getRankingsAroundUser(
            @PathVariable UUID competitionId,
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "2") int range) {
        try {
            Integer userPosition = rankingService.getUserPosition(competitionId, userId);
            if (userPosition == -1) {
                return ResponseEntity.notFound().build();
            }

            int start = Math.max(1, userPosition - range);
            int end = userPosition + range;

            List<Ranking> rankings = rankingService.getCompetitionRanking(competitionId);
            List<Ranking> aroundUser = rankings.stream()
                    .filter(r -> r.getRank() >= start && r.getRank() <= end)
                    .toList();

            return ResponseEntity.ok(aroundUser);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}