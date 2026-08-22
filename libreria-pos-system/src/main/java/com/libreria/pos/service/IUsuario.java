package com.libreria.pos.service;

import com.libreria.pos.dto.LoginRequest;
import com.libreria.pos.dto.UsuarioRequest;
import com.libreria.pos.dto.UsuarioResponse;
import com.libreria.pos.entities.UsuarioEntity;
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
