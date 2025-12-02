package com.mitienda.api_tienda.Service;

import com.mitienda.api_tienda.DTO.ClienteRequestDTO;
import com.mitienda.api_tienda.DTO.DashboardMetricsDTO;
import com.mitienda.api_tienda.DTO.VentaRequestDTO;
import com.mitienda.api_tienda.Model.*;
import com.mitienda.api_tienda.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
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
    @Autowired
    private ProductoVarianteRepository productoVarianteRepository;

    // --- Lógica para CREAR Venta ---
    /**
     * Crea una nueva venta.
     * 1. Obtiene al vendedor desde el token (username).
     * 2. Busca o crea un nuevo cliente usando el DNI.
     * 3. Procesa los detalles, calcula el total y guarda la venta.
     */
    @Transactional
    // ¡CORRECCIÓN 1: La firma del método debe aceptar 'username' desde el controlador!
    public Venta crearVenta(VentaRequestDTO ventaRequest, String username) {

        // 1. OBTENER VENDEDOR (Del Token)
        Usuario usuario = (Usuario) usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario (Vendedor) no encontrado"));

        // 2. OBTENER O CREAR CLIENTE (Lógica "Find or Create")
        ClienteRequestDTO clienteData = ventaRequest.getClienteData();
        Optional<Cliente> clienteOpt = clienteRepository.findByDni(clienteData.getDni());

        Cliente cliente;
        if (clienteOpt.isPresent()) {
            cliente = clienteOpt.get(); // Usamos el cliente existente
        } else {
            // Creamos un cliente nuevo si no existe
            cliente = new Cliente();
            cliente.setNombres(clienteData.getNombres());
            cliente.setApellidos(clienteData.getApellidos());
            cliente.setDni(clienteData.getDni());
            cliente.setCelular(clienteData.getCelular());
            cliente.setEmail(clienteData.getEmail());
            cliente = clienteRepository.save(cliente); // Guardamos el nuevo cliente
        }

        // 3. Crear el objeto Venta principal
        Venta nuevaVenta = new Venta();
        nuevaVenta.setUsuario(usuario);
        nuevaVenta.setCliente(cliente);
        nuevaVenta.setTipoComprobante(ventaRequest.getTipoComprobante());

        List<DetalleVenta> detallesGuardados = new ArrayList<>();
        BigDecimal montoTotalCalculado = BigDecimal.ZERO;

        // 4. Procesar Detalles
        for (VentaRequestDTO.DetalleVentaDTO itemDTO : ventaRequest.getDetalles()) {
            Producto producto = productoRepository.findById(itemDTO.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // --- VALIDACIÓN Y DESCUENTO DE STOCK ---
            ProductoVariante variante = null;
            if (itemDTO.getIdVariante() != null) {
                // A. ES UN PRODUCTO CON VARIANTE (Ej. Zapatilla Talla 10)
                variante = productoVarianteRepository.findById(itemDTO.getIdVariante())
                        .orElseThrow(() -> new RuntimeException("Variante no encontrada"));

                if (variante.getStockActual() < itemDTO.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() + " (" + variante.getTalla() + ")");
                }

                // Descontar stock de la variante
                variante.setStockActual(variante.getStockActual() - itemDTO.getCantidad());
                productoVarianteRepository.save(variante);

                // También descontamos del "total" del padre para mantener coherencia
                producto.setStockActual(producto.getStockActual() - itemDTO.getCantidad());
                productoRepository.save(producto);

            } else {
                // B. ES UN PRODUCTO SIMPLE (Sin variantes)
                if (producto.getStockActual() < itemDTO.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
                }
                producto.setStockActual(producto.getStockActual() - itemDTO.getCantidad());
                productoRepository.save(producto);
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(itemDTO.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setVariante(variante);

            // ... (cálculo de subtotal y guardado en lista sigue igual) ...
            BigDecimal subtotal = producto.getPrecioVenta().multiply(new BigDecimal(itemDTO.getCantidad()));
            detalle.setSubtotal(subtotal);
            montoTotalCalculado = montoTotalCalculado.add(subtotal);
            detalle.setVenta(nuevaVenta);
            detallesGuardados.add(detalle);
        }

        // ... (Guardar venta y retornar sigue igual) ...
        nuevaVenta.setDetalles(detallesGuardados);
        nuevaVenta.setMontoTotal(montoTotalCalculado);
        return ventaRepository.save(nuevaVenta);
    }

    // --- Lógica para CONSULTAR Ventas ---

    public DashboardMetricsDTO obtenerMetricas() {
        DashboardMetricsDTO metricas = new DashboardMetricsDTO();

        LocalDateTime ahora = LocalDateTime.now();

        // 1. Definir rangos de fecha
        LocalDateTime inicioDia = ahora.with(LocalTime.MIN);
        LocalDateTime finDia = ahora.with(LocalTime.MAX);

        // Inicio de la semana (Lunes)
        LocalDateTime inicioSemana = ahora.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);

        // Inicio del mes (Día 1)
        LocalDateTime inicioMes = ahora.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);

        // 2. Consultar Ventas
        metricas.setVentasHoy(ventaRepository.sumMontoTotalBetween(inicioDia, finDia));
        metricas.setCantidadVentasHoy(ventaRepository.countVentasBetween(inicioDia, finDia));

        metricas.setVentasSemana(ventaRepository.sumMontoTotalBetween(inicioSemana, finDia));
        metricas.setVentasMes(ventaRepository.sumMontoTotalBetween(inicioMes, finDia));

        metricas.setIngresosTotales(obtenerIngresosTotales()); // (Ya tenías este método)

        // 3. Consultar Conteos Generales
        metricas.setTotalProductos(productoRepository.count());
        metricas.setTotalUsuarios(usuarioRepository.count());
        metricas.setTotalClientes(clienteRepository.count());

        return metricas;
    }


    /**
     * Obtiene todas las ventas aplicando filtros y ordenamiento.
     * ¡CORRECCIÓN 4: Eliminado el método duplicado!
     */
    public List<Venta> obtenerTodasLasVentas(String sortBy, String order, String comprobante, LocalDate fechaInicio, LocalDate fechaFin) {

        // 1. Obtenemos la lista completa optimizada (anti N+1)
        List<Venta> ventas = ventaRepository.findAllWithDetails();
        Stream<Venta> stream = ventas.stream();

        // 2. Filtro de Comprobante
        if (comprobante != null && !comprobante.isEmpty()) {
            stream = stream.filter(v -> v.getTipoComprobante().equalsIgnoreCase(comprobante));
        }

        // 3. Filtro de Rango de Fechas
        if (fechaInicio != null) {
            stream = stream.filter(v -> !v.getFechaVenta().toLocalDate().isBefore(fechaInicio));
        }
        if (fechaFin != null) {
            stream = stream.filter(v -> !v.getFechaVenta().toLocalDate().isAfter(fechaFin));
        }

        // 4. Ordenamiento
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
            // Orden por defecto: Más recientes primero
            stream = stream.sorted(Comparator.comparing(Venta::getFechaVenta).reversed());
        }

        return stream.collect(Collectors.toList());
    }

    // --- ¡CORRECCIÓN 5: Usar métodos ...WithDetails para evitar bucles! ---

    public Optional<Venta> obtenerVentaPorId(Integer id) {
        return ventaRepository.findByIdWithDetails(id); // <-- CORREGIDO
    }

    public List<Venta> obtenerVentasPorUsuario(Integer idUsuario) {
        return ventaRepository.findByUsuarioIdUsuarioWithDetails(idUsuario); // <-- CORREGIDO
    }

    public List<Venta> obtenerVentasPorCliente(Integer idCliente) {
        return ventaRepository.findByClienteIdClienteWithDetails(idCliente); // <-- CORREGIDO
    }

    public List<Venta> obtenerVentasPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaVentaBetweenWithDetails(inicio, fin); // <-- CORREGIDO
    }

    public BigDecimal obtenerIngresosTotales() {
        BigDecimal total = ventaRepository.calcularTotalVentas(); // (Ahora sí funciona)
        return (total == null) ? BigDecimal.ZERO : total;
    }
}