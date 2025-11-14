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
    // Responde a GET /api/categorias
    @GetMapping("/categorias")
    public List<CategoriaDTO> obtenerTodasLasCategorias() {
        return categoriaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // --- ENDPOINTS DE ADMIN (Protegidos por SecurityConfig) ---

    @GetMapping("/admin/categorias")
    public List<CategoriaDTO> obtenerTodasLasCategoriasAdmin() {
        return categoriaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/admin/categorias")
    public ResponseEntity<Categoria> crearCategoria(@Valid @RequestBody Categoria categoria) {
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
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Integer id, @Valid @RequestBody Categoria categoriaDetalles) {
        return categoriaRepository.findById(id)
                .map(categoriaExistente -> {
                    categoriaExistente.setNombre(categoriaDetalles.getNombre());
                    categoriaExistente.setDescripcion(categoriaDetalles.getDescripcion());

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

    // --- Mapeador (El que arreglamos antes) ---
    private CategoriaDTO convertToDTO(Categoria categoria) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setIdCategoria(categoria.getIdCategoria());
        dto.setNombre(categoria.getNombre());
        if (categoria.getCategoriaPadre() != null) {
            dto.setIdCategoriaPadre(categoria.getCategoriaPadre().getIdCategoria());
        } else {
            dto.setIdCategoriaPadre(null);
        }
        return dto;
    }
}