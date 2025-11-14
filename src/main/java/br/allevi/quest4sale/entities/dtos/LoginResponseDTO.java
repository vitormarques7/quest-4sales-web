package br.allevi.quest4sale.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private UUID userId;
    private String username;
    private String email;
    private String role;
}
