package com.mitienda.api_tienda.Service;

import com.mitienda.api_tienda.DTO.VentaRequestDTO;
import com.mitienda.api_tienda.Model.*;
import com.mitienda.api_tienda.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service // <-- ¡Este SÍ es un Servicio!
public class VentaService {

    // --- Inyección de Repositorios ---
    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // --- Lógica para CREAR Venta ---
    @Transactional
    public Venta crearVenta(VentaRequestDTO ventaRequest) {

        // 1. Validar entidades principales
        Usuario usuario = usuarioRepository.findById(ventaRequest.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario (Vendedor) no encontrado"));

        Cliente cliente = null; // El cliente puede ser nulo
        if (ventaRequest.getIdCliente() != null) {
            cliente = clienteRepository.findById(ventaRequest.getIdCliente())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        }

        // 2. Crear el objeto Venta principal
        Venta nuevaVenta = new Venta();
        nuevaVenta.setUsuario(usuario);
        nuevaVenta.setCliente(cliente);
        nuevaVenta.setTipoComprobante(ventaRequest.getTipoComprobante());

        List<DetalleVenta> detallesGuardados = new ArrayList<>();
        BigDecimal montoTotalCalculado = BigDecimal.ZERO;

        // 3. Procesar cada item del detalle
        for (VentaRequestDTO.DetalleVentaDTO itemDTO : ventaRequest.getDetalles()) {

            Producto producto = productoRepository.findById(itemDTO.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: ID " + itemDTO.getIdProducto()));

            // 4. VALIDACIÓN DE STOCK
            if (producto.getStockActual() < itemDTO.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            // 5. Crear el DetalleVenta
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(itemDTO.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());

            BigDecimal subtotal = producto.getPrecioVenta().multiply(new BigDecimal(itemDTO.getCantidad()));
            detalle.setSubtotal(subtotal);
            montoTotalCalculado = montoTotalCalculado.add(subtotal);

            detalle.setVenta(nuevaVenta);
            detallesGuardados.add(detalle);

            // 7. Descontar el stock
            producto.setStockActual(producto.getStockActual() - itemDTO.getCantidad());
            productoRepository.save(producto);
        }

        nuevaVenta.setDetalles(detallesGuardados);
        nuevaVenta.setMontoTotal(montoTotalCalculado); // Asignamos el total calculado

        // 6. Guardar la Venta y sus Detalles (en cascada)
        return ventaRepository.save(nuevaVenta);
    }

    // --- Lógica para CONSULTAR Ventas ---

    public List<Venta> obtenerTodasLasVentas() {
        return ventaRepository.findAll();
    }

    public Optional<Venta> obtenerVentaPorId(Integer id) {
        return ventaRepository.findById(id);
    }

    public List<Venta> obtenerVentasPorUsuario(Integer idUsuario) {
        return ventaRepository.findByUsuarioIdUsuario(idUsuario);
    }

    public List<Venta> obtenerVentasPorCliente(Integer idCliente) {
        return ventaRepository.findByClienteIdCliente(idCliente);
    }

    public List<Venta> obtenerVentasPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaVentaBetween(inicio, fin);
    }

    public BigDecimal obtenerIngresosTotales() {
        BigDecimal total = ventaRepository.calcularTotalVentas();
        return (total == null) ? BigDecimal.ZERO : total;
    }
}