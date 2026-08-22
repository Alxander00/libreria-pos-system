package com.libreria.pos.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        // 1. Configuración base (CORS y CSRF)
        http.cors(cors -> cors.configure(http))
                .csrf(csrf -> csrf.disable());

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.authorizeHttpRequests(auth -> auth
                        // RUTAS PÚBLICAS (Sin login)
                        .requestMatchers(
                                "/usuario/register", "/usuario/login", "/usuario/recuperar-password", "/error",
                                "/producto/imagen/**", "/uploads/**", "/imagenes/**",
                                "/producto/**", "/categoria/**"
                        ).permitAll()

                // 2. RUTAS EXCLUSIVAS DE ADMIN
                .requestMatchers("/pedidos/todos", "/admin/**").hasRole("ADMIN") // 1. Primero Admin
                .requestMatchers("/pedidos/**").hasAnyRole("ADMIN", "CLIENTE") // 2. Luego compartido
                .requestMatchers("/carrito/**", "/pagos/**").hasRole("CLIENTE") // 3. Al final Cliente

                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
