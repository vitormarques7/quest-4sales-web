package br.allevi.quest4sale.entities;

import br.allevi.quest4sale.entities.Enums.CompetitionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "competition", indexes = {
    @Index(name = "idx_competition_status", columnList = "status"),
    @Index(name = "idx_competition_dates", columnList = "start_date, end_date")
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Competition {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    @NotBlank
    @Size(max = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    @Size(max = 500)
    private String description;
    
    @Column(name = "start_date", nullable = false)
    @NotNull
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    @NotNull
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CompetitionStatus status = CompetitionStatus.PLANEJADA;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
