package br.allevi.quest4sale.security.handlers;

import br.allevi.quest4sale.entities.dtos.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.warn("Access denied for user attempting to access: {} - Reason: {}",
                request.getRequestURI(),
                accessDeniedException.getMessage());

        String message = buildDetailedMessage(request, accessDeniedException);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                message,
                request.getRequestURI()
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String buildDetailedMessage(HttpServletRequest request, AccessDeniedException exception) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.startsWith("/api/competitions") && !method.equals("GET")) {
            return "Acesso negado. Apenas usuários com perfil ADMIN podem gerenciar competições.";
        }

        if (path.startsWith("/api/users")) {
            return "Acesso negado. Apenas usuários com perfil MANAGER podem gerenciar usuários.";
        }

        if (path.startsWith("/api/sales")) {
            return "Acesso negado. Você não tem permissão para realizar esta operação em vendas.";
        }

        if (path.startsWith("/api/scores")) {
            return "Acesso negado. Apenas ADMIN, MANAGER e SELLER podem acessar pontuações.";
        }

        return "Você não tem permissão para acessar este recurso. Verifique suas credenciais e perfil de acesso.";
    }
}
