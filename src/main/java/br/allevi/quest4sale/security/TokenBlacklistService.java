package br.allevi.quest4sale.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {

    // Em produção, considere usar Redis para persistência distribuída
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    /**
     * Adiciona um token à blacklist
     * @param token Token JWT a ser invalidado
     */
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
        log.info("Token adicionado à blacklist. Total de tokens blacklisted: {}", blacklistedTokens.size());
    }

    /**
     * Verifica se um token está na blacklist
     * @param token Token JWT a ser verificado
     * @return true se o token está blacklisted, false caso contrário
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Remove tokens expirados da blacklist (limpeza periódica)
     * Em produção, considere usar @Scheduled para executar periodicamente
     */
    public void cleanupExpiredTokens() {
        // Implementação futura: remover tokens que já expiraram
        // Por enquanto, a limpeza seria manual ou via scheduler
        log.info("Limpeza de tokens expirados executada");
    }

    /**
     * Retorna o número de tokens na blacklist (útil para monitoramento)
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
}
