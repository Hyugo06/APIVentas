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

    // --- LÓGICA DE CREAR MULTI-SUCURSAL ---
    public Producto guardarProducto(ProductoRequestDTO dto) {
        Marca marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con ID: " + dto.getIdMarca()));
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + dto.getIdCategoria()));

        Producto producto = new Producto();
        if (dto.getCodigoSku() != null && !dto.getCodigoSku().trim().isEmpty()) {
            producto.setCodigoSku(dto.getCodigoSku().toUpperCase());
        } else {
            String codigoGenerado = generarSkuAutomatico(categoria, marca);
            producto.setCodigoSku(codigoGenerado);
        }
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecioRegular(dto.getPrecioRegular());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setPrecioCompra(dto.getPrecioCompra());
        producto.setEnOferta(dto.getEnOferta() != null ? dto.getEnOferta() : false);
        producto.setCaracteristicas(dto.getCaracteristicas());
        producto.setMarca(marca);
        producto.setCategoria(categoria);
        producto.setUrlImagen(dto.getUrlImagen());

        // ¡Se eliminó producto.setSucursal() porque ahora es Universal!

        if (dto.getVariantes() != null) {
            List<ProductoVariante> variantesList = dto.getVariantes().stream().map(vDto -> {
                ProductoVariante v = new ProductoVariante();
                v.setColor(vDto.getColor());
                v.setTalla(vDto.getTalla());
                v.setSkuVariante(vDto.getSkuVariante());
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

                // 👇 NUEVO: GUARDAR STOCK POR SUCURSAL 👇
                if (vDto.getInventarios() != null) {
                    List<InventarioSucursal> inventarios = vDto.getInventarios().stream().map(iDto -> {
                        InventarioSucursal inv = new InventarioSucursal();
                        Sucursal s = new Sucursal();
                        s.setIdSucursal(iDto.getIdSucursal());
                        inv.setSucursal(s);
                        inv.setStockActual(iDto.getStockActual() != null ? iDto.getStockActual() : 0);
                        inv.setVariante(v);
                        return inv;
                    }).collect(Collectors.toList());
                    v.setInventarios(inventarios);
                }

                return v;
            }).collect(Collectors.toList());

            producto.setVariantes(variantesList);
        }
        return productoRepository.save(producto);
    }

    private String generarSkuAutomatico(Categoria cat, Marca marca) {
        String inicialesAncestros = "";
        Categoria padre = cat.getCategoriaPadre();
        Categoria abuelo = (padre != null) ? padre.getCategoriaPadre() : null;

        if (abuelo != null && abuelo.getNombre() != null && !abuelo.getNombre().isEmpty()) {
            inicialesAncestros += abuelo.getNombre().substring(0, 1).toUpperCase();
        }
        if (padre != null && padre.getNombre() != null && !padre.getNombre().isEmpty()) {
            inicialesAncestros += padre.getNombre().substring(0, 1).toUpperCase();
        }

        String preCatHijo = (cat.getCodigoCorto() != null) ? cat.getCodigoCorto()
                : cat.getNombre().substring(0, Math.min(cat.getNombre().length(), 3)).toUpperCase();

        String preCatFinal = inicialesAncestros + preCatHijo;

        String preMarca = (marca.getCodigoCorto() != null) ? marca.getCodigoCorto()
                : marca.getNombre().substring(0, Math.min(marca.getNombre().length(), 3)).toUpperCase();

        String prefijo = preCatFinal + "-" + preMarca + "-";

        String ultimoSku = productoRepository.findTopByCodigoSkuStartingWithOrderByIdProductoDesc(prefijo)
                .map(Producto::getCodigoSku)
                .orElse(null);

        int correlativo = 1;
        if (ultimoSku != null) {
            try {
                String numeroStr = ultimoSku.replace(prefijo, "");
                correlativo = Integer.parseInt(numeroStr) + 1;
            } catch (Exception e) {
                correlativo = 1;
            }
        }
        return prefijo + String.format("%03d", correlativo);
    }

    // --- LÓGICA DE ACTUALIZAR MULTI-SUCURSAL ---
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
        }
        productoExistente.setNombre(dto.getNombre());
        productoExistente.setDescripcion(dto.getDescripcion());
        productoExistente.setPrecioRegular(dto.getPrecioRegular());
        productoExistente.setPrecioVenta(dto.getPrecioVenta());
        productoExistente.setPrecioCompra(dto.getPrecioCompra());
        productoExistente.setEnOferta(dto.getEnOferta() != null ? dto.getEnOferta() : false);
        productoExistente.setCaracteristicas(dto.getCaracteristicas());
        productoExistente.setMarca(marca);
        productoExistente.setCategoria(categoria);
        productoExistente.setUrlImagen(dto.getUrlImagen());

        if (dto.getVariantes() != null) {
            List<Integer> idsParaMantener = new ArrayList<>();

            for (ProductoVarianteDTO vDto : dto.getVariantes()) {
                ProductoVariante varianteEntidad = null;

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
                    varianteEntidad.setUrlImagen(vDto.getUrlImagen());
                    actualizarGaleriaVariante(varianteEntidad, vDto.getGaleriaImagenes(), productoExistente);
                    actualizarInventariosVariante(varianteEntidad, vDto.getInventarios());
                    idsParaMantener.add(varianteEntidad.getIdVariante());
                } else {
                    ProductoVariante nueva = new ProductoVariante();
                    nueva.setProducto(productoExistente);
                    nueva.setColor(vDto.getColor());
                    nueva.setTalla(vDto.getTalla());
                    nueva.setSkuVariante(vDto.getSkuVariante());
                    nueva.setUrlImagen(vDto.getUrlImagen());
                    actualizarGaleriaVariante(nueva, vDto.getGaleriaImagenes(), productoExistente);
                    actualizarInventariosVariante(nueva, vDto.getInventarios());
                    productoExistente.getVariantes().add(nueva);
                }
            }
            productoExistente.getVariantes().removeIf(v -> {
                if (v.getIdVariante() == null) return false;
                return !idsParaMantener.contains(v.getIdVariante());
            });
        }

        return Optional.of(productoRepository.save(productoExistente));
    }

    private void actualizarGaleriaVariante(ProductoVariante variante, List<String> nuevasUrls, Producto producto) {
        if (nuevasUrls == null) return;
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

    // 👇 NUEVO: Método para actualizar stock de tiendas sin duplicar
    private void actualizarInventariosVariante(ProductoVariante variante, List<InventarioDTO> nuevosInventarios) {
        if (nuevosInventarios == null) return;
        if (variante.getInventarios() == null) {
            variante.setInventarios(new ArrayList<>());
        } else {
            variante.getInventarios().clear();
        }

        List<InventarioSucursal> nuevosEntidades = nuevosInventarios.stream().map(iDto -> {
            InventarioSucursal inv = new InventarioSucursal();
            Sucursal s = new Sucursal();
            s.setIdSucursal(iDto.getIdSucursal());
            inv.setSucursal(s);
            inv.setStockActual(iDto.getStockActual() != null ? iDto.getStockActual() : 0);
            inv.setVariante(variante);
            return inv;
        }).collect(Collectors.toList());
        variante.getInventarios().addAll(nuevosEntidades);
    }

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

        if (producto.getVariantes() != null) {
            dto.setVariantes(producto.getVariantes().stream().map(this::convertirAVarianteDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    // 👇 NUEVO: Mapear la mochila de inventarios al enviar los datos a Angular
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

        if (v.getInventarios() != null) {
            List<InventarioDTO> invs = v.getInventarios().stream().map(i -> {
                InventarioDTO idto = new InventarioDTO();
                idto.setIdSucursal(i.getSucursal().getIdSucursal());
                idto.setStockActual(i.getStockActual());
                return idto;
            }).collect(Collectors.toList());
            vDto.setInventarios(invs);
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