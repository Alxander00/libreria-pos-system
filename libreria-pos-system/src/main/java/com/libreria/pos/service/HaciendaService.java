package com.libreria.pos.service;

import com.libreria.pos.dto.dte.FacturaElectronicaDTO;

public interface HaciendaService {
    String firmarFactura(FacturaElectronicaDTO factura);
}