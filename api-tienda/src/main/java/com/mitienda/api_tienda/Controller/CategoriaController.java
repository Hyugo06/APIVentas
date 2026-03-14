package com.mitienda.api_tienda.Controller;

import com.mitienda.api_tienda.DTO.CategoriaDTO;
import com.mitienda.api_tienda.Model.Categoria;
import com.mitienda.api_tienda.Repository.CategoriaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api") // Prefijo /api
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // --- ENDPOINT PÚBLICO (Para filtros de tienda) ---
    @GetMapping("/categorias")
    public List<CategoriaDTO> obtenerTodasLasCategorias() {
        return categoriaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // --- ENDPOINTS DE ADMIN ---
    @GetMapping("/admin/categorias")
    public List<CategoriaDTO> obtenerTodasLasCategoriasAdmin() {
        return categoriaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/admin/categorias")
    public ResponseEntity<?> crearCategoria(@Valid @RequestBody Categoria categoria) {
        // Validación estricta de los 3 niveles
        if (categoria.getCategoriaPadre() != null) {
            Categoria padre = categoriaRepository.findById(categoria.getCategoriaPadre().getIdCategoria()).orElse(null);

            if (padre != null && padre.getCategoriaPadre() != null) {
                Categoria abuelo = categoriaRepository.findById(padre.getCategoriaPadre().getIdCategoria()).orElse(null);

                // Si el abuelo también tiene un padre registrado, es un 4to nivel
                if (abuelo != null && abuelo.getCategoriaPadre() != null) {
                    return ResponseEntity.badRequest().body("Error: La estructura solo permite 3 fases (Abuelo > Padre > Hijo).");
                }
            }
        }

        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        return new ResponseEntity<>(categoriaGuardada, HttpStatus.CREATED);
    }

    @GetMapping("/admin/categorias/{id}")
    public ResponseEntity<Categoria> obtenerCategoriaPorId(@PathVariable Integer id) {
        return categoriaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/admin/categorias/{id}")
    public ResponseEntity<?> actualizarCategoria(@PathVariable Integer id, @Valid @RequestBody Categoria categoriaDetalles) {

        // Validación estricta para que no muevan una categoría a un 4to nivel
        if (categoriaDetalles.getCategoriaPadre() != null) {
            Categoria padre = categoriaRepository.findById(categoriaDetalles.getCategoriaPadre().getIdCategoria()).orElse(null);
            if (padre != null && padre.getCategoriaPadre() != null) {
                Categoria abuelo = categoriaRepository.findById(padre.getCategoriaPadre().getIdCategoria()).orElse(null);
                if (abuelo != null && abuelo.getCategoriaPadre() != null) {
                    return ResponseEntity.badRequest().body("Error: No puedes mover esta categoría a un 4to nivel.");
                }
            }
        }

        return categoriaRepository.findById(id)
                .map(categoriaExistente -> {
                    categoriaExistente.setNombre(categoriaDetalles.getNombre());
                    categoriaExistente.setDescripcion(categoriaDetalles.getDescripcion());
                    categoriaExistente.setCodigoCorto(categoriaDetalles.getCodigoCorto());

                    if (categoriaDetalles.getCategoriaPadre() != null) {
                        Categoria padre = categoriaRepository.findById(categoriaDetalles.getCategoriaPadre().getIdCategoria())
                                .orElse(null);
                        categoriaExistente.setCategoriaPadre(padre);
                    } else {
                        categoriaExistente.setCategoriaPadre(null);
                    }

                    Categoria categoriaActualizada = categoriaRepository.save(categoriaExistente);
                    return ResponseEntity.ok(categoriaActualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id) {
        if (!categoriaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            categoriaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    // --- Mapeador (Arma la ruta completa automáticamente) ---
    private CategoriaDTO convertToDTO(Categoria categoria) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setIdCategoria(categoria.getIdCategoria());
        dto.setNombre(categoria.getNombre());
        dto.setCodigoCorto(categoria.getCodigoCorto());
        dto.setDescripcion(categoria.getDescripcion());

        if (categoria.getCategoriaPadre() != null) {
            dto.setIdCategoriaPadre(categoria.getCategoriaPadre().getIdCategoria());
        } else {
            dto.setIdCategoriaPadre(null);
        }

        // ARMADO DE LA RUTA COMPLETA (Abuelo > Padre > Hijo)
        String ruta = categoria.getNombre();
        Categoria padre = categoria.getCategoriaPadre();

        if (padre != null) {
            ruta = padre.getNombre() + " > " + ruta;
            Categoria abuelo = padre.getCategoriaPadre();
            if (abuelo != null) {
                ruta = abuelo.getNombre() + " > " + ruta;
            }
        }
        dto.setRutaCompleta(ruta);

        return dto;
    }
}