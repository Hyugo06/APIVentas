package com.mitienda.api_tienda.DTO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaResponseDTO {
    private Integer idVenta;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String estado;
    private String tipoComprobante;

    // Datos Cliente
    private String nombreCliente;
    private String dniCliente;
    private String celularCliente;

    // Cupón
    private String codigoCupon;
    private BigDecimal montoDescuento;

    private List<DetalleResponseDTO> detalles;

    @Data
    public static class DetalleResponseDTO {
        private String producto;

        private String sku;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;

        private String color;
        private String talla;
    }
}