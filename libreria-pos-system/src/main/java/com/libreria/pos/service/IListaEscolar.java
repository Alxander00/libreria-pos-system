package com.libreria.pos.service;

import com.libreria.pos.dto.ListaEscolarRequest;
import com.libreria.pos.dto.ListaEscolarResponse;

import java.util.List;

public interface IListaEscolar {
    ListaEscolarResponse crearLista(ListaEscolarRequest request);
    List<ListaEscolarResponse> obtenerMisListas();
    List<ListaEscolarResponse> obtenerTodasLasListas();
    ListaEscolarResponse armarLista(Long idLista);
    ListaEscolarResponse retirarLista(Long idLista);
    ListaEscolarResponse obtenerLista(Long id);
    ListaEscolarResponse actualizarLista(Long id, ListaEscolarRequest request);
}