package com.jala.backend.storage.service;

import com.jala.backend.storage.config.StorageProperties;
import com.jala.backend.storage.enums.StorageFolder;
import com.jala.backend.storage.exception.FileStorageException;
import com.jala.backend.storage.util.FileValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

            // x-upsert is intentionally NOT set: silent overwrite of an
            // existing object is a security risk (flagged in review). Object
            // names are unique per entity+sequence, so collisions are not
            // expected; a rare retry after a partial failure surfaces as a
            // storage error rather than silently clobbering data.
            restClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + storageProperties.getServiceRoleKey())
                    .header("apikey", storageProperties.getServiceRoleKey())
                    .header("Content-Type", file.getContentType())
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

        FileValidationUtil.rejectPathTraversal(entityId);
        FileValidationUtil.rejectPathTraversal(fileName);

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileStorageException("File size exceeds the maximum allowed limit of 10 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(java.util.Locale.ROOT))) {
            throw new FileStorageException("Unsupported image type: " + contentType);
        }

        // Validates the extension (allow-list) and rejects traversal sequences.
        FileValidationUtil.extractExtension(fileName);

        // Magic-byte sniffing: the real content must match a supported image type.
        FileValidationUtil.requireImageContent(file);
    }

    private String buildObjectPath(StorageFolder folder, String entityId, String fileName) {
        // Segments are already validated (no "/", "\\" or ".."), but encode
        // them as defense-in-depth so nothing from the client can alter the
        // structure of the storage URL path.
        return folder.getFolder()
                + "/" + encodeSegment(entityId)
                + "/" + encodeSegment(fileName);
    }

    private static String encodeSegment(String segment) {
        return java.net.URLEncoder
                .encode(segment, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");
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
