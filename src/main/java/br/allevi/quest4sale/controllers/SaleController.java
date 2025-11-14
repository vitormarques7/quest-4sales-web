package br.allevi.quest4sale.controllers;

import br.allevi.quest4sale.entities.Sale;
import br.allevi.quest4sale.services.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;

    @GetMapping
    public Page<Sale> getAllSales(@PageableDefault(size = 20, sort = "saleDate") Pageable pageable) {
        return saleService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sale> getSalesById(@PathVariable UUID id) {
        return saleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public Page<Sale> getSalesByUserId(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "saleDate") Pageable pageable) {
        return saleService.findByUser(userId, pageable);
    }

    @GetMapping("/period")
    public Page<Sale> getSalesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 20, sort = "saleDate") Pageable pageable) {
        return saleService.findByPeriod(start, end, pageable);
    }

    @GetMapping("/user/{userId}/period")
    public Page<Sale> getSalesByUserAndPeriod(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 20, sort = "saleDate") Pageable pageable) {
        return saleService.findByPeriod(userId, start, end, pageable);
    }
    @PostMapping
    public ResponseEntity<Sale> createSale(@Valid @RequestBody Sale sale) {
        Sale created = saleService.create(sale);
        return ResponseEntity.ok(created);
    }
    @GetMapping("/user/{userId}/total")
    public ResponseEntity<BigDecimal> getTotalSale(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        BigDecimal total = saleService.getTotalSalesByUser(userId, start, end);
        return ResponseEntity.ok(total);
    }
}
