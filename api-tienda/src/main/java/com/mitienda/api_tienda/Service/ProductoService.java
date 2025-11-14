package com.mitienda.api_tienda.Service;

import com.mitienda.api_tienda.DTO.*;
import com.mitienda.api_tienda.Model.*;
import com.mitienda.api_tienda.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    // --- REPOSITORIOS INYECTADOS ---
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private MarcaRepository marcaRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ImagenProductoRepository imagenProductoRepository;


    // --- LÓGICA DE CREAR ---
    public Producto guardarProducto(ProductoRequestDTO dto) {
        // Valida que la marca y categoría existan
        Marca marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con ID: " + dto.getIdMarca()));
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + dto.getIdCategoria()));

        Producto producto = new Producto();
        // Seteamos todos los campos desde el DTO
        producto.setCodigoSku(dto.getCodigoSku());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecioRegular(dto.getPrecioRegular());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setPrecioCompra(dto.getPrecioCompra());
        producto.setStockActual(dto.getStockActual());
        producto.setCaracteristicas(dto.getCaracteristicas());
        producto.setMarca(marca);
        producto.setCategoria(categoria);

        return productoRepository.save(producto);
    }

    // --- ¡¡AQUÍ ESTÁ LA LÓGICA COMPLETA!! ---
    public Optional<Producto> actualizarProducto(Integer id, ProductoRequestDTO dto) {

        // 1. Buscar el producto existente
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isEmpty()) {
            return Optional.empty(); // No se encontró, devuelve vacío
        }

        // 2. Validar las relaciones (Marca y Categoría)
        Marca marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con ID: " + dto.getIdMarca()));
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + dto.getIdCategoria()));

        // 3. Obtener la entidad y actualizar todos sus campos
        Producto productoExistente = productoOpt.get();
        productoExistente.setCodigoSku(dto.getCodigoSku());
        productoExistente.setNombre(dto.getNombre());
        productoExistente.setDescripcion(dto.getDescripcion());
        productoExistente.setPrecioRegular(dto.getPrecioRegular());
        productoExistente.setPrecioVenta(dto.getPrecioVenta());
        productoExistente.setPrecioCompra(dto.getPrecioCompra());
        productoExistente.setStockActual(dto.getStockActual());
        productoExistente.setCaracteristicas(dto.getCaracteristicas());
        productoExistente.setMarca(marca);
        productoExistente.setCategoria(categoria);

        // 4. Guardar la entidad actualizada y devolverla
        return Optional.of(productoRepository.save(productoExistente));
    }


    // --- LÓGICA DE CONSULTA (Corregida) ---

    public List<Producto> obtenerTodos(String search, String categoriaNombre) {
        // Llama al nuevo método del repositorio con los filtros
        return productoRepository.findAllWithDetailsAndFilters(search, categoriaNombre);
    }

    public Optional<Producto> obtenerPorId(Integer id) {
        return productoRepository.findByIdWithDetails(id);
    }

    public void eliminarProducto(Integer id) {
        productoRepository.deleteById(id);
    }

    // --- LÓGICA DE IMÁGENES ---

    public List<ImagenDTO> obtenerImagenesPorProducto(Integer idProducto) {
        if (!productoRepository.existsById(idProducto)) {
            throw new RuntimeException("Producto no encontrado con ID: " + idProducto);
        }
        List<ImagenProducto> imagenes = imagenProductoRepository.findByProductoIdProducto(idProducto);
        return imagenes.stream()
                .map(this::convertirAImagenDTO)
                .collect(Collectors.toList());
    }

    // --- MAPEADORES (Completados) ---

    public ProductoPublicoDTO convertirAPublicoDTO(Producto producto) {
        ProductoPublicoDTO dto = new ProductoPublicoDTO();
        dto.setIdProducto(producto.getIdProducto());
        dto.setCodigoSku(producto.getCodigoSku());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecioRegular(producto.getPrecioRegular());
        dto.setPrecioVenta(producto.getPrecioVenta());
        dto.setStockActual(producto.getStockActual());
        dto.setCaracteristicas(producto.getCaracteristicas());
        dto.setMarca(convertirAMarcaDTO(producto.getMarca()));
        dto.setCategoria(convertirACategoriaDTO(producto.getCategoria()));
        return dto;
    }

    public ProductoAdminDTO convertirAAdminDTO(Producto producto) {
        ProductoAdminDTO dto = new ProductoAdminDTO();

        // --- ¡LÓGICA COMPLETADA! ---
        dto.setIdProducto(producto.getIdProducto());
        dto.setCodigoSku(producto.getCodigoSku());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecioRegular(producto.getPrecioRegular());
        dto.setPrecioVenta(producto.getPrecioVenta());
        dto.setStockActual(producto.getStockActual());
        dto.setCaracteristicas(producto.getCaracteristicas());
        dto.setMarca(convertirAMarcaDTO(producto.getMarca()));
        dto.setCategoria(convertirACategoriaDTO(producto.getCategoria()));
        // El campo clave de admin:
        dto.setPrecioCompra(producto.getPrecioCompra());

        return dto;
    }

    private MarcaDTO convertirAMarcaDTO(Marca marca) {
        if (marca == null) return null;
        MarcaDTO dto = new MarcaDTO();
        dto.setIdMarca(marca.getIdMarca());
        dto.setNombre(marca.getNombre());
        return dto;
    }

    private CategoriaDTO convertirACategoriaDTO(Categoria categoria) {
        if (categoria == null) return null;
        CategoriaDTO dto = new CategoriaDTO();
        dto.setIdCategoria(categoria.getIdCategoria());
        dto.setNombre(categoria.getNombre());
        return dto;
    }

    private ImagenDTO convertirAImagenDTO(ImagenProducto imagen) {
        ImagenDTO dto = new ImagenDTO();
        dto.setIdImagen(imagen.getIdImagen());
        dto.setUrlImagen(imagen.getUrlImagen());
        dto.setDescripcionAlt(imagen.getDescripcionAlt());
        dto.setOrden(imagen.getOrden());
        return dto;
    }
}