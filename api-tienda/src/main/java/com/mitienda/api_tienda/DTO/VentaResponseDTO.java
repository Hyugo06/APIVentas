package com.mitienda.api_tienda.DTO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaResponseDTO {
    private Integer idVenta;
    private LocalDateTime fecha;
    private BigDecimal total;     // Lo que pagó finalmente
    private String estado;
    private String tipoComprobante;

    // Datos Cliente
    private String nombreCliente;
    private String dniCliente;
    private String celularCliente;

    // --- DATOS DEL CUPÓN ---
    private String codigoCupon;       // Ej: "HYUGO2025"
    private BigDecimal montoDescuento; // Ej: 20.00
    // -----------------------

    // Reutilizamos tu DTO de detalle existente o creamos uno simple
    // Para simplificar, usaré una estructura genérica aquí, pero puedes usar tu DetalleVentaDTO si lo tienes de salida
    private List<DetalleResponseDTO> detalles;

    @Data
    public static class DetalleResponseDTO {
        private String producto;
        private String sku;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}