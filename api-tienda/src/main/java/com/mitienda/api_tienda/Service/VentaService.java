package com.mitienda.api_tienda.Service;

import com.mitienda.api_tienda.DTO.ClienteRequestDTO;
import com.mitienda.api_tienda.DTO.DashboardMetricsDTO;
import com.mitienda.api_tienda.DTO.VentaRequestDTO;
import com.mitienda.api_tienda.DTO.VentaResponseDTO; // <--- IMPORTANTE
import com.mitienda.api_tienda.Model.*;
import com.mitienda.api_tienda.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class VentaService {

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
    @Autowired
    private ProductoVarianteRepository productoVarianteRepository;
    @Autowired
    private CuponRepository cuponRepository;

    // --- Lógica para CREAR Venta ---
    @Transactional
    public Venta crearVenta(VentaRequestDTO ventaRequest, String username) {

        Usuario usuario = (Usuario) usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ClienteRequestDTO clienteData = ventaRequest.getClienteData();
        String dniBusqueda = clienteData.getDni().trim();
        Optional<Cliente> clienteOpt = clienteRepository.findTopByDni(dniBusqueda);
        Cliente cliente;

        if (clienteOpt.isPresent()) {
            cliente = clienteOpt.get();
        } else {
            cliente = new Cliente();
            cliente.setNombres(clienteData.getNombres());
            cliente.setApellidos(clienteData.getApellidos());
            cliente.setDni(dniBusqueda);
            cliente.setCelular(clienteData.getCelular());
            cliente.setEmail(clienteData.getEmail());
            cliente = clienteRepository.save(cliente);
        }

        Venta nuevaVenta = new Venta();
        nuevaVenta.setUsuario(usuario);
        nuevaVenta.setCliente(cliente);
        nuevaVenta.setTipoComprobante(ventaRequest.getTipoComprobante());

        List<DetalleVenta> detallesGuardados = new ArrayList<>();
        BigDecimal montoTotalCalculado = BigDecimal.ZERO;

        // BUCLE DE PRODUCTOS
        for (VentaRequestDTO.DetalleVentaDTO itemDTO : ventaRequest.getDetalles()) {
            Producto producto = productoRepository.findById(itemDTO.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            ProductoVariante variante = null;
            if (itemDTO.getIdVariante() != null) {
                variante = productoVarianteRepository.findById(itemDTO.getIdVariante())
                        .orElseThrow(() -> new RuntimeException("Variante no encontrada"));

                if (variante.getStockActual() < itemDTO.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente: " + producto.getNombre());
                }
                variante.setStockActual(variante.getStockActual() - itemDTO.getCantidad());
                productoVarianteRepository.save(variante);
                producto.setStockActual(producto.getStockActual() - itemDTO.getCantidad());
                productoRepository.save(producto);
            } else {
                if (producto.getStockActual() < itemDTO.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente: " + producto.getNombre());
                }
                producto.setStockActual(producto.getStockActual() - itemDTO.getCantidad());
                productoRepository.save(producto);
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(itemDTO.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setVariante(variante);

            BigDecimal subtotal = producto.getPrecioVenta().multiply(new BigDecimal(itemDTO.getCantidad()));
            detalle.setSubtotal(subtotal);
            montoTotalCalculado = montoTotalCalculado.add(subtotal);

            detalle.setVenta(nuevaVenta);
            detallesGuardados.add(detalle);
        }

        // --- LÓGICA DE CUPÓN CORREGIDA ---
        BigDecimal descuento = BigDecimal.ZERO;

        if (ventaRequest.getIdCupon() != null) {
            Cupon cupon = cuponRepository.findById(ventaRequest.getIdCupon())
                    .orElseThrow(() -> new RuntimeException("Cupón no encontrado"));

            if (!cupon.isActivo() || cupon.getUsosDisponibles() <= 0) {
                throw new RuntimeException("El cupón ya no es válido");
            }

            if ("FIJO".equals(cupon.getTipoDescuento())) {
                // CORREGIDO: cupon.getValor() ya es BigDecimal, no uses valueOf
                descuento = cupon.getValor();
            } else {
                // CORREGIDO: División con redondeo
                BigDecimal porcentaje = cupon.getValor().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                descuento = montoTotalCalculado.multiply(porcentaje);
            }

            cupon.setUsosDisponibles(cupon.getUsosDisponibles() - 1);
            cuponRepository.save(cupon);

            nuevaVenta.setCupon(cupon);
            nuevaVenta.setMontoDescuento(descuento);
        }

        BigDecimal totalFinal = montoTotalCalculado.subtract(descuento);
        if (totalFinal.compareTo(BigDecimal.ZERO) < 0) totalFinal = BigDecimal.ZERO;

        nuevaVenta.setDetalles(detallesGuardados);

        // CORREGIDO: Usamos setMontoTotal (asegúrate de tenerlo en Venta.java)
        nuevaVenta.setMontoTotal(totalFinal);

        return ventaRepository.save(nuevaVenta);
    }

    // --- MÉTODOS DE CONSULTA ---

    public DashboardMetricsDTO obtenerMetricas() {
        DashboardMetricsDTO metricas = new DashboardMetricsDTO();
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioDia = ahora.with(LocalTime.MIN);
        LocalDateTime finDia = ahora.with(LocalTime.MAX);
        LocalDateTime inicioSemana = ahora.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        LocalDateTime inicioMes = ahora.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);

        metricas.setVentasHoy(ventaRepository.sumMontoTotalBetween(inicioDia, finDia));
        metricas.setCantidadVentasHoy(ventaRepository.countVentasBetween(inicioDia, finDia));
        metricas.setVentasSemana(ventaRepository.sumMontoTotalBetween(inicioSemana, finDia));
        metricas.setVentasMes(ventaRepository.sumMontoTotalBetween(inicioMes, finDia));
        metricas.setIngresosTotales(obtenerIngresosTotales());
        metricas.setTotalProductos(productoRepository.count());
        metricas.setTotalUsuarios(usuarioRepository.count());
        metricas.setTotalClientes(clienteRepository.count());

        return metricas;
    }

    public List<Venta> obtenerTodasLasVentas(String sortBy, String order, String comprobante, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Venta> ventas = ventaRepository.findAllWithDetails();
        Stream<Venta> stream = ventas.stream();

        if (comprobante != null && !comprobante.isEmpty()) {
            stream = stream.filter(v -> v.getTipoComprobante().equalsIgnoreCase(comprobante));
        }
        if (fechaInicio != null) {
            stream = stream.filter(v -> !v.getFechaVenta().toLocalDate().isBefore(fechaInicio));
        }
        if (fechaFin != null) {
            stream = stream.filter(v -> !v.getFechaVenta().toLocalDate().isAfter(fechaFin));
        }

        if (sortBy != null && !sortBy.isEmpty()) {
            Comparator<Venta> comparator = null;
            if (sortBy.equalsIgnoreCase("idVenta")) {
                comparator = Comparator.comparing(Venta::getIdVenta);
            } else if (sortBy.equalsIgnoreCase("fechaVenta")) {
                comparator = Comparator.comparing(Venta::getFechaVenta);
            }
            if (comparator != null) {
                if (order != null && order.equalsIgnoreCase("desc")) {
                    comparator = comparator.reversed();
                }
                stream = stream.sorted(comparator);
            }
        } else {
            stream = stream.sorted(Comparator.comparing(Venta::getFechaVenta).reversed());
        }
        return stream.collect(Collectors.toList());
    }

    public Optional<Venta> obtenerVentaPorId(Integer id) {
        return ventaRepository.findByIdWithDetails(id);
    }

    public List<Venta> obtenerVentasPorUsuario(Integer idUsuario) {
        return ventaRepository.findByUsuarioIdUsuarioWithDetails(idUsuario);
    }

    public List<Venta> obtenerVentasPorCliente(Integer idCliente) {
        return ventaRepository.findByClienteIdClienteWithDetails(idCliente);
    }

    public List<Venta> obtenerVentasPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaVentaBetweenWithDetails(inicio, fin);
    }

    public BigDecimal obtenerIngresosTotales() {
        BigDecimal total = ventaRepository.calcularTotalVentas();
        return (total == null) ? BigDecimal.ZERO : total;
    }

    @Transactional
    public void anularVenta(Integer idVenta) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if ("ANULADA".equals(venta.getEstado())) {
            throw new RuntimeException("La venta ya está anulada.");
        }

        for (DetalleVenta detalle : venta.getDetalles()) {
            if (detalle.getVariante() != null) {
                ProductoVariante variante = productoVarianteRepository.findById(detalle.getVariante().getIdVariante()).orElse(null);
                if (variante != null) {
                    variante.setStockActual(variante.getStockActual() + detalle.getCantidad());
                    productoVarianteRepository.saveAndFlush(variante);
                    Producto padre = variante.getProducto();
                    if(padre != null) {
                        padre.setStockActual(padre.getStockActual() + detalle.getCantidad());
                        productoRepository.saveAndFlush(padre);
                    }
                }
            } else {
                Producto producto = detalle.getProducto();
                producto.setStockActual(producto.getStockActual() + detalle.getCantidad());
                productoRepository.saveAndFlush(producto);
            }
        }
        venta.setEstado("ANULADA");
        ventaRepository.saveAndFlush(venta);
    }

    // --- ¡MÉTODO NUEVO AGREGADO! CONVERTIR A DTO ---
    // --- CONVERTIDOR DTO ---
    // --- CONVERTIDOR DTO ---
    public VentaResponseDTO convertirADTO(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setIdVenta(venta.getIdVenta());
        dto.setFecha(venta.getFechaVenta());
        dto.setTotal(venta.getMontoTotal());
        dto.setEstado(venta.getEstado());
        dto.setTipoComprobante(venta.getTipoComprobante());

        if (venta.getCliente() != null) {
            dto.setNombreCliente(venta.getCliente().getNombres() + " " + venta.getCliente().getApellidos());
            dto.setDniCliente(venta.getCliente().getDni());
            dto.setCelularCliente(venta.getCliente().getCelular());
        }

        if (venta.getCupon() != null) {
            dto.setCodigoCupon(venta.getCupon().getCodigo());
            dto.setMontoDescuento(venta.getMontoDescuento());
        }

        List<VentaResponseDTO.DetalleResponseDTO> detallesDTO = venta.getDetalles().stream().map(d -> {
            VentaResponseDTO.DetalleResponseDTO det = new VentaResponseDTO.DetalleResponseDTO();
            det.setProducto(d.getProducto().getNombre());
            det.setCantidad(d.getCantidad());
            det.setPrecioUnitario(d.getPrecioUnitario());
            det.setSubtotal(d.getSubtotal());

            String codigoTemp = "COD-" + d.getProducto().getIdProducto();

            if(d.getVariante() != null) {
                // Si tiene variante, mostramos Talla/Color
                det.setSku(codigoTemp + " (" + d.getVariante().getTalla() + ")");
            } else {
                det.setSku(codigoTemp);
            }
            return det;
        }).collect(Collectors.toList());

        dto.setDetalles(detallesDTO);
        return dto;
    }
}