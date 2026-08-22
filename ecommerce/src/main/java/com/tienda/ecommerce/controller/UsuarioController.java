package com.tienda.ecommerce.controller;

import com.tienda.ecommerce.dto.LoginRequest;
import com.tienda.ecommerce.dto.LoginResponse;
import com.tienda.ecommerce.dto.UsuarioRequest;
import com.tienda.ecommerce.dto.UsuarioResponse;
import com.tienda.ecommerce.entities.UsuarioEntity;
import com.tienda.ecommerce.service.IUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private IUsuario iUsuario;

    @PostMapping("/register")
    public UsuarioResponse register(@RequestBody UsuarioRequest req) {
        UsuarioEntity u = new UsuarioEntity();

        u.setNombre(req.getNombre());
        u.setEmail(req.getEmail());
        u.setPassword(req.getPassword());
        u.setDireccion(req.getDireccion());
        u.setTelefono(req.getTelefono());

        if (req.getRol() != null) {
            u.setRol(UsuarioEntity.Rol.valueOf(req.getRol()));
        }else {
            u.setRol(UsuarioEntity.Rol.CLIENTE);
        }

        UsuarioEntity save = iUsuario.save(u);

        UsuarioResponse respuesta = new UsuarioResponse();
        respuesta.setIdUsuario(save.getIdUsuario());
        respuesta.setNombre(save.getNombre());
        respuesta.setEmail(save.getEmail());
        respuesta.setDireccion(save.getDireccion());
        respuesta.setTelefono(save.getTelefono());
        respuesta.setRol(save.getRol().name());
        return respuesta;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest usuario) {
        String token = iUsuario.login(usuario);
        return new LoginResponse(token);
    }

    @GetMapping("/todos")
    public Page<UsuarioEntity> listarTodos(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return iUsuario.searchClientes(buscar, PageRequest.of(page, size));
    }

    @GetMapping("/mi-perfil")
    public UsuarioResponse obtenerMiPerfil() {
        return iUsuario.obtenerMiPerfil();
    }

    @PutMapping("/actualizar")
    public UsuarioResponse actualizarPerfil(@RequestBody UsuarioRequest req) {
        return iUsuario.actualizarPerfil(req);
    }

    @PostMapping(value = "/actualizar-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UsuarioResponse actualizarAvatar(@RequestParam("file") MultipartFile file) {
        return iUsuario.actualizarAvatar(file);
    }

    @PostMapping("/recuperar-password")
    public org.springframework.http.ResponseEntity<?> recuperarPassword(@RequestBody java.util.Map<String, String> request) {
        iUsuario.recuperarPassword(request.get("email"));
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("mensaje", "Procesado"));
    }
}
