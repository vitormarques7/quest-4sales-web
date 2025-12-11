package br.allevi.quest4sale.security.handlers;

import br.allevi.quest4sale.entities.dtos.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

        log.warn("Authentication failed for request to: {} - Reason: {}",
                request.getRequestURI(),
                authException.getMessage());

        String message = buildDetailedMessage(request, authException);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                message,
                request.getRequestURI()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String buildDetailedMessage(HttpServletRequest request, AuthenticationException exception) {
        String exceptionMessage = exception.getMessage();

        // Token expirado
        if (exceptionMessage != null && exceptionMessage.contains("expired")) {
            return "Sua sessão expirou. Por favor, faça login novamente.";
        }

        // Token inválido
        if (exceptionMessage != null && (exceptionMessage.contains("invalid") || exceptionMessage.contains("malformed"))) {
            return "Token de autenticação inválido. Por favor, faça login novamente.";
        }

        // Token ausente
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "Token de autenticação não fornecido. Inclua o header 'Authorization: Bearer {token}' na requisição.";
        }

        // Credenciais inválidas (login)
        if (request.getRequestURI().contains("/login")) {
            return "Credenciais inválidas. Verifique seu usuário e senha.";
        }

        // Mensagem genérica
        return "Autenticação necessária. Por favor, faça login para acessar este recurso.";
    }
}
