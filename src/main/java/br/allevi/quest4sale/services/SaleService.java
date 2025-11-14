package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Sale;
import br.allevi.quest4sale.entities.User;
import br.allevi.quest4sale.exceptions.ResourceNotFoundException;
import br.allevi.quest4sale.repositories.SaleRepository;
import br.allevi.quest4sale.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final UserRepository userRepository;

    public SaleService(SaleRepository saleRepository, UserRepository userRepository) {
        this.saleRepository = saleRepository;
        this.userRepository = userRepository;
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
        return saleRepository.save(sale);
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



