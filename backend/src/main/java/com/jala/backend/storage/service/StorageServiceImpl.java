package com.jala.backend.storage.service;

import com.jala.backend.storage.config.StorageProperties;
import com.jala.backend.storage.enums.StorageFolder;
import com.jala.backend.storage.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB

    private final StorageProperties storageProperties;

    private final RestClient restClient;

    @Override
    public String upload(
            MultipartFile file,
            StorageFolder folder,
            String entityId,
            String fileName) {

        validate(file, folder, entityId, fileName);

        String objectPath = buildObjectPath(folder, entityId, fileName);
        String uploadUrl = buildUploadUrl(objectPath);

        try {
            byte[] fileBytes = file.getBytes();

            restClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + storageProperties.getServiceRoleKey())
                    .header("apikey", storageProperties.getServiceRoleKey())
                    .header("Content-Type", file.getContentType())
                    .header("x-upsert", "true")
                    .body(fileBytes)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new FileStorageException(
                                "Failed to upload file to Supabase Storage. Status: " + response.getStatusCode());
                    })
                    .toBodilessEntity();

            log.info("Uploaded file [{}] to Supabase Storage path [{}]", fileName, objectPath);

            return buildPublicUrl(objectPath);
        } catch (IOException e) {
            throw new FileStorageException("Unable to read file contents for upload: " + fileName, e);
        } catch (FileStorageException e) {
            throw e;
        } catch (Exception e) {
            throw new FileStorageException("Unexpected error while uploading file: " + fileName, e);
        }
    }

    private void validate(MultipartFile file, StorageFolder folder, String entityId, String fileName) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File must not be null or empty");
        }

        if (folder == null) {
            throw new FileStorageException("Storage folder must be specified");
        }

        if (!StringUtils.hasText(entityId)) {
            throw new FileStorageException("Entity id must be provided");
        }

        if (!StringUtils.hasText(fileName)) {
            throw new FileStorageException("File name must be provided");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileStorageException("File size exceeds the maximum allowed limit of 10 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(java.util.Locale.ROOT))) {
            throw new FileStorageException("Unsupported image type: " + contentType);
        }

        String extension = extractExtension(fileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase(java.util.Locale.ROOT))) {
            throw new FileStorageException("Unsupported file extension: " + extension);
        }
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new FileStorageException("File name must include a valid extension: " + fileName);
        }
        return fileName.substring(dotIndex + 1);
    }

    private String buildObjectPath(StorageFolder folder, String entityId, String fileName) {
        return folder.getFolder() + "/" + entityId + "/" + fileName;
    }

    private String buildUploadUrl(String objectPath) {
        return storageProperties.getUrl()
                + "/storage/v1/object/"
                + storageProperties.getStorageBucket()
                + "/"
                + objectPath;
    }

    private String buildPublicUrl(String objectPath) {
        return storageProperties.getUrl()
                + "/storage/v1/object/public/"
                + storageProperties.getStorageBucket()
                + "/"
                + objectPath;
    }
}
