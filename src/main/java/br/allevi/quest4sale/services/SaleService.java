package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Sale;
import br.allevi.quest4sale.entities.User;
import br.allevi.quest4sale.repositories.SaleRepository;
import br.allevi.quest4sale.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
        User user = userRepository.findById(userId).orElseThrow();
        sale.setUser(user);
        return saleRepository.save(sale);
    }

    @Transactional(readOnly = true)
    public List<Sale> getUserSales(UUID userId, LocalDate start, LocalDate end) {
        User user = userRepository.findById(userId).orElseThrow();
        return saleRepository.findByUserAndSaleDateBetween(user, start, end);
    }

    @Transactional(readOnly = true)
    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Sale> findById(UUID id) {
        return saleRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Sale> findByUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return saleRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Sale> findByPeriod(LocalDate start, LocalDate end) {
        return saleRepository.findBySaleDateBetween(start, end);
    }

    @Transactional(readOnly = true)
    public List<Sale> findByPeriod(UUID userId, LocalDate start, LocalDate end) {
        User user = userRepository.findById(userId).orElseThrow();
        return saleRepository.findByUserAndSaleDateBetween(user, start, end);
    }

    @Transactional
    public Sale create(Sale sale) {
        return saleRepository.save(sale);
    }

    @Transactional(readOnly = true)
    public Double getTotalSalesByUser(UUID userId, LocalDate start, LocalDate end) {
        User user = userRepository.findById(userId).orElseThrow();
        return saleRepository.findByUserAndSaleDateBetween(user, start, end)
                .stream()
                .map(Sale::getAmount)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
    }
}



