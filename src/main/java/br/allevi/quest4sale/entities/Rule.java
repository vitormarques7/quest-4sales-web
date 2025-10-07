package br.allevi.quest4sale.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "competition")
@EqualsAndHashCode(of = "id")
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;
    @Column(name = "value_weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal valueWeight;
    @Column(name = "items_weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemsWeight;
    @Column(name = "positivation_weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal positivationWeight;
    @Column(name = "trip_weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal tripWeight;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
