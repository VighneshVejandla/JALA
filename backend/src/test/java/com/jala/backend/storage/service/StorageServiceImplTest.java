package com.jala.backend.storage.service;

import com.jala.backend.storage.config.StorageProperties;
import com.jala.backend.storage.enums.StorageFolder;
import com.jala.backend.storage.exception.FileStorageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Covers the {@code validate} guard rails, which all short-circuit before
 * the RestClient is touched (so no HTTP stubbing is required).
 */
@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

    private static final byte[] PNG =
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A};

    @Mock
    private StorageProperties storageProperties;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private StorageServiceImpl service;

    private MultipartFile png() {
        return new MockMultipartFile("f", "scan.png", "image/png", PNG);
    }

    @Test
    @DisplayName("null file is rejected")
    void nullFile() {
        assertThatThrownBy(() ->
                service.upload(null, StorageFolder.MEDICINE, "e1", "a.png"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    @DisplayName("empty file is rejected")
    void emptyFile() {
        MultipartFile empty =
                new MockMultipartFile("f", "a.png", "image/png", new byte[0]);

        assertThatThrownBy(() ->
                service.upload(empty, StorageFolder.MEDICINE, "e1", "a.png"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    @DisplayName("missing folder is rejected")
    void nullFolder() {
        assertThatThrownBy(() ->
                service.upload(png(), null, "e1", "a.png"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("folder");
    }

    @Test
    @DisplayName("blank entity id is rejected")
    void blankEntityId() {
        assertThatThrownBy(() ->
                service.upload(png(), StorageFolder.MEDICINE, "  ", "a.png"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Entity id");
    }

    @Test
    @DisplayName("path traversal in the entity id is rejected")
    void traversalEntityId() {
        assertThatThrownBy(() ->
                service.upload(png(), StorageFolder.MEDICINE, "../secret", "a.png"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Invalid file or path segment");
    }

    @Test
    @DisplayName("path traversal in the file name is rejected")
    void traversalFileName() {
        assertThatThrownBy(() ->
                service.upload(png(), StorageFolder.MEDICINE, "e1", "../a.png"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Invalid file or path segment");
    }

    @Test
    @DisplayName("unsupported content type is rejected")
    void badContentType() {
        MultipartFile pdf =
                new MockMultipartFile("f", "a.png", "application/pdf", PNG);

        assertThatThrownBy(() ->
                service.upload(pdf, StorageFolder.MEDICINE, "e1", "a.png"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Unsupported image type");
    }

    @Test
    @DisplayName("content that is not really an image is rejected")
    void notAnImage() {
        MultipartFile fake = new MockMultipartFile(
                "f", "a.png", "image/png", new byte[]{1, 2, 3, 4});

        assertThatThrownBy(() ->
                service.upload(fake, StorageFolder.MEDICINE, "e1", "a.png"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("not a supported image");
    }
}
