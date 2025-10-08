package br.allevi.quest4sale.controllers;

import br.allevi.quest4sale.entities.Sale;
import br.allevi.quest4sale.services.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;

    @GetMapping
    public List<Sale> getAllSales() {
        return saleService.findAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Sale> getSalesById(@PathVariable UUID id) {
        return saleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/user/{userId}")
    public List<Sale> getSalesByUserId(@PathVariable UUID userId) {
        return saleService.findByUser(userId);
    }
    @GetMapping("/period")
    public List<Sale> getSalesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return saleService.findByPeriod(start, end);
    }
    @GetMapping("/user/{userId}/period")
    public List<Sale> getSalesByUserAndPeriod(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return saleService.findByPeriod(userId, start, end);
    }
    @PostMapping
    public ResponseEntity<Sale> createSale(@Valid @RequestBody Sale sale) {
        try {
            Sale created = saleService.create(sale);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/user/{userId}/total")
    public ResponseEntity<Double> getTotalSale(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        Double total = saleService.getTotalSalesByUser(userId, start, end);
        return ResponseEntity.ok(total);
    }
}
