package com.mitienda.api_tienda.Controller;

import com.mitienda.api_tienda.DTO.MarcaDTO;
import com.mitienda.api_tienda.Model.Marca;
import com.mitienda.api_tienda.Repository.MarcaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/marcas")
public class MarcaController {

    @Autowired
    private MarcaRepository marcaRepository;

    @GetMapping
    public List<MarcaDTO> obtenerTodasLasMarcas() {
        // Obtenemos las entidades y las convertimos a DTOs
        return marcaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MarcaDTO convertToDTO(Marca marca) {
        MarcaDTO dto = new MarcaDTO();
        dto.setIdMarca(marca.getIdMarca());
        dto.setNombre(marca.getNombre());
        return dto;
    }
}