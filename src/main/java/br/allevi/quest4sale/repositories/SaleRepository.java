package br.allevi.quest4sale.repositories;

import br.allevi.quest4sale.entities.Sale;
import br.allevi.quest4sale.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {
    List<Sale> findByUser(User user);
    List<Sale> findByUserAndSaleDateBetween(User user, LocalDate start, LocalDate end);
}


