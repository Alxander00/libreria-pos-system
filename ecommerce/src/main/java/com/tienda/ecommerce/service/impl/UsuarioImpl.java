package com.tienda.ecommerce.service.impl;

import com.tienda.ecommerce.dto.LoginRequest;
import com.tienda.ecommerce.dto.UsuarioRequest;
import com.tienda.ecommerce.dto.UsuarioResponse;
import com.tienda.ecommerce.entities.UsuarioEntity;
import com.tienda.ecommerce.repository.UsuarioRepository;
import com.tienda.ecommerce.security.JwtUtil;
import com.tienda.ecommerce.service.EmailService;
import com.tienda.ecommerce.service.IUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class UsuarioImpl implements IUsuario {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    // Carpeta donde se guardarán las fotos dentro de tu carpeta 'uploads'
    private final String uploadDir = "uploads/avatars";

    @Override
    public UsuarioEntity save(UsuarioEntity usuarioEntity) {
        if (usuarioEntity.getRol() == null) {
            usuarioEntity.setRol(UsuarioEntity.Rol.CLIENTE); // 👈 Corregido
        }

        usuarioEntity.setPassword(passwordEncoder.encode(usuarioEntity.getPassword()));
        return usuarioRepository.save(usuarioEntity);
    }

    @Override
    public String login(LoginRequest loginRequest) {
        // 👈 Restaurada TU lógica original de login
        UsuarioEntity usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        return jwtUtil.generarToken(usuario);
    }

    @Override
    public List<UsuarioEntity> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Page<UsuarioEntity> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    @Override
    public Page<UsuarioEntity> searchClientes(String nombre, Pageable pageable) {
        return usuarioRepository.findByNombreContainingIgnoreCaseAndRol(nombre, UsuarioEntity.Rol.CLIENTE, pageable); // 👈 Corregido
    }

    @Override
    public UsuarioResponse obtenerMiPerfil() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return mapearAResponse(usuario);
    }

    @Override
    public UsuarioResponse actualizarPerfil(UsuarioRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombre(request.getNombre());
        usuario.setTelefono(request.getTelefono());
        usuario.setDireccion(request.getDireccion());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        UsuarioEntity actualizado = usuarioRepository.save(usuario);
        return mapearAResponse(actualizado);
    }

    @Override
    public UsuarioResponse actualizarAvatar(MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
            Path root = Paths.get(uploadDir);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), root.resolve(filename));

            if (usuario.getFotoUrl() != null) {
                try {
                    Files.deleteIfExists(root.resolve(usuario.getFotoUrl()));
                } catch (IOException e) {
                    System.out.println("No se pudo borrar foto anterior");
                }
            }

            usuario.setFotoUrl(filename);
            usuarioRepository.save(usuario);

            return mapearAResponse(usuario);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage());
        }
    }

    private UsuarioResponse mapearAResponse(UsuarioEntity u) {
        UsuarioResponse res = new UsuarioResponse();
        res.setIdUsuario(u.getIdUsuario());
        res.setNombre(u.getNombre());
        res.setEmail(u.getEmail());
        res.setDireccion(u.getDireccion());
        res.setTelefono(u.getTelefono());
        res.setRol(u.getRol().name());
        res.setFotoUrl(u.getFotoUrl());
        return res;
    }

    @Override
    public void recuperarPassword(String email) {
        java.util.Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            UsuarioEntity usuario = usuarioOpt.get();

            // Generar clave temporal de 8 caracteres
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder tempPass = new StringBuilder();
            java.util.Random rnd = new java.util.Random();
            while (tempPass.length() < 8) {
                int index = (int) (rnd.nextFloat() * chars.length());
                tempPass.append(chars.charAt(index));
            }
            String nuevaClave = tempPass.toString();

            usuario.setPassword(passwordEncoder.encode(nuevaClave));
            usuarioRepository.save(usuario);

            String asunto = "Recuperación de Contraseña - MI TIENDA";
            String cuerpo = "Hola " + usuario.getNombre() + ",\n\n"
                    + "Se ha solicitado un restablecimiento de contraseña.\n\n"
                    + "Tu nueva clave temporal es: " + nuevaClave + "\n\n"
                    + "Inicia sesión con esta contraseña y cámbiala en tu perfil inmediatamente.\n\n"
                    + "Saludos,\nEl equipo de MI TIENDA";

            emailService.enviarNotificacion(email, asunto, cuerpo);
        }
    }
}