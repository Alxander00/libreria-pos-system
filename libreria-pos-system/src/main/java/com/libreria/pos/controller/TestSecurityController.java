package com.libreria.pos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSecurityController {

    @GetMapping("/protegido")
    public  String protegido(){
        return "Acceso permitido, token valido";
    }
}
