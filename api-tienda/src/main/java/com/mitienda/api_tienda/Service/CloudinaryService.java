package com.mitienda.api_tienda.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String subirImagen(MultipartFile file) {
        try {
            // Subimos el archivo a Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            // Retornamos la URL segura (https) para guardarla en la BD
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null; // O puedes lanzar una excepción personalizada
        }
    }

    // Método extra por si algún día necesitas borrar fotos
    public Map eliminarImagen(String idPublico) throws IOException {
        return cloudinary.uploader().destroy(idPublico, ObjectUtils.emptyMap());
    }
}