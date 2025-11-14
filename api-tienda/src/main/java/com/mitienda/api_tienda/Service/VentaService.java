package com.mitienda.api_tienda.Service;

import com.mitienda.api_tienda.DTO.ClienteRequestDTO;
import com.mitienda.api_tienda.DTO.VentaRequestDTO;
import com.mitienda.api_tienda.Model.*;
import com.mitienda.api_tienda.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: ID " + itemDTO.getIdProducto()));

            // ¡CORRECCIÓN 2: Arreglo del typo 'getCantadad' a 'getCantidad'!
            if (producto.getStockActual() < itemDTO.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            // --- ¡CORRECCIÓN 3: Lógica de Detalle Faltante! ---
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(itemDTO.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta()); // Precio al momento de la venta

            // Calcular subtotal
            BigDecimal subtotal = producto.getPrecioVenta().multiply(new BigDecimal(itemDTO.getCantidad()));
            detalle.setSubtotal(subtotal);

            // Sumar al total
            montoTotalCalculado = montoTotalCalculado.add(subtotal);

            detalle.setVenta(nuevaVenta); // Asociar con la venta principal
            detallesGuardados.add(detalle); // Añadir a la lista

            // (Confiamos en el Trigger de la BD para descontar el stock,
            // así que no lo hacemos aquí en Java)
        }

        nuevaVenta.setDetalles(detallesGuardados);
        nuevaVenta.setMontoTotal(montoTotalCalculado); // Asignamos el total calculado

        // 5. Guardar la Venta y sus Detalles (en cascada)
        return ventaRepository.save(nuevaVenta);
    }

    // --- Lógica para CONSULTAR Ventas ---

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