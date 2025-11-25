package com.mitienda.api_tienda.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    private final Path rootLocation = Paths.get("media");

    public StorageService() {
        try {
            // Crea la carpeta 'media' si no existe al iniciar
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar la carpeta de almacenamiento", e);
        }
    }

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Error: El archivo está vacío.");
            }

            // Generar un nombre único para evitar duplicados (ej. "uuid_nombre.jpg")
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // Copiar el archivo a la carpeta destino
            Path destinationFile = this.rootLocation.resolve(
                            Paths.get(filename))
                    .normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Devolvemos la URL pública relativa
            return "/media/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Fallo al guardar el archivo.", e);
        }
    }

    public void delete(String filenameUrl) {
        try {
            // El filenameUrl viene como "/media/uuid_foto.jpg"
            // Quitamos "/media/" para obtener el nombre real del archivo
            String filename = filenameUrl.replace("/media/", "");

            Path file = rootLocation.resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo eliminar el archivo: " + filenameUrl, e);
        }
    }
}