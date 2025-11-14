package com.mitienda.api_tienda.Controller;

import com.mitienda.api_tienda.DTO.CategoriaDTO;
import com.mitienda.api_tienda.Model.Categoria;
import com.mitienda.api_tienda.Repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping
    public List<CategoriaDTO> obtenerTodasLasCategorias() {
        return categoriaRepository.findAll().stream()
                .map(this::convertToDTO) // Llama al método modificado
                .collect(Collectors.toList());
    }

    private CategoriaDTO convertToDTO(Categoria categoria) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setIdCategoria(categoria.getIdCategoria());
        dto.setNombre(categoria.getNombre());

        // Añadimos el ID del padre (si existe)
        if (categoria.getCategoriaPadre() != null) {
            dto.setIdCategoriaPadre(categoria.getCategoriaPadre().getIdCategoria());
        } else {
            dto.setIdCategoriaPadre(null);
        }

        return dto;
    }
}