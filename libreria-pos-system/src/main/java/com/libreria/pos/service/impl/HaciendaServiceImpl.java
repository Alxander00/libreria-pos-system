package com.libreria.pos.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libreria.pos.dto.dte.FacturaElectronicaDTO;
import com.libreria.pos.service.HaciendaService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HaciendaServiceImpl implements HaciendaService {

    private final ObjectMapper objectMapper;

    public HaciendaServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String firmarFactura(FacturaElectronicaDTO factura) {
        try {
            // 1. Convertimos la factura a JSON real para validar que tu estructura está perfecta
            String facturaJsonStr = objectMapper.writeValueAsString(factura);

            System.out.println("==================================================");
            System.out.println("🧾 FACTURA GENERADA (MODO SIMULADOR) 🧾");
            System.out.println(facturaJsonStr);
            System.out.println("==================================================");

            // 2. Simulamos la firma criptográfica (JWS) que te devolvería el Firmador real
            String firmaSimulada = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.SIMULADOR_DE_FIRMA.XyZ_123_FALSA";

            // 3. Simulamos el código de Sello de Recepción que te daría el Ministerio de Hacienda
            String selloSimulado = UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 30);

            // 4. Devolvemos una cadena JSON simulando la respuesta exitosa
            return "{\n" +
                    "  \"estado\": \"PROCESADO\",\n" +
                    "  \"selloRecibido\": \"" + selloSimulado + "\",\n" +
                    "  \"firmaElectronica\": \"" + firmaSimulada + "\",\n" +
                    "  \"mensaje\": \"Simulación exitosa para desarrollo\"\n" +
                    "}";

        } catch (Exception e) {
            throw new RuntimeException("Error en el simulador de Hacienda: " + e.getMessage());
        }
    }
}