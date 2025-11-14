package br.allevi.quest4sale.entities.dtos;

import br.allevi.quest4sale.validation.PasswordStrength;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    private String username;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Password é obrigatório")
    @Size(min = 8, message = "Password deve ter no mínimo 8 caracteres")
    @PasswordStrength
    private String password;

    @NotBlank(message = "First name é obrigatório")
    private String firstName;

    @NotBlank(message = "Last name é obrigatório")
    private String lastName;

    private UUID roleId;
}
