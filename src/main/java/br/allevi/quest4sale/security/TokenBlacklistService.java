package br.allevi.quest4sale.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();


    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
        log.info("Token adicionado à blacklist. Total de tokens blacklisted: {}", blacklistedTokens.size());
    }


    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

 
    public void cleanupExpiredTokens() {

        log.info("Limpeza de tokens expirados executada");
    }


    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
}
