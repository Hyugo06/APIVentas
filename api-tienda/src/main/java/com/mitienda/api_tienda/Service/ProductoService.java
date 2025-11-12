package com.mitienda.api_tienda.Service;

import com.mitienda.api_tienda.DTO.*; // Importa los nuevos DTOs
import com.mitienda.api_tienda.Model.Categoria;
import com.mitienda.api_tienda.Model.ImagenProducto;
import com.mitienda.api_tienda.Model.Marca;
import com.mitienda.api_tienda.Model.Producto;
import com.mitienda.api_tienda.Repository.CategoriaRepository;
import com.mitienda.api_tienda.Repository.ImagenProductoRepository;
import com.mitienda.api_tienda.Repository.MarcaRepository;
import com.mitienda.api_tienda.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Importante para las listas

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private MarcaRepository marcaRepository; // Necesario para buscar la marca
    @Autowired
    private CategoriaRepository categoriaRepository; // Necesario para buscar la categoría
    @Autowired
    private ImagenProductoRepository imagenProductoRepository;


    // --- MÉTODOS DE CREACIÓN/ACTUALIZACIÓN (Ahora usan DTO) ---

    public Producto guardarProducto(ProductoRequestDTO dto) {
        // 1. Validar y buscar las relaciones
        Marca marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 2. Construir la entidad Producto
        Producto producto = new Producto();
        producto.setCodigoSku(dto.getCodigoSku());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecioRegular(dto.getPrecioRegular());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setPrecioCompra(dto.getPrecioCompra()); // Guardamos el costo
        producto.setStockActual(dto.getStockActual());
        producto.setCaracteristicas(dto.getCaracteristicas());
        producto.setMarca(marca);
        producto.setCategoria(categoria);

        return productoRepository.save(producto);
    }

    public Optional<Producto> actualizarProducto(Integer id, ProductoRequestDTO dto) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isEmpty()) {
            return Optional.empty();
        }

        // Validar relaciones
        Marca marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Producto producto = productoOpt.get();
        // Actualizar todos los campos
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

        return Optional.of(productoRepository.save(producto));
    }

    // --- MÉTODOS DE CONSULTA (Devuelven la Entidad, el Controlador mapeará) ---

    public Optional<Producto> obtenerPorId(Integer id) {
        return productoRepository.findById(id);
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public void eliminarProducto(Integer id) {
        productoRepository.deleteById(id);
    }

    // --- MAPEADORES (La magia de conversión) ---

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

        // ¡La diferencia clave!
        dto.setPrecioCompra(producto.getPrecioCompra());
        return dto;
    }

    // Mapeadores de helpers
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

    public List<ImagenDTO> obtenerImagenesPorProducto(Integer idProducto) {
        // 1. Verificamos que el producto exista
        if (!productoRepository.existsById(idProducto)) {
            // Si no existe, lanzamos una excepción
            throw new RuntimeException("Producto no encontrado con ID: " + idProducto);
        }

        // 2. Buscamos las entidades de imagen en la BD
        List<ImagenProducto> imagenes = imagenProductoRepository.findByProductoIdProducto(idProducto);

        // 3. Convertimos la lista de entidades a una lista de DTOs y la devolvemos
        return imagenes.stream()
                .map(this::convertirAImagenDTO)
                .collect(Collectors.toList());
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