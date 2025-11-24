package com.mitienda.api_tienda.Controller;

import com.mitienda.api_tienda.Service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Autowired
    private StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        // 1. Guardar el archivo
        String url = storageService.store(file);

        // 2. Devolver la URL generada en un JSON: { "url": "/media/foto.jpg" }
        return ResponseEntity.ok(Map.of("url", url));
    }
}