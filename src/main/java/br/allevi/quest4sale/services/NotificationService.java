package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Competition;
import br.allevi.quest4sale.entities.Notification;
import br.allevi.quest4sale.entities.User;
import br.allevi.quest4sale.entities.Enums.NotificationType;
import br.allevi.quest4sale.entities.dtos.NotificationDTO;
import br.allevi.quest4sale.exceptions.ResourceNotFoundException;
import br.allevi.quest4sale.repositories.CompetitionRepository;
import br.allevi.quest4sale.repositories.NotificationRepository;
import br.allevi.quest4sale.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .userName(notification.getUser().getUsername())
                .competitionId(notification.getCompetition() != null ? notification.getCompetition().getId() : null)
                .competitionName(notification.getCompetition() != null ? notification.getCompetition().getName() : null)
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification não encontrada com ID: " + id));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }


    @Transactional
    public void notifyRankingEntry(UUID userId, UUID competitionId, int rank) {
        log.info("Criando notificação de entrada no ranking: User ID={}, Competition ID={}, Rank={}",
                userId, competitionId, rank);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competição não encontrada: " + competitionId));

        String title = "Você entrou no ranking!";
        String message = String.format("Parabéns! Você está em %dº lugar na competição '%s'.",
                rank, competition.getName());

        Notification notification = Notification.builder()
                .user(user)
                .competition(competition)
                .type(NotificationType.INFO)
                .title(title)
                .message(message)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Notificação de entrada no ranking criada com sucesso");
    }


    @Transactional
    public void notifyRankImprovement(UUID userId, UUID competitionId, int oldRank, int newRank) {
        log.info("Criando notificação de melhoria no ranking: User ID={}, Old Rank={}, New Rank={}",
                userId, oldRank, newRank);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competição não encontrada: " + competitionId));

        String title = "Você subiu no ranking!";
        String message;

        if (newRank == 1) {
            message = String.format("Parabéns! Você chegou ao 1º lugar na competição '%s'! 🏆",
                    competition.getName());
        } else {
            int positions = oldRank - newRank;
            message = String.format("Parabéns! Você subiu %d %s no ranking da competição '%s'. Agora você está em %dº lugar!",
                    positions,
                    positions == 1 ? "posição" : "posições",
                    competition.getName(),
                    newRank);
        }

        Notification notification = Notification.builder()
                .user(user)
                .competition(competition)
                .type(newRank <= 3 ? NotificationType.PREMIO : NotificationType.INFO)
                .title(title)
                .message(message)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Notificação de melhoria no ranking criada com sucesso");
    }


    @Transactional
    public void notifyRankDrop(UUID userId, UUID competitionId, int oldRank, int newRank) {
        log.info("Criando notificação de queda no ranking: User ID={}, Old Rank={}, New Rank={}",
                userId, oldRank, newRank);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competição não encontrada: " + competitionId));

        int positions = newRank - oldRank;
        String title = "Atualização de ranking";
        String message = String.format("Você caiu %d %s no ranking da competição '%s'. Agora você está em %dº lugar. Continue se esforçando!",
                positions,
                positions == 1 ? "posição" : "posições",
                competition.getName(),
                newRank);

        Notification notification = Notification.builder()
                .user(user)
                .competition(competition)
                .type(NotificationType.AVISO)
                .title(title)
                .message(message)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Notificação de queda no ranking criada com sucesso");
    }


    @Transactional
    public void notifyCompetitionStarted(UUID competitionId) {
        log.info("Criando notificações de início de competição: Competition ID={}", competitionId);

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competição não encontrada: " + competitionId));

        List<User> allUsers = userRepository.findAll();

        String title = "Nova competição iniciada!";
        String message = String.format("A competição '%s' começou! Participe e concorra a prêmios incríveis!",
                competition.getName());

        allUsers.forEach(user -> {
            Notification notification = Notification.builder()
                    .user(user)
                    .competition(competition)
                    .type(NotificationType.SISTEMA)
                    .title(title)
                    .message(message)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
        });

        log.info("Notificações de início de competição criadas para {} usuários", allUsers.size());
    }


    @Transactional
    public void notifyCompetitionFinished(UUID competitionId) {
        log.info("Criando notificações de fim de competição: Competition ID={}", competitionId);

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competição não encontrada: " + competitionId));

        List<User> allUsers = userRepository.findAll();

        String title = "Competição finalizada!";
        String message = String.format("A competição '%s' foi finalizada! Confira os resultados finais e os vencedores.",
                competition.getName());

        allUsers.forEach(user -> {
            Notification notification = Notification.builder()
                    .user(user)
                    .competition(competition)
                    .type(NotificationType.SISTEMA)
                    .title(title)
                    .message(message)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
        });

        log.info("Notificações de fim de competição criadas para {} usuários", allUsers.size());
    }
}