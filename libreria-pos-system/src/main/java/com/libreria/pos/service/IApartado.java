package com.libreria.pos.service;

import com.libreria.pos.dto.AbonoRequest;
import com.libreria.pos.dto.ApartadoRequest;
import com.libreria.pos.dto.ApartadoResponse;

import java.util.List;

public interface IApartado {
    ApartadoResponse crearApartado(ApartadoRequest request);
    List<ApartadoResponse> obtenerMisApartados();
    List<ApartadoResponse> obtenerTodosApartados();
    ApartadoResponse abonar(Long idApartado, AbonoRequest request);
    ApartadoResponse cancelar(Long idApartado);
    ApartadoResponse liquidar(Long idApartado);
}