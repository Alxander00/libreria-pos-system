package com.tienda.ecommerce.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class PruebaController {

    @GetMapping("/test")
    public String test(){
        return "Hola, el backend SI responde";
    }

    @GetMapping("/rol")
    public String rol(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return "Email: " + authentication.getName() + " | Roles: " + authentication.getAuthorities();
    }
}
