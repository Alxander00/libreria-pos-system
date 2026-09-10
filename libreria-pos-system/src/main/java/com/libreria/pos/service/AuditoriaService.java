package com.libreria.pos.service;

import com.libreria.pos.entities.AuditoriaEntity;
import com.libreria.pos.repository.AuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired(required = false)
    private HttpServletRequest request;

    public void registrar(String accion, String detalles) {
        AuditoriaEntity auditoria = new AuditoriaEntity();

        // Obtener email del usuario autenticado
        String email = "SISTEMA";
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            email = auth.getName();
        }

        auditoria.setUsuarioEmail(email);
        auditoria.setAccion(accion);
        auditoria.setDetalles(detalles);

        // Obtener IP y endpoint
        if (request != null) {
            auditoria.setIpCliente(request.getRemoteAddr());
            auditoria.setEndpoint(request.getRequestURI());
        }

        auditoriaRepository.save(auditoria);
    }
}