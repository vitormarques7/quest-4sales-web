package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Competition;
import br.allevi.quest4sale.entities.Enums.CompetitionStatus;
import br.allevi.quest4sale.entities.Sale;
import br.allevi.quest4sale.entities.User;
import br.allevi.quest4sale.exceptions.BadRequestException;
import br.allevi.quest4sale.exceptions.ResourceNotFoundException;
import br.allevi.quest4sale.repositories.CompetitionRepository;
import br.allevi.quest4sale.repositories.SaleRepository;
import br.allevi.quest4sale.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;
    private final ScoreService scoreService;

    public SaleService(
            SaleRepository saleRepository,
            UserRepository userRepository,
            CompetitionRepository competitionRepository,
            ScoreService scoreService) {
        this.saleRepository = saleRepository;
        this.userRepository = userRepository;
        this.competitionRepository = competitionRepository;
        this.scoreService = scoreService;
    }

    @Transactional
    public Sale createSale(UUID userId, Sale sale) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));
        sale.setUser(user);
        return saleRepository.save(sale);
    }

    @Transactional(readOnly = true)
    public List<Sale> getUserSales(UUID userId, LocalDate start, LocalDate end) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));
        return saleRepository.findByUserAndSaleDateBetween(user, start, end);
    }

    @Transactional(readOnly = true)
    public Page<Sale> findAll(Pageable pageable) {
        return saleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Sale> findById(UUID id) {
        return saleRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Sale> findByUser(UUID userId, Pageable pageable) {
        User user = getUserOrThrow(userId);
        return saleRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Sale> findByPeriod(LocalDate start, LocalDate end, Pageable pageable) {
        return saleRepository.findBySaleDateBetween(start, end, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Sale> findByPeriod(UUID userId, LocalDate start, LocalDate end, Pageable pageable) {
        User user = getUserOrThrow(userId);
        return saleRepository.findByUserAndSaleDateBetween(user, start, end, pageable);
    }

    @Transactional
    public Sale create(Sale sale) {
        // Validações
        validateSale(sale);

        // Salvar venda
        Sale savedSale = saleRepository.save(sale);
        log.info("Venda criada: ID={}, User ID={}, Amount={}, Date={}",
                savedSale.getId(), savedSale.getUser().getId(), savedSale.getAmount(), savedSale.getSaleDate());

        // Buscar competições ativas no período da venda
        List<Competition> activeCompetitions = competitionRepository
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        savedSale.getSaleDate(),
                        savedSale.getSaleDate()
                );

        // Filtrar apenas competições ATIVAS
        List<Competition> validCompetitions = activeCompetitions.stream()
                .filter(competition -> competition.getStatus() == CompetitionStatus.ATIVA)
                .toList();

        if (validCompetitions.isEmpty()) {
            log.warn("Venda criada mas nenhuma competição ativa encontrada para a data: {}", savedSale.getSaleDate());
        } else {
            log.info("Venda se qualifica para {} competição(ões) ativa(s)", validCompetitions.size());

            // Calcular score para cada competição ativa
            for (Competition competition : validCompetitions) {
                try {
                    scoreService.calculateAndSaveScore(savedSale.getId(), competition.getId());
                    log.info("Score calculado para competição: {} (ID: {})",
                            competition.getName(), competition.getId());
                } catch (Exception e) {
                    log.error("Erro ao calcular score para competição ID {}: {}",
                            competition.getId(), e.getMessage(), e);
                }
            }
        }

        return savedSale;
    }

    private void validateSale(Sale sale) {
        if (sale == null) {
            throw new BadRequestException("Venda não pode ser nula");
        }

        if (sale.getAmount() == null || sale.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Valor da venda deve ser maior que zero");
        }

        if (sale.getQuantity() == null || sale.getQuantity() <= 0) {
            throw new BadRequestException("Quantidade deve ser maior que zero");
        }

        if (sale.getSaleDate() == null) {
            throw new BadRequestException("Data da venda é obrigatória");
        }

        if (sale.getSaleDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Data da venda não pode ser no futuro");
        }

        if (sale.getPositiveSale() == null) {
            sale.setPositiveSale(false);
        }

        if (sale.getTravelQuantity() == null) {
            sale.setTravelQuantity(0);
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalSalesByUser(UUID userId, LocalDate start, LocalDate end) {
        User user = getUserOrThrow(userId);
        return saleRepository.sumAmountByUserAndPeriod(user, start, end);
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));
    }
}



