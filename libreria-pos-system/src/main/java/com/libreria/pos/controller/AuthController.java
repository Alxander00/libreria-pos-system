package com.libreria.pos.controller;

import com.libreria.pos.dto.RefreshTokenRequest;
import com.libreria.pos.dto.TokenResponse;
import com.libreria.pos.entities.UsuarioEntity;
import com.libreria.pos.repository.UsuarioRepository;
import com.libreria.pos.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/me")
    public String me(){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName() +" " + auth.getAuthorities();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.validaToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido o expirado");
        }

        String email = jwtUtil.obtenerEmail(refreshToken);
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String nuevoToken = jwtUtil.generarToken(usuario);
        String nuevoRefreshToken = jwtUtil.generarRefreshToken(usuario);

        return ResponseEntity.ok(new TokenResponse(nuevoToken, nuevoRefreshToken));
    }
}