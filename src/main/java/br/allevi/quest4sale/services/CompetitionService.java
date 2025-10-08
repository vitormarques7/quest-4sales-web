package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Competition;
import br.allevi.quest4sale.entities.Enums.CompetitionStatus;
import br.allevi.quest4sale.repositories.CompetitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CompetitionService {

    private final CompetitionRepository competitionRepository;

    public CompetitionService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @Transactional(readOnly = true)
    public Competition getById(UUID id) {
        return competitionRepository.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Optional<Competition> findById(UUID id) {
        return competitionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Competition> findAll() {
        return competitionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Competition> findActive() {
        return competitionRepository.findByStatus(CompetitionStatus.ATIVA);
    }

    @Transactional
    public Competition create(Competition competition) {
        return competitionRepository.save(competition);
    }

    @Transactional
    public Competition update(Competition competition) {
        return competitionRepository.save(competition);
    }

    @Transactional
    public void delete(UUID id) {
        competitionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Competition> getActiveOn(LocalDate date) {
        return competitionRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);
    }

    @Transactional
    public void startCompetition(UUID id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Competition not found"));
        
        if (competition.getStatus() != CompetitionStatus.PLANEJADA) {
            throw new RuntimeException("Competition must be in PLANNED status to start");
        }
        
        competition.setStatus(CompetitionStatus.ATIVA);
        competitionRepository.save(competition);
    }

    @Transactional
    public void finishCompetition(UUID id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Competition not found"));
        
        competition.setStatus(CompetitionStatus.FINALIZADA);
        competitionRepository.save(competition);
    }

    @Transactional
    public Competition updateStatus(UUID id, CompetitionStatus status) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Competition not found"));
        
        competition.setStatus(status);
        return competitionRepository.save(competition);
    }
}



