package com.juanma.emprenglobal.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@ConfigurationProperties(prefix = "application.path")
public class UploadFileService {
    private String URL;
    private String rootFolder;
    private final String EXTENSION_REGEX = ".+(?=\\.[^\\.]+$)";
    private final String FILES_PATH_FORMAT = "/%d/%s";

    public String uploadPhoto(long id, MultipartFile file, PicEntities entity) {
        String entityPath = rootFolder + entity.name();
        String extension = file.getOriginalFilename()
                .replaceFirst(EXTENSION_REGEX, "")
                .toLowerCase();
        Path dirPath = Paths.get(entityPath, String.valueOf(id));
        Path filePath = Paths.get(entityPath, String.valueOf(id), entity.getTag() + extension);

        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectory(dirPath);
            }
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return translateToURL(filePath.toString());
    }

    private String translateToURL(String filePath) {
        return filePath
                .replaceFirst(rootFolder, URL)
                .toLowerCase();
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public String getPathFormat(PicEntities entity) {
        return rootFolder + entity.name() + FILES_PATH_FORMAT;
    }
}
