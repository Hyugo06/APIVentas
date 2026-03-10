package com.mitienda.api_tienda.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class StorageService {

    private final Path rootLocation = Paths.get("media");

    public StorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar la carpeta", e);
        }
    }

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) throw new RuntimeException("Archivo vacío");

            String filename = UUID.randomUUID().toString() + ".jpg";
            Path destinationFile = this.rootLocation.resolve(filename).toAbsolutePath();

            // --- LÓGICA DE REDIMENSIÓN ---
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            int targetWidth = 1000;
            int targetHeight = (originalImage.getHeight() * targetWidth) / originalImage.getWidth();

            Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);

            ImageIO.write(outputImage, "jpg", destinationFile.toFile());
            // -----------------------------

            return "/media/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar imagen", e);
        }
    }

    public void delete(String filenameUrl) {
        try {
            String filename = filenameUrl.replace("/media/", "");
            Files.deleteIfExists(this.rootLocation.resolve(filename));
        } catch (IOException e) {
            System.err.println("No se pudo borrar: " + filenameUrl);
        }
    }
}