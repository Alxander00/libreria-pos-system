package com.libreria.pos.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/me")
    public String me(){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName() +" " + auth.getAuthorities();
    }
}
