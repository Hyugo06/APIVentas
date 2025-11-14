package com.mitienda.api_tienda.Controller;

import com.mitienda.api_tienda.DTO.MarcaDTO;
import com.mitienda.api_tienda.Model.Marca;
import com.mitienda.api_tienda.Repository.MarcaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
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



    @GetMapping("/admin/marcas/{id}")
    public ResponseEntity<Marca> obtenerMarcaPorId(@PathVariable Integer id) {
        return marcaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/marcas")
    public ResponseEntity<Marca> crearMarca(@Valid @RequestBody Marca marca) {
        Marca marcaGuardada = marcaRepository.save(marca);
        return new ResponseEntity<>(marcaGuardada, HttpStatus.CREATED);
    }

    @PutMapping("/admin/marcas/{id}")
    public ResponseEntity<Marca> actualizarMarca(@PathVariable Integer id, @Valid @RequestBody Marca marcaDetalles) {
        return marcaRepository.findById(id)
                .map(marcaExistente -> {
                    marcaExistente.setNombre(marcaDetalles.getNombre());
                    marcaExistente.setDescripcion(marcaDetalles.getDescripcion());
                    Marca marcaActualizada = marcaRepository.save(marcaExistente);
                    return ResponseEntity.ok(marcaActualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/admin/marcas/{id}")
    public ResponseEntity<Void> eliminarMarca(@PathVariable Integer id) {
        if (!marcaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // (En un proyecto real, aquí deberíamos comprobar que la marca no esté
        // siendo usada por ningún producto antes de borrarla)
        marcaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    private MarcaDTO convertToDTO(Marca marca) {
        MarcaDTO dto = new MarcaDTO();
        dto.setIdMarca(marca.getIdMarca());
        dto.setNombre(marca.getNombre());
        return dto;
    }
}