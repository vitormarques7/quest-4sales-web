package br.allevi.quest4sale.entities.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleDTO {

    @NotNull(message = "Peso do valor não pode ser nulo")
    @DecimalMin(value = "0.0", message = "Peso do valor deve ser maior ou igual a 0")
    private BigDecimal valueWeight;

    @NotNull(message = "Peso de itens não pode ser nulo")
    @DecimalMin(value = "0.0", message = "Peso de itens deve ser maior ou igual a 0")
    private BigDecimal itemsWeight;

    @NotNull(message = "Peso de positivação não pode ser nulo")
    @DecimalMin(value = "0.0", message = "Peso de positivação deve ser maior ou igual a 0")
    private BigDecimal positivationWeight;

    @NotNull(message = "Peso de viagens não pode ser nulo")
    @DecimalMin(value = "0.0", message = "Peso de viagens deve ser maior ou igual a 0")
    private BigDecimal tripWeight;
}
