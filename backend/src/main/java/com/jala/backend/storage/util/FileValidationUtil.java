package com.jala.backend.storage.util;

import com.jala.backend.storage.exception.FileStorageException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Shared upload validation: extension parsing (previously re-implemented
 * in three services), path-traversal rejection and magic-byte sniffing
 * so a file's real content matches its declared image type.
 */
public final class FileValidationUtil {

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "webp");

    private static final byte[] JPEG_MAGIC =
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    private static final byte[] PNG_MAGIC =
            {(byte) 0x89, 0x50, 0x4E, 0x47};

    private static final byte[] RIFF_MAGIC =
            {0x52, 0x49, 0x46, 0x46}; // "RIFF" container header (bytes 0-3)

    private static final byte[] WEBP_MAGIC =
            {0x57, 0x45, 0x42, 0x50}; // "WEBP" form type (bytes 8-11)

    private FileValidationUtil() {
    }

    /**
     * @throws FileStorageException when the name is missing an extension
     *         or contains path separators / traversal sequences.
     */
    public static String extractExtension(String fileName) {

        // A missing filename is a distinct, clearer client error than a
        // path-traversal rejection.
        if (fileName == null || fileName.isBlank()) {
            throw new FileStorageException("File name is required");
        }

        rejectPathTraversal(fileName);

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new FileStorageException(
                    "File name must include a valid extension: " + fileName);
        }

        String extension = fileName
                .substring(dotIndex + 1)
                .toLowerCase(java.util.Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileStorageException(
                    "Unsupported file extension: " + extension);
        }

        return extension;
    }

    /** Rejects values that could escape the intended storage prefix. */
    public static void rejectPathTraversal(String value) {

        if (value == null
                || value.contains("/")
                || value.contains("\\")
                || value.contains("..")) {

            throw new FileStorageException(
                    "Invalid file or path segment");
        }
    }

    /**
     * Verifies the upload's leading bytes match a supported image
     * format, so a mislabeled non-image cannot be stored.
     */
    public static void requireImageContent(MultipartFile file) {

        byte[] header;

        // Read enough for the WEBP form type at bytes 8-11, and close the
        // stream (try-with-resources) so no file handle is leaked.
        try (InputStream in = file.getInputStream()) {
            header = in.readNBytes(12);
        } catch (IOException e) {
            throw new FileStorageException(
                    "Unable to read uploaded file", e);
        }

        boolean isImage =
                startsWith(header, 0, JPEG_MAGIC)
                        || startsWith(header, 0, PNG_MAGIC)
                        // WEBP: "RIFF"...."WEBP" — a bare RIFF (e.g. WAV,
                        // AVI) must not pass as an image.
                        || (startsWith(header, 0, RIFF_MAGIC)
                                && startsWith(header, 8, WEBP_MAGIC));

        if (!isImage) {
            throw new FileStorageException(
                    "File content is not a supported image format");
        }
    }

    private static boolean startsWith(byte[] data, int offset, byte[] expected) {

        if (data.length < offset + expected.length) {
            return false;
        }
        return Arrays.equals(
                Arrays.copyOfRange(data, offset, offset + expected.length),
                expected);
    }
}
