package com.abhinav.lms.file.service;

import com.abhinav.lms.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class LocalStorageService implements StorageService {

    private final Path rootLocation;
    private final List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "webp", "gif", "pdf", "docx", "txt", "zip");

    public LocalStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            log.info("Storage directory initialized at: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            log.error("Could not initialize storage directory", e);
            throw new BusinessException("Could not initialize storage location directory");
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("Failed to store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new BusinessException("Cannot store file with relative path sequence " + originalFilename);
        }

        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null || !allowedExtensions.contains(extension.toLowerCase())) {
            throw new BusinessException("File type not allowed. Allowed types are: " + String.join(", ", allowedExtensions));
        }

        // Generate unique filename with UUID prefix to avoid name collisions
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        try {
            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename))
                    .normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new BusinessException("Cannot store file outside current directory bounds");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                log.info("Stored file: {} as {}", originalFilename, uniqueFilename);
            }
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new BusinessException("Failed to store file: " + originalFilename);
        }

        return uniqueFilename;
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            log.error("Malformed URL resolved for file: {}", filename, e);
            throw new BusinessException("Could not read file: " + filename);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Files.deleteIfExists(file);
            log.info("Deleted file: {}", filename);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filename, e);
            throw new BusinessException("Failed to delete file: " + filename);
        }
    }
}
