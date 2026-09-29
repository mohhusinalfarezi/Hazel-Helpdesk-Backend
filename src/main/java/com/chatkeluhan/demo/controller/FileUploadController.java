package com.chatkeluhan.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    // Direktori penyimpanan lokal (akan terbuat otomatis sejajar dengan folder src)
    private final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // 1. Buat folder "uploads" jika belum ada
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 2. Ambil ekstensi asli dan buat nama file unik dengan UUID
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
            String fileName = UUID.randomUUID().toString() + extension;

            // 3. Simpan file ke dalam server
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // 4. Susun URL gambar (10.0.2.2 adalah IP localhost khusus untuk Emulator Android)
            String fileUrl = "http://10.0.2.2:8080/uploads/" + fileName;

            response.put("status", "success");
            response.put("imageUrl", fileUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("status", "error");
            response.put("message", "Gagal mengunggah gambar: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}