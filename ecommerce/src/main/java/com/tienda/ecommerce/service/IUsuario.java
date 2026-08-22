package com.tienda.ecommerce.service;

import com.tienda.ecommerce.dto.LoginRequest;
import com.tienda.ecommerce.dto.UsuarioRequest;
import com.tienda.ecommerce.dto.UsuarioResponse;
import com.tienda.ecommerce.entities.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUsuario {

    UsuarioEntity save(UsuarioEntity usuario);
    String login(LoginRequest login);
    List<UsuarioEntity> findAll();
    Page<UsuarioEntity> findAll(Pageable pageable);
    Page<UsuarioEntity> searchClientes(String nombre, Pageable pageable);
    UsuarioResponse obtenerMiPerfil();
    UsuarioResponse actualizarPerfil(UsuarioRequest request);
    UsuarioResponse actualizarAvatar(MultipartFile file);
    void recuperarPassword(String email);
}
