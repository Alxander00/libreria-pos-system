package com.libreria.pos.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

    @GetMapping("/test")
    public String test(){
        return "Acceso CLIENTE correcto";
    }

    @GetMapping("/rol")
    public String rol(){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return "Email: " + auth.getName() + " | Roles: " + auth.getAuthorities();
    }
}
