/*
package com.libreria.pos.config;

import com.libreria.pos.entities.UsuarioEntity;
import com.libreria.pos.repository.UsuarioRepository;
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
    public void run(String... args) {
        try {
            String adminEmail = "alextejada025@gmail.com";

            // Verificamos si la base de datos ya tiene este correo
            if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {

                UsuarioEntity admin = new UsuarioEntity();
                admin.setNombre("Ernesto (CEO)");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRol(UsuarioEntity.Rol.ADMIN);
                admin.setTelefono("71584643");
                admin.setDireccion("El Salvador");

                usuarioRepository.save(admin);
                System.out.println("✅ Cuenta de Administrador sembrada con éxito en la Base de Datos.");
            } else {
                System.out.println("ℹ️ El administrador ya existe, no se creó uno nuevo.");
            }

        } catch (Exception e) {
            // ✅ Si la BD falla, solo imprimimos el error y NO tumbamos la app
            System.err.println("⚠️ AdminSeeder: No se pudo verificar/crear el admin: " + e.getMessage());
            System.err.println("⚠️ La app continuará arrancando. El admin se creará cuando la BD esté disponible.");
        }
    }
}*/
