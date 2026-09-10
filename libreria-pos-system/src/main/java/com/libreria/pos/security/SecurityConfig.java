package com.libreria.pos.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CORS usando el bean CorsConfigurationSource
        http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable());

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.authorizeHttpRequests(auth -> auth
                // ✅ Permitir preflight OPTIONS siempre
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // RUTAS PÚBLICAS (Sin login)
                .requestMatchers(
                        "/usuario/register", "/usuario/login", "/usuario/recuperar-password", "/error",
                        "/producto/imagen/**", "/uploads/**", "/imagenes/**",
                        "/producto/**", "/categoria/**",
                        "/api/webhooks/**",
                        "/api/pagos/wompi/**"
                ).permitAll()

                // RUTAS EXCLUSIVAS DE ADMIN
                .requestMatchers("/pedidos/todos", "/admin/**").hasRole("ADMIN")
                .requestMatchers("/pedidos/**").hasAnyRole("ADMIN", "CLIENTE")
                .requestMatchers("/carrito/**", "/pagos/**").hasRole("CLIENTE")

                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}