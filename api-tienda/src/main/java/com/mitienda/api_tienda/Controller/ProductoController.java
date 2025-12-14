package com.mitienda.api_tienda.Controller;

import com.mitienda.api_tienda.DTO.*;
import com.mitienda.api_tienda.Model.ImagenProducto;
import com.mitienda.api_tienda.Model.Producto;
import com.mitienda.api_tienda.Repository.ImagenProductoRepository;
import com.mitienda.api_tienda.Service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mitienda.api_tienda.DTO.ImagenDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
// ¡OJO! Quitamos el RequestMapping de aquí para ponerlo en cada método
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ImagenProductoRepository imagenProductoRepository;

    // --- ENDPOINTS PÚBLICOS (NO MUESTRAN PRECIO DE COMPRA) ---

    @GetMapping("/api/productos")
    public List<ProductoPublicoDTO> obtenerTodosPublico(
            // Añadimos los parámetros de consulta (no son obligatorios)
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoria
    ) {
        return productoService.obtenerTodos(search, categoria) // Se los pasamos al servicio
                .stream()
                .map(productoService::convertirAPublicoDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/productos/{id}")
    public ResponseEntity<ProductoPublicoDTO> obtenerPorIdPublico(@PathVariable Integer id) {
        return productoService.obtenerPorId(id)
                .map(productoService::convertirAPublicoDTO) // Convierte a DTO público
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- ENDPOINTS DE ADMIN (SÍ MUESTRAN PRECIO DE COMPRA) ---
    // (Estos son los que protegerías con Spring Security más adelante)

    @GetMapping("/api/admin/productos")
    public List<ProductoAdminDTO> obtenerTodosAdmin(
            @RequestParam(required = false) String search
    ) {
        // CORRECCIÓN: Pasamos 'search' al servicio
        return productoService.obtenerTodos(search, null)
                .stream()
                .map(productoService::convertirAAdminDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/admin/productos/{id}")
    public ResponseEntity<ProductoAdminDTO> obtenerPorIdAdmin(@PathVariable Integer id) {
        return productoService.obtenerPorId(id)
                .map(productoService::convertirAAdminDTO) // Convierte a DTO de Admin
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- ENDPOINTS DE CREACIÓN/ACTUALIZACIÓN ---
    // (Estos también deberían ser solo para ADMIN)

    @PostMapping("/api/admin/productos")
    public ResponseEntity<ProductoAdminDTO> crearProducto(
            @Valid @RequestBody ProductoRequestDTO dto) {

        Producto productoGuardado = productoService.guardarProducto(dto);
        // Devolvemos el DTO de Admin para confirmar
        return new ResponseEntity<>(
                productoService.convertirAAdminDTO(productoGuardado),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/api/admin/productos/{id}")
    public ResponseEntity<ProductoAdminDTO> actualizarProducto(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoRequestDTO dto) {

        return productoService.actualizarProducto(id, dto)
                .map(productoService::convertirAAdminDTO) // Convierte a DTO de Admin
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/admin/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/productos/{idProducto}/imagenes")
    public ResponseEntity<ImagenProducto> agregarImagenAProducto(
            @PathVariable Integer idProducto,
            @Valid @RequestBody ImagenRequestDTO imagenRequest) {

        // 1. Buscar el producto (usamos el servicio)
        Optional<Producto> productoOpt = productoService.obtenerPorId(idProducto);
        if (productoOpt.isEmpty()) {
            return ResponseEntity.notFound().build(); // Devuelve 404 si el producto no existe
        }

        // 2. Crear el nuevo objeto Imagen
        ImagenProducto nuevaImagen = new ImagenProducto();
        nuevaImagen.setProducto(productoOpt.get()); // Asocia el producto
        nuevaImagen.setUrlImagen(imagenRequest.getUrlImagen());
        nuevaImagen.setDescripcionAlt(imagenRequest.getDescripcionAlt());
        nuevaImagen.setOrden(imagenRequest.getOrden());

        // 3. Guardar la imagen en la BD
        ImagenProducto imagenGuardada = imagenProductoRepository.save(nuevaImagen);

        return new ResponseEntity<>(imagenGuardada, HttpStatus.CREATED);
    }

    @GetMapping("/api/productos/{idProducto}/imagenes")
    public ResponseEntity<?> obtenerImagenesDeProducto(@PathVariable Integer idProducto) {
        try {
            // Llama al servicio para obtener la lista de DTOs
            List<ImagenDTO> imagenes = productoService.obtenerImagenesPorProducto(idProducto);
            // Devuelve 200 OK y la lista
            return ResponseEntity.ok(imagenes);
        } catch (RuntimeException e) {
            // Si el servicio lanzó la excepción (Producto no encontrado), devolvemos 404
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/api/admin/imagenes/{id}")
    public ResponseEntity<Void> eliminarImagen(@PathVariable Integer id) {
        Optional<ImagenProducto> imagenOpt = imagenProductoRepository.findById(id);

        if (imagenOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ImagenProducto imagen = imagenOpt.get();

        // 1. Borrar archivo del disco (Opcional pero recomendado)
        try {
            // Necesitas inyectar StorageService en este controlador si no está
            // @Autowired private StorageService storageService;
            // storageService.delete(imagen.getUrlImagen());
        } catch (Exception e) {
            System.err.println("No se pudo borrar el archivo físico: " + e.getMessage());
        }

        // 2. Borrar registro de la base de datos
        imagenProductoRepository.delete(imagen);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/variantes/{idVariante}/imagenes")
    public ResponseEntity<ImagenProducto> agregarImagenAVariante(
            @PathVariable Integer idVariante,
            @Valid @RequestBody ImagenRequestDTO imagenRequest) {
        return ResponseEntity.ok(null); // (Placeholder hasta implementar el servicio)
    }
}