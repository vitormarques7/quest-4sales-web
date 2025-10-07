package br.allevi.quest4sale.services;

import br.allevi.quest4sale.entities.Sale;
import br.allevi.quest4sale.entities.User;
import br.allevi.quest4sale.repositories.SaleRepository;
import br.allevi.quest4sale.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
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
}


