package com.mitienda.api_tienda.Service;

import com.mitienda.api_tienda.DTO.*;
import com.mitienda.api_tienda.Model.*;
import com.mitienda.api_tienda.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
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


    // --- LÓGICA DE CREAR (Sin cambios) ---
    public Producto guardarProducto(ProductoRequestDTO dto) {
        Marca marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con ID: " + dto.getIdMarca()));
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + dto.getIdCategoria()));

        Producto producto = new Producto();
        if (dto.getCodigoSku() != null && !dto.getCodigoSku().trim().isEmpty()) {
            // A. Si el usuario escribió algo, lo respetamos
            producto.setCodigoSku(dto.getCodigoSku().toUpperCase());
        } else {
            // B. Si está vacío, ¡Lo generamos nosotros! 🪄
            String codigoGenerado = generarSkuAutomatico(categoria, marca);
            producto.setCodigoSku(codigoGenerado);
        }
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecioRegular(dto.getPrecioRegular());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setPrecioCompra(dto.getPrecioCompra());
        producto.setEnOferta(dto.getEnOferta() != null ? dto.getEnOferta() : false);
        producto.setStockActual(dto.getStockActual() != null ? dto.getStockActual() : 0);
        producto.setCaracteristicas(dto.getCaracteristicas());
        if (dto.getIdSucursal() != null) {
            Sucursal sucursal = new Sucursal();
            sucursal.setIdSucursal(dto.getIdSucursal());
            producto.setSucursal(sucursal);
        }
        producto.setMarca(marca);
        producto.setCategoria(categoria);
        producto.setUrlImagen(dto.getUrlImagen());

        if (dto.getVariantes() != null) {
            List<ProductoVariante> variantesList = dto.getVariantes().stream().map(vDto -> {
                ProductoVariante v = new ProductoVariante();
                v.setColor(vDto.getColor());
                v.setTalla(vDto.getTalla());
                v.setSkuVariante(vDto.getSkuVariante());
                v.setStockActual(vDto.getStockActual() != null ? vDto.getStockActual() : 0);
                v.setUrlImagen(vDto.getUrlImagen());
                v.setProducto(producto);

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
                int stockTotal = variantesList.stream().mapToInt(ProductoVariante::getStockActual).sum();
                producto.setStockActual(stockTotal);
            }
        }
        return productoRepository.save(producto);
    }

    private String generarSkuAutomatico(Categoria cat, Marca marca) {
        // 1. Escalar el árbol genealógico para buscar al Padre y al Abuelo
        String inicialesAncestros = "";
        Categoria padre = cat.getCategoriaPadre();
        Categoria abuelo = (padre != null) ? padre.getCategoriaPadre() : null;

        // Si tiene abuelo (Ej: "Hombre"), tomamos su primera letra "H"
        if (abuelo != null && abuelo.getNombre() != null && !abuelo.getNombre().isEmpty()) {
            inicialesAncestros += abuelo.getNombre().substring(0, 1).toUpperCase();
        }

        // Si tiene padre (Ej: "Ropa"), tomamos su primera letra "R"
        if (padre != null && padre.getNombre() != null && !padre.getNombre().isEmpty()) {
            inicialesAncestros += padre.getNombre().substring(0, 1).toUpperCase();
        }

        // 2. Obtener Prefijo del Hijo (Ej: "POL")
        String preCatHijo = (cat.getCodigoCorto() != null) ? cat.getCodigoCorto()
                : cat.getNombre().substring(0, Math.min(cat.getNombre().length(), 3)).toUpperCase();

        // 3. Fusionar Ancestros + Hijo (Ej: "HR" + "POL" = "HRPOL")
        String preCatFinal = inicialesAncestros + preCatHijo;

        // 4. Obtener Prefijo de la Marca (Ej: "ADI")
        String preMarca = (marca.getCodigoCorto() != null) ? marca.getCodigoCorto()
                : marca.getNombre().substring(0, Math.min(marca.getNombre().length(), 3)).toUpperCase();

        // Prefijo Base Final: "HRPOL-ADI-"
        String prefijo = preCatFinal + "-" + preMarca + "-";

        // 5. Buscar último correlativo en BD para ESTE nuevo prefijo
        String ultimoSku = productoRepository.findTopByCodigoSkuStartingWithOrderByIdProductoDesc(prefijo)
                .map(Producto::getCodigoSku)
                .orElse(null);

        int correlativo = 1; // Empezamos en 1 por defecto

        if (ultimoSku != null) {
            try {
                // Si encontramos "HRPOL-ADI-004", extraemos el "004"
                String numeroStr = ultimoSku.replace(prefijo, "");
                correlativo = Integer.parseInt(numeroStr) + 1; // Siguiente: 5
            } catch (Exception e) {
                correlativo = 1;
            }
        }

        // 6. Formatear a 3 dígitos (Ej: 1 -> "001")
        return prefijo + String.format("%03d", correlativo);
    }

    // --- LÓGICA DE ACTUALIZAR (¡CORREGIDA Y BLINDADA!) ---
    public Optional<Producto> actualizarProducto(Integer id, ProductoRequestDTO dto) {

        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isEmpty()) {
            return Optional.empty();
        }

        Marca marca = marcaRepository.findById(dto.getIdMarca()).orElseThrow(() -> new RuntimeException("Marca no encontrada"));
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria()).orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Producto productoExistente = productoOpt.get();
        if (dto.getCodigoSku() != null && !dto.getCodigoSku().trim().isEmpty()) {
            productoExistente.setCodigoSku(dto.getCodigoSku().toUpperCase());
        } else {
            String codigoGenerado = generarSkuAutomatico(categoria, marca);
            productoExistente.setCodigoSku(codigoGenerado);
        }
        productoExistente.setNombre(dto.getNombre());
        productoExistente.setDescripcion(dto.getDescripcion());
        productoExistente.setPrecioRegular(dto.getPrecioRegular());
        productoExistente.setPrecioVenta(dto.getPrecioVenta());
        productoExistente.setPrecioCompra(dto.getPrecioCompra());
        productoExistente.setEnOferta(dto.getEnOferta() != null ? dto.getEnOferta() : false);
        productoExistente.setCaracteristicas(dto.getCaracteristicas());
        if (dto.getIdSucursal() != null) {
            Sucursal sucursal = new Sucursal();
            sucursal.setIdSucursal(dto.getIdSucursal());
            productoExistente.setSucursal(sucursal);
        }
        productoExistente.setMarca(marca);
        productoExistente.setCategoria(categoria);
        productoExistente.setUrlImagen(dto.getUrlImagen());

        // Si no vienen variantes en el DTO, actualizamos el stock manual
        if (dto.getVariantes() == null || dto.getVariantes().isEmpty()) {
            productoExistente.setStockActual(dto.getStockActual() != null ? dto.getStockActual() : 0);
        }

        // --- INICIO: Lógica Inteligente de Variantes ---
        if (dto.getVariantes() != null) {

            // Lista para saber qué IDs seguimos usando (para no borrarlos por error)
            List<Integer> idsParaMantener = new ArrayList<>();

            for (ProductoVarianteDTO vDto : dto.getVariantes()) {

                ProductoVariante varianteEntidad = null;

                // 1. Buscamos si la variante YA EXISTE en la base de datos (por su ID)
                if (vDto.getIdVariante() != null) {
                    varianteEntidad = productoExistente.getVariantes().stream()
                            .filter(v -> v.getIdVariante().equals(vDto.getIdVariante()))
                            .findFirst()
                            .orElse(null);
                }

                if (varianteEntidad != null) {
                    varianteEntidad.setColor(vDto.getColor());
                    varianteEntidad.setTalla(vDto.getTalla());
                    varianteEntidad.setSkuVariante(vDto.getSkuVariante());
                    varianteEntidad.setStockActual(vDto.getStockActual() != null ? vDto.getStockActual() : 0);
                    varianteEntidad.setUrlImagen(vDto.getUrlImagen());
                    actualizarGaleriaVariante(varianteEntidad, vDto.getGaleriaImagenes(), productoExistente);
                    idsParaMantener.add(varianteEntidad.getIdVariante());
                } else {
                    ProductoVariante nueva = new ProductoVariante();
                    nueva.setProducto(productoExistente);
                    nueva.setColor(vDto.getColor());
                    nueva.setTalla(vDto.getTalla());
                    nueva.setSkuVariante(vDto.getSkuVariante());
                    nueva.setStockActual(vDto.getStockActual() != null ? vDto.getStockActual() : 0);
                    nueva.setUrlImagen(vDto.getUrlImagen());
                    actualizarGaleriaVariante(nueva, vDto.getGaleriaImagenes(), productoExistente);
                    productoExistente.getVariantes().add(nueva);
                }
            }
            productoExistente.getVariantes().removeIf(v -> {
                if (v.getIdVariante() == null) return false; // Las nuevas no se borran
                return !idsParaMantener.contains(v.getIdVariante());
            });

            // 3. Recalcular Stock Total Sumado
            int stockTotal = productoExistente.getVariantes().stream()
                    .mapToInt(ProductoVariante::getStockActual)
                    .sum();
            productoExistente.setStockActual(stockTotal);
        }
        // --- FIN: Lógica Inteligente ---

        return Optional.of(productoRepository.save(productoExistente));
    }

    // --- NUEVO MÉTODO AUXILIAR PARA NO REPETIR CÓDIGO ---
    private void actualizarGaleriaVariante(ProductoVariante variante, List<String> nuevasUrls, Producto producto) {
        if (nuevasUrls == null) return;

        // Las imágenes SÍ podemos borrarlas y crearlas de nuevo porque no tienen ventas asociadas
        if (variante.getImagenes() == null) {
            variante.setImagenes(new ArrayList<>());
        } else {
            variante.getImagenes().clear();
        }

        List<ImagenProducto> nuevasEntidades = nuevasUrls.stream().map(url -> {
            ImagenProducto img = new ImagenProducto();
            img.setUrlImagen(url);
            img.setDescripcionAlt(producto.getNombre() + " - " + variante.getColor());
            img.setOrden(0);
            img.setVariante(variante);
            img.setProducto(producto);
            return img;
        }).collect(Collectors.toList());

        variante.getImagenes().addAll(nuevasEntidades);
    }

    // --- LÓGICA DE CONSULTA (Igual que antes) ---

    public List<Producto> obtenerTodos(String search, String categoriaNombre) {
        return productoRepository.findAllWithDetailsAndFilters(search, categoriaNombre);
    }

    public Optional<Producto> obtenerPorId(Integer id) {
        return productoRepository.findByIdWithDetails(id);
    }

    public void eliminarProducto(Integer id) {
        productoRepository.deleteById(id);
    }

    public List<ImagenDTO> obtenerImagenesPorProducto(Integer idProducto) {
        if (!productoRepository.existsById(idProducto)) {
            throw new RuntimeException("Producto no encontrado con ID: " + idProducto);
        }
        List<ImagenProducto> imagenes = imagenProductoRepository.findByProductoIdProducto(idProducto);
        return imagenes.stream()
                .map(this::convertirAImagenDTO)
                .collect(Collectors.toList());
    }

    // --- MAPEADORES (Igual que antes) ---

    public ProductoPublicoDTO convertirAPublicoDTO(Producto producto) {
        if (producto == null) return null;
        ProductoPublicoDTO dto = new ProductoPublicoDTO();
        dto.setIdProducto(producto.getIdProducto());
        dto.setCodigoSku(producto.getCodigoSku());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setEnOferta(producto.getEnOferta());
        dto.setPrecioRegular(producto.getPrecioRegular());
        dto.setPrecioVenta(producto.getPrecioVenta());
        dto.setStockActual(producto.getStockActual());
        dto.setUrlImagen(producto.getUrlImagen());
        dto.setMarca(convertirAMarcaDTO(producto.getMarca()));
        dto.setCategoria(convertirACategoriaDTO(producto.getCategoria()));
        dto.setCaracteristicas(producto.getCaracteristicas());
        if (producto.getVariantes() != null) {
            dto.setVariantes(producto.getVariantes().stream()
                    .map(this::convertirAVarianteDTO)
                    .collect(Collectors.toList()));
        }
        if (producto.getSucursal() != null) {
            java.util.Map<String, Integer> sucursalMap = new java.util.HashMap<>();
            sucursalMap.put("idSucursal", producto.getSucursal().getIdSucursal());
            dto.setSucursal(sucursalMap);
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
        dto.setEnOferta(producto.getEnOferta());
        dto.setStockActual(producto.getStockActual());
        dto.setCaracteristicas(producto.getCaracteristicas());
        dto.setMarca(convertirAMarcaDTO(producto.getMarca()));
        dto.setCategoria(convertirACategoriaDTO(producto.getCategoria()));
        dto.setPrecioCompra(producto.getPrecioCompra());
        dto.setUrlImagen(producto.getUrlImagen());

        if (producto.getSucursal() != null) {
            java.util.Map<String, Integer> sucursalMap = new java.util.HashMap<>();
            sucursalMap.put("idSucursal", producto.getSucursal().getIdSucursal());
            dto.setSucursal(sucursalMap);
        }

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
        dto.setCodigoCorto(categoria.getCodigoCorto());
          if (categoria.getCategoriaPadre() != null) {
            dto.setCategoriaPadre(convertirACategoriaDTO(categoria.getCategoriaPadre()));
        }
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