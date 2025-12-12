package br.allevi.quest4sale.config;

import br.allevi.quest4sale.security.JwtAuthenticationFilter;
import br.allevi.quest4sale.security.handlers.CustomAccessDeniedHandler;
import br.allevi.quest4sale.security.handlers.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomAuthenticationEntryPoint authenticationEntryPoint,
                          CustomAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configure(http))  
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/auth/**").authenticated()
                        // Competições
                        .requestMatchers(HttpMethod.GET, "/api/competitions/**").permitAll()
                        .requestMatchers("/api/competitions/**").hasRole("ADMIN")
                        // Usuários
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "MANAGER")
                        // Vendas
                        .requestMatchers(HttpMethod.GET, "/api/sales/**").hasAnyRole("ADMIN", "MANAGER", "SELLER")
                        .requestMatchers("/api/sales/**").hasAnyRole("ADMIN", "MANAGER", "SELLER") // Permite POST para criar venda
                        // Scores e Ranking
                        .requestMatchers("/api/scores/**").hasAnyRole("ADMIN", "MANAGER", "SELLER")
                        .requestMatchers("/api/ranking/**").permitAll()
                        // Notificações
                        .requestMatchers("/api/notifications/**").authenticated()
                        // REGRAS (Adicionado)
                        .requestMatchers(HttpMethod.GET, "/api/rules/**").authenticated()
                        .requestMatchers("/api/rules/**").hasAnyRole("ADMIN", "MANAGER")
                        
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}