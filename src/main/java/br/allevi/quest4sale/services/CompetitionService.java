package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Competition;
import br.allevi.quest4sale.entities.Enums.CompetitionStatus;
import br.allevi.quest4sale.exceptions.InvalidStateException;
import br.allevi.quest4sale.exceptions.ResourceNotFoundException;
import br.allevi.quest4sale.repositories.CompetitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final NotificationService notificationService;

    public CompetitionService(
            CompetitionRepository competitionRepository,
            NotificationService notificationService) {
        this.competitionRepository = competitionRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Competition getById(UUID id) {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competition não encontrada com ID: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<Competition> findById(UUID id) {
        return competitionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Competition> findAll() {
        return competitionRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Competition> findActive() {
        return competitionRepository.findByActiveTrueAndStatus(CompetitionStatus.ATIVA);
    }

    @Transactional
    public Competition create(Competition competition) {
        validateCompetition(competition);

        return competitionRepository.save(competition);
    }

    private void validateCompetition(Competition competition) {
        if (competition.getEndDate().isBefore(competition.getStartDate())) {
            throw new InvalidStateException("Data de fim não pode ser anterior à data de início");
        }
    }

    @Transactional
    public Competition update(Competition competition) {
        return competitionRepository.save(competition);
    }

    @Transactional
    public void delete(UUID id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competition não encontrada com ID: " + id));

        competition.setActive(false);
        competitionRepository.save(competition);

        log.info("Competição desativada (soft delete): {} (ID: {})", competition.getName(), id);
    }

    @Transactional(readOnly = true)
    public List<Competition> getActiveOn(LocalDate date) {
        return competitionRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);
    }

    @Transactional
    public void startCompetition(UUID id) {
        log.info("Iniciando competição ID: {}", id);

        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competition não encontrada com ID: " + id));

        if (competition.getStatus() != CompetitionStatus.PLANEJADA) {
            throw new InvalidStateException("Competition deve estar no status PLANEJADA para ser iniciada. Status atual: " + competition.getStatus());
        }

        competition.setStatus(CompetitionStatus.ATIVA);
        competitionRepository.save(competition);

        log.info("Competição iniciada: {} (ID: {})", competition.getName(), id);

        try {
            notificationService.notifyCompetitionStarted(id);
            log.info("Notificações de início enviadas para competição ID: {}", id);
        } catch (Exception e) {
            log.error("Erro ao enviar notificações de início da competição ID {}: {}", id, e.getMessage(), e);
        }
    }

    @Transactional
    public void finishCompetition(UUID id) {
        log.info("Finalizando competição ID: {}", id);

        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competition não encontrada com ID: " + id));

        competition.setStatus(CompetitionStatus.FINALIZADA);
        competitionRepository.save(competition);

        log.info("Competição finalizada: {} (ID: {})", competition.getName(), id);

        try {
            notificationService.notifyCompetitionFinished(id);
            log.info("Notificações de finalização enviadas para competição ID: {}", id);
        } catch (Exception e) {
            log.error("Erro ao enviar notificações de finalização da competição ID {}: {}", id, e.getMessage(), e);
        }
    }

    @Transactional
    public Competition updateStatus(UUID id, CompetitionStatus status) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competition não encontrada com ID: " + id));

        competition.setStatus(status);
        return competitionRepository.save(competition);
    }
}



