package com.mitienda.api_tienda.Controller;

import com.mitienda.api_tienda.Service.CloudinaryService; // Importamos tu nuevo servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Autowired
    private CloudinaryService cloudinaryService; // <--- Usamos Cloudinary en vez de Storage local

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        // 1. Subir el archivo a Cloudinary
        String url = cloudinaryService.subirImagen(file);

        if (url == null) {
            return ResponseEntity.internalServerError().build(); // Error si falla la subida
        }

        // 2. Devolver la URL generada (https://res.cloudinary.com/...)
        // El frontend recibirá esto y lo usará para crear el producto
        return ResponseEntity.ok(Map.of("url", url));
    }
}