package com.tienda.ecommerce.config;

import com.tienda.ecommerce.entities.UsuarioEntity;
import com.tienda.ecommerce.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // El correo que usarás para entrar al panel
        String adminEmail = "admin@techstore.com";

        // Verificamos si la base de datos ya tiene este correo
        if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {

            UsuarioEntity admin = new UsuarioEntity();
            admin.setNombre("Ernesto (CEO)");
            admin.setEmail(adminEmail);
            // Encriptamos la contraseña para que Spring Security la acepte
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol(UsuarioEntity.Rol.ADMIN);
            admin.setTelefono("70000000");
            admin.setDireccion("El Salvador");

            usuarioRepository.save(admin);
            System.out.println("✅ Cuenta de Administrador sembrada con éxito en la Base de Datos.");
        }
    }
}