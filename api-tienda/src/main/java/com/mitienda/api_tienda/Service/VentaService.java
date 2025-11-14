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
    public Venta crearVenta(VentaRequestDTO ventaRequest, String username) {

        // 1. OBTENER VENDEDOR (Del Token, 100% seguro)
        Usuario usuario = (Usuario) usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario (Vendedor) no encontrado"));

        // 2. OBTENER O CREAR CLIENTE (Lógica "Find or Create")
        ClienteRequestDTO clienteData = ventaRequest.getClienteData();

        // Buscamos al cliente por DNI
        Optional<Cliente> clienteOpt = clienteRepository.findByDni(clienteData.getDni());

        Cliente cliente;
        if (clienteOpt.isPresent()) {
            // Si ya existe, lo usamos
            cliente = clienteOpt.get();
        } else {
            // Si no existe, creamos uno nuevo con los datos del formulario
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
        nuevaVenta.setUsuario(usuario); // <-- Vendedor (del token)
        nuevaVenta.setCliente(cliente);  // <-- Cliente (encontrado o creado)
        nuevaVenta.setTipoComprobante(ventaRequest.getTipoComprobante());

        List<DetalleVenta> detallesGuardados = new ArrayList<>();
        BigDecimal montoTotalCalculado = BigDecimal.ZERO;

        for (VentaRequestDTO.DetalleVentaDTO itemDTO : ventaRequest.getDetalles()) {
            Producto producto = productoRepository.findById(itemDTO.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: ID " + itemDTO.getIdProducto()));

            if (producto.getStockActual() < itemDTO.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(itemDTO.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());

            BigDecimal subtotal = producto.getPrecioVenta().multiply(new BigDecimal(itemDTO.getCantidad()));
            detalle.setSubtotal(subtotal);
            montoTotalCalculado = montoTotalCalculado.add(subtotal);

            detalle.setVenta(nuevaVenta);
            detallesGuardados.add(detalle);

            // --- ¡¡ARREGLO DE LÓGICA (CONFLICTO)!! ---
            // Tu trigger de BD ya hace esto. Si dejas estas dos líneas,
            // el stock se descontará DOBLE.
            // Comentamos la lógica de Java y confiamos en el trigger de PostgreSQL.

            // producto.setStockActual(producto.getStockActual() - itemDTO.getCantidad());
            // productoRepository.save(producto);
        }

        nuevaVenta.setDetalles(detallesGuardados);
        nuevaVenta.setMontoTotal(montoTotalCalculado);

        return ventaRepository.save(nuevaVenta);
    }

    // --- ¡¡ARREGLO DE RENDIMIENTO N+1!! ---
    // (Ahora llamamos a los nuevos métodos rápidos)

    public List<Venta> obtenerTodasLasVentas(String sortBy, String order, String comprobante, LocalDate fechaInicio, LocalDate fechaFin) {

        // 1. Obtenemos la lista completa optimizada (anti N+1)
        List<Venta> ventas = ventaRepository.findAllWithDetails();
        Stream<Venta> stream = ventas.stream();

        // 2. Aplicamos Filtro de Comprobante
        if (comprobante != null && !comprobante.isEmpty()) {
            stream = stream.filter(v -> v.getTipoComprobante().equalsIgnoreCase(comprobante));
        }

        // 3. Aplicamos Filtro de Rango de Fechas
        if (fechaInicio != null) {
            stream = stream.filter(v -> !v.getFechaVenta().toLocalDate().isBefore(fechaInicio));
        }
        if (fechaFin != null) {
            stream = stream.filter(v -> !v.getFechaVenta().toLocalDate().isAfter(fechaFin));
        }

        // 4. Aplicamos Ordenamiento (Sorting)
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

        // 5. Devolvemos la lista filtrada y ordenada
        return stream.collect(Collectors.toList());
    }

    public Optional<Venta> obtenerVentaPorId(Integer id) {
        return ventaRepository.findByIdWithDetails(id); // <-- Llama al método rápido
    }

    public List<Venta> obtenerVentasPorUsuario(Integer idUsuario) {
        return ventaRepository.findByUsuarioIdUsuarioWithDetails(idUsuario); // <-- Llama al método rápido
    }

    public List<Venta> obtenerVentasPorCliente(Integer idCliente) {
        return ventaRepository.findByClienteIdClienteWithDetails(idCliente); // <-- Llama al método rápido
    }

    public List<Venta> obtenerVentasPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaVentaBetweenWithDetails(inicio, fin); // <-- Llama al método rápido
    }

    public BigDecimal obtenerIngresosTotales() {
        BigDecimal total = ventaRepository.calcularTotalVentas(); // (Ahora sí funciona)
        return (total == null) ? BigDecimal.ZERO : total;
    }
}