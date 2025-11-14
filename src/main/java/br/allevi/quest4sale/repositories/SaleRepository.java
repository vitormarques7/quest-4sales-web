package br.allevi.quest4sale.repositories;

import br.allevi.quest4sale.entities.Sale;
import br.allevi.quest4sale.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {
    Page<Sale> findByUser(User user, Pageable pageable);
    Page<Sale> findByUserAndSaleDateBetween(User user, LocalDate start, LocalDate end, Pageable pageable);
    Page<Sale> findBySaleDateBetween(LocalDate start, LocalDate end, Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Sale s WHERE s.user = :user AND s.saleDate BETWEEN :start AND :end")
    BigDecimal sumAmountByUserAndPeriod(@Param("user") User user, @Param("start") LocalDate start, @Param("end") LocalDate end);
}



