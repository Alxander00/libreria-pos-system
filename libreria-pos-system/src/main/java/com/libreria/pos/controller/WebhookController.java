package com.libreria.pos.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.libreria.pos.entities.EstadoPedido;
import com.libreria.pos.entities.PedidoEntity;
import com.libreria.pos.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/wompi")
    public ResponseEntity<String> recibirEventoWompi(@RequestBody String payload) {
        try {
            JsonNode json = objectMapper.readTree(payload);

            // Verificar que existe el campo "event"
            if (json.has("event") && "transaction.updated".equals(json.get("event").asText())) {

                // Usamos .path() en lugar de .get() para que no truene si no existe
                JsonNode data = json.path("data");
                JsonNode transaction = data.path("transaction");

                // Verificar que la transacción existe
                if (!transaction.isMissingNode()) {
                    String status = transaction.path("status").asText("");
                    String reference = transaction.path("reference").asText("");

                    // Extraer SOLO los números de la referencia (ej: "Pedido #1" -> "1")
                    String numeroPedidoStr = reference.replaceAll("[^0-9]", "");

                    if (!numeroPedidoStr.isEmpty()) {
                        Long idPedido = Long.parseLong(numeroPedidoStr);

                        Optional<PedidoEntity> optionalPedido = pedidoRepository.findById(idPedido);
                        if (optionalPedido.isPresent()) {
                            PedidoEntity pedido = optionalPedido.get();

                            if ("APPROVED".equals(status)) {
                                pedido.setEstado(EstadoPedido.PAGADO); // Asegúrate de tener este estado en tu ENUM
                            } else if ("DECLINED".equals(status) || "ERROR".equals(status)) {
                                pedido.setEstado(EstadoPedido.PENDIENTE); // O el estado que uses para fallo
                            }

                            pedidoRepository.save(pedido);
                            return ResponseEntity.ok("Evento recibido y pedido actualizado");
                        } else {
                            return ResponseEntity.ok("Evento recibido pero pedido no encontrado");
                        }
                    }
                }
            }

            return ResponseEntity.ok("Evento recibido sin acción");

        } catch (Exception e) {
            // Esto imprimirá el error exacto en la consola de IntelliJ
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error procesando el evento: " + e.getMessage());
        }
    }
}