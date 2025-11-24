package com.mitienda.api_tienda.DTO;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardMetricsDTO {
    // Ventas monetarias
    private BigDecimal ventasHoy;
    private BigDecimal ventasSemana;
    private BigDecimal ventasMes;
    private BigDecimal ingresosTotales;

    // Conteos
    private Long cantidadVentasHoy;
    private Long totalProductos;
    private Long totalUsuarios;
    private Long totalClientes;
}