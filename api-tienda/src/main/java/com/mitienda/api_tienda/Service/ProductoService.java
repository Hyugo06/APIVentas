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
        Marca marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con ID: " + dto.getIdMarca()));
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + dto.getIdCategoria()));

        Producto producto = new Producto();
        producto.setCodigoSku(dto.getCodigoSku());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecioRegular(dto.getPrecioRegular());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setPrecioCompra(dto.getPrecioCompra());
        // Inicializamos en 0, se sobrescribe si hay variantes
        producto.setStockActual(dto.getStockActual() != null ? dto.getStockActual() : 0);
        producto.setCaracteristicas(dto.getCaracteristicas());
        producto.setMarca(marca);
        producto.setCategoria(categoria);
        producto.setUrlImagen(dto.getUrlImagen());

        // --- LÓGICA DE VARIANTES (CREAR) ---
        if (dto.getVariantes() != null) {
            List<ProductoVariante> variantesList = dto.getVariantes().stream().map(vDto -> {
                ProductoVariante v = new ProductoVariante();
                v.setColor(vDto.getColor());
                v.setTalla(vDto.getTalla());
                v.setSkuVariante(vDto.getSkuVariante());

                // --- ¡PROTECCIÓN CONTRA NULOS! ---
                v.setStockActual(vDto.getStockActual() != null ? vDto.getStockActual() : 0);
                // --------------------------------

                v.setUrlImagen(vDto.getUrlImagen());
                v.setProducto(producto);

                // Guardar galería de la variante
                if (vDto.getGaleriaImagenes() != null) {
                    List<ImagenProducto> galeria = vDto.getGaleriaImagenes().stream().map(url -> {
                        ImagenProducto img = new ImagenProducto();
                        img.setUrlImagen(url);
                        img.setDescripcionAlt(producto.getNombre() + " - " + v.getColor());
                        img.setOrden(0);
                        img.setVariante(v);
                        img.setProducto(producto);
                        return img;
                    }).collect(Collectors.toList());
                    v.setImagenes(galeria);
                }

                return v;
            }).collect(Collectors.toList());

            producto.setVariantes(variantesList);

            if (!variantesList.isEmpty()) {
                // Ahora es seguro sumar porque garantizamos que no hay nulls
                int stockTotal = variantesList.stream().mapToInt(ProductoVariante::getStockActual).sum();
                producto.setStockActual(stockTotal);
            }
        }

        return productoRepository.save(producto);
    }

    // --- LÓGICA DE ACTUALIZAR ---
    public Optional<Producto> actualizarProducto(Integer id, ProductoRequestDTO dto) {

        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isEmpty()) {
            return Optional.empty();
        }

        Marca marca = marcaRepository.findById(dto.getIdMarca()).orElseThrow(() -> new RuntimeException("Marca no encontrada"));
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria()).orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Producto productoExistente = productoOpt.get();
        productoExistente.setCodigoSku(dto.getCodigoSku());
        productoExistente.setNombre(dto.getNombre());
        productoExistente.setDescripcion(dto.getDescripcion());
        productoExistente.setPrecioRegular(dto.getPrecioRegular());
        productoExistente.setPrecioVenta(dto.getPrecioVenta());
        productoExistente.setPrecioCompra(dto.getPrecioCompra());

        if (dto.getVariantes() == null || dto.getVariantes().isEmpty()) {
            productoExistente.setStockActual(dto.getStockActual() != null ? dto.getStockActual() : 0);
        }

        productoExistente.setCaracteristicas(dto.getCaracteristicas());
        productoExistente.setMarca(marca);
        productoExistente.setCategoria(categoria);
        productoExistente.setUrlImagen(dto.getUrlImagen());

        // --- LÓGICA DE VARIANTES (ACTUALIZAR) ---
        if (dto.getVariantes() != null) {
            productoExistente.getVariantes().clear();

            List<ProductoVariante> nuevasVariantes = dto.getVariantes().stream().map(vDto -> {
                ProductoVariante v = new ProductoVariante();
                v.setColor(vDto.getColor());
                v.setTalla(vDto.getTalla());
                v.setSkuVariante(vDto.getSkuVariante());

                // --- ¡PROTECCIÓN CONTRA NULOS! ---
                v.setStockActual(vDto.getStockActual() != null ? vDto.getStockActual() : 0);
                // --------------------------------

                v.setUrlImagen(vDto.getUrlImagen());
                v.setProducto(productoExistente);

                if (vDto.getGaleriaImagenes() != null) {
                    List<ImagenProducto> galeria = vDto.getGaleriaImagenes().stream().map(url -> {
                        ImagenProducto img = new ImagenProducto();
                        img.setUrlImagen(url);
                        img.setDescripcionAlt(productoExistente.getNombre() + " - " + v.getColor());
                        img.setOrden(0);
                        img.setVariante(v);
                        img.setProducto(productoExistente);
                        return img;
                    }).collect(Collectors.toList());
                    v.setImagenes(galeria);
                }

                return v;
            }).collect(Collectors.toList());

            productoExistente.getVariantes().addAll(nuevasVariantes);

            if (!nuevasVariantes.isEmpty()) {
                int stockTotal = nuevasVariantes.stream().mapToInt(ProductoVariante::getStockActual).sum();
                productoExistente.setStockActual(stockTotal);
            }
        }

        return Optional.of(productoRepository.save(productoExistente));
    }

    // --- LÓGICA DE CONSULTA ---

    public List<Producto> obtenerTodos(String search, String categoriaNombre) {
        return productoRepository.findAllWithDetailsAndFilters(search, categoriaNombre);
    }

    public Optional<Producto> obtenerPorId(Integer id) {
        return productoRepository.findByIdWithDetails(id);
    }

    public void eliminarProducto(Integer id) {
        productoRepository.deleteById(id);
    }

    // --- LÓGICA DE IMÁGENES (Antigua) ---
    public List<ImagenDTO> obtenerImagenesPorProducto(Integer idProducto) {
        if (!productoRepository.existsById(idProducto)) {
            throw new RuntimeException("Producto no encontrado con ID: " + idProducto);
        }
        List<ImagenProducto> imagenes = imagenProductoRepository.findByProductoIdProducto(idProducto);
        return imagenes.stream()
                .map(this::convertirAImagenDTO)
                .collect(Collectors.toList());
    }

    // --- MAPEADORES ---

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
        dto.setUrlImagen(producto.getUrlImagen());

        if (producto.getVariantes() != null) {
            dto.setVariantes(producto.getVariantes().stream().map(this::convertirAVarianteDTO).collect(Collectors.toList()));
        }

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
        dto.setPrecioCompra(producto.getPrecioCompra());
        dto.setUrlImagen(producto.getUrlImagen());

        if (producto.getVariantes() != null) {
            dto.setVariantes(producto.getVariantes().stream().map(this::convertirAVarianteDTO).collect(Collectors.toList()));
        }

        return dto;
    }

    private ProductoVarianteDTO convertirAVarianteDTO(ProductoVariante v) {
        ProductoVarianteDTO vDto = new ProductoVarianteDTO();
        vDto.setIdVariante(v.getIdVariante());
        vDto.setColor(v.getColor());
        vDto.setTalla(v.getTalla());
        vDto.setSkuVariante(v.getSkuVariante());
        vDto.setStockActual(v.getStockActual());
        vDto.setUrlImagen(v.getUrlImagen());
        if (v.getImagenes() != null) {
            vDto.setGaleriaImagenes(v.getImagenes().stream()
                    .map(ImagenProducto::getUrlImagen)
                    .collect(Collectors.toList()));
        }
        return vDto;
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