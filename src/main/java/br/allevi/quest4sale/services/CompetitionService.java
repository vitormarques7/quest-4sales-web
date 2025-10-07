package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Competition;
import br.allevi.quest4sale.repositories.CompetitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
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
}


