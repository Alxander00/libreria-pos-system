package com.libreria.pos.service;

import com.libreria.pos.dto.dte.*;
import com.libreria.pos.entities.PedidoEntity;
import com.libreria.pos.entities.PedidoDetalleEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DteBuilderService {

    public FacturaElectronicaDTO construirFactura(PedidoEntity pedido) {
        FacturaElectronicaDTO factura = new FacturaElectronicaDTO();

        // 1. Identificación
        IdentificacionDTO identificacion = new IdentificacionDTO();
        identificacion.setAmbiente("00"); // 00 = Pruebas
        identificacion.setCodigoGeneracion(UUID.randomUUID().toString().toUpperCase());
        // Usamos getIdPedidos() de tu entidad para el correlativo de 15 dígitos
        identificacion.setNumeroControl(String.format("DTE-01-M001P001-%015d", pedido.getIdPedidos()));
        identificacion.setFecEmi(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        identificacion.setHorEmi(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        factura.setIdentificacion(identificacion);

        // 2. Emisor (Datos fijos de la Librería)
        EmisorDTO emisor = new EmisorDTO();
        emisor.setNit("AQUI_VA_TU_NIT_SIN_GUIONES");
        emisor.setNrc("AQUI_TU_NRC");
        emisor.setNombre("Ernesto Alexander Tejada Siguenza");
        emisor.setCodActividad("47610");
        emisor.setDescActividad("Venta al por menor de libros y artículos de papelería");
        emisor.setNombreComercial("Libreria");
        emisor.setTipoEstablecimiento("01"); // 01 = Sucursal

        DireccionDTO dirEmisor = new DireccionDTO();
        dirEmisor.setDepartamento("01"); // Ahuachapán
        dirEmisor.setMunicipio("03");    // Atiquizaya
        dirEmisor.setComplemento("Dirección exacta del local");
        emisor.setDireccion(dirEmisor);

        emisor.setTelefono("22222222");
        emisor.setCorreo("contacto@libreria.com");
        factura.setEmisor(emisor);

        // 3. Receptor (Consumidor Final)
        ReceptorDTO receptor = new ReceptorDTO();
        receptor.setTipoDocumento("37"); // 37 = Otro
        receptor.setNumDocumento("000000000");
        receptor.setNombre("Cliente General");
        factura.setReceptor(receptor);

        // 4. Cuerpo (Usamos getItems() de tu entidad)
        List<ItemDTO> items = new ArrayList<>();
        int correlativo = 1;

        for (PedidoDetalleEntity detalle : pedido.getItems()) {
            ItemDTO item = new ItemDTO();
            item.setNumItem(correlativo++);
            item.setCantidad(detalle.getCantidad());
            item.setDescripcion(detalle.getProducto().getNombre());
            item.setPrecioUni(detalle.getPrecio()); // IVA Incluido

            double ventaGravada = detalle.getCantidad() * detalle.getPrecio();
            item.setVentaGravada(ventaGravada);
            item.setTributos(List.of("20")); // Código 20 = IVA 13%
            items.add(item);
        }
        factura.setCuerpoDocumento(items);

        // 5. Resumen
        ResumenDTO resumen = new ResumenDTO();
        double totalPedido = pedido.getTotal();

        resumen.setTotalGravada(totalPedido);
        resumen.setSubTotalVentas(totalPedido);
        resumen.setSubTotal(totalPedido);
        resumen.setMontoTotalOperacion(totalPedido);
        resumen.setTotalPagar(totalPedido);

        // Convertimos el total numérico a letras
        resumen.setTotalLetras(convertirTotalALetras(totalPedido));

        PagoDTO pago = new PagoDTO();
        // Evaluamos el metodoPago de tu entidad para asignar el código de Hacienda
        String metodoBd = pedido.getMetodoPago() != null ? pedido.getMetodoPago().toLowerCase() : "";
        if (metodoBd.contains("tarjeta")) {
            pago.setCodigo("02"); // 02 = Tarjeta
        } else if (metodoBd.contains("transferencia") || metodoBd.contains("deposito")) {
            pago.setCodigo("05"); // 05 = Transferencia
        } else {
            pago.setCodigo("01"); // 01 = Billetes y monedas (Por defecto)
        }

        pago.setMontoPago(totalPedido);
        resumen.setPagos(List.of(pago));

        factura.setResumen(resumen);

        return factura;
    }

    // --- UTILIDAD: Conversor de Número a Letras para el Ministerio de Hacienda ---
    private String convertirTotalALetras(double total) {
        String[] unidades = {"CERO", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE"};
        String[] decenas = {"DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE",
                "VEINTE", "VEINTIUNO", "VEINTIDOS", "VEINTITRES", "VEINTICUATRO", "VEINTICINCO", "VEINTISEIS", "VEINTISIETE", "VEINTIOCHO", "VEINTINUEVE"};
        String[] decenasPuras = {"", "", "VEINTI", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"};
        String[] centenas = {"", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"};

        long enteros = (long) total;
        int centavos = (int) Math.round((total - enteros) * 100);
        String letras = "";

        if (enteros == 0) letras = "CERO";
        else if (enteros == 100) letras = "CIEN";
        else {
            int c = (int) (enteros / 100);
            int d = (int) ((enteros % 100) / 10);
            int u = (int) (enteros % 10);

            if (c > 0) letras += centenas[c] + " ";
            if (d == 1 || d == 2) {
                letras += (d == 1 ? decenas[u] : decenas[d * 10 - 10 + u]) + " ";
            } else {
                if (d > 2) letras += decenasPuras[d] + (u > 0 ? " Y " : " ");
                if (u > 0) letras += unidades[u] + " ";
            }
        }
        return letras.trim() + " DOLARES CON " + String.format("%02d", centavos) + "/100";
    }
}