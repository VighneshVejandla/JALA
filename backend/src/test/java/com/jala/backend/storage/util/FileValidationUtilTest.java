package com.jala.backend.storage.util;

import com.jala.backend.storage.exception.FileStorageException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileValidationUtilTest {

    @Nested
    class ExtractExtension {

        @ParameterizedTest
        @CsvSource({
                "photo.jpg, jpg",
                "photo.jpeg, jpeg",
                "photo.png, png",
                "photo.webp, webp",
                "PHOTO.JPG, jpg",
                "archive.tar.png, png"
        })
        void returnsLowerCasedAllowedExtension(String fileName,
                                               String expected) {
            assertThat(FileValidationUtil.extractExtension(fileName))
                    .isEqualTo(expected);
        }

        @Test
        void throwsWhenNoExtensionPresent() {
            assertThatThrownBy(() ->
                    FileValidationUtil.extractExtension("photo"))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("must include a valid extension");
        }

        @Test
        void throwsWhenNameEndsWithDot() {
            assertThatThrownBy(() ->
                    FileValidationUtil.extractExtension("photo."))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("must include a valid extension");
        }

        @ParameterizedTest
        @ValueSource(strings = {"photo.gif", "photo.exe", "photo.svg"})
        void throwsForUnsupportedExtensions(String fileName) {
            assertThatThrownBy(() ->
                    FileValidationUtil.extractExtension(fileName))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Unsupported file extension");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "../evil.jpg",
                "a/b.jpg",
                "a\\b.jpg",
                "..",
                "evil..jpg"
        })
        void throwsForTraversalSequences(String fileName) {
            assertThatThrownBy(() ->
                    FileValidationUtil.extractExtension(fileName))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Invalid file or path segment");
        }

        @Test
        void throwsForNullFileName() {
            assertThatThrownBy(() ->
                    FileValidationUtil.extractExtension(null))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Invalid file or path segment");
        }
    }

    @Nested
    class RejectPathTraversal {

        @Test
        void acceptsCleanValue() {
            assertThatCode(() ->
                    FileValidationUtil.rejectPathTraversal("photo.jpg"))
                    .doesNotThrowAnyException();
        }

        @Test
        void throwsForNull() {
            assertThatThrownBy(() ->
                    FileValidationUtil.rejectPathTraversal(null))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Invalid file or path segment");
        }

        @ParameterizedTest
        @ValueSource(strings = {"a/b", "a\\b", "a..b", "../x", "/abs"})
        void throwsForSeparatorsAndTraversal(String value) {
            assertThatThrownBy(() ->
                    FileValidationUtil.rejectPathTraversal(value))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Invalid file or path segment");
        }
    }

    @Nested
    class RequireImageContent {

        private MockMultipartFile fileWithBytes(byte[] content) {
            return new MockMultipartFile(
                    "file", "upload.jpg", "image/jpeg", content);
        }

        @Test
        void acceptsJpegMagicBytes() {
            MockMultipartFile file = fileWithBytes(new byte[]{
                    (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0});

            assertThatCode(() ->
                    FileValidationUtil.requireImageContent(file))
                    .doesNotThrowAnyException();
        }

        @Test
        void acceptsPngMagicBytes() {
            MockMultipartFile file = fileWithBytes(new byte[]{
                    (byte) 0x89, 0x50, 0x4E, 0x47});

            assertThatCode(() ->
                    FileValidationUtil.requireImageContent(file))
                    .doesNotThrowAnyException();
        }

        @Test
        void acceptsRiffWebpMagicBytes() {
            // Full WEBP signature: "RIFF" + 4-byte size + "WEBP".
            MockMultipartFile file = fileWithBytes(new byte[]{
                    0x52, 0x49, 0x46, 0x46, 0x24, 0x00, 0x00, 0x00,
                    0x57, 0x45, 0x42, 0x50});

            assertThatCode(() ->
                    FileValidationUtil.requireImageContent(file))
                    .doesNotThrowAnyException();
        }

        @Test
        void rejectsBareRiffThatIsNotWebp() {
            // "RIFF" container that is not WEBP (e.g. a WAV/AVI) must be
            // rejected — a bare RIFF header is not enough.
            MockMultipartFile file = fileWithBytes(new byte[]{
                    0x52, 0x49, 0x46, 0x46, 0x24, 0x00, 0x00, 0x00,
                    0x57, 0x41, 0x56, 0x45}); // "WAVE"

            assertThatThrownBy(() ->
                    FileValidationUtil.requireImageContent(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("File content is not a supported image format");
        }

        @Test
        void rejectsGarbageBytes() {
            MockMultipartFile file = fileWithBytes(
                    "not-an-image".getBytes());

            assertThatThrownBy(() ->
                    FileValidationUtil.requireImageContent(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("File content is not a supported image format");
        }

        @Test
        void rejectsEmptyFile() {
            MockMultipartFile file = fileWithBytes(new byte[0]);

            assertThatThrownBy(() ->
                    FileValidationUtil.requireImageContent(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("File content is not a supported image format");
        }

        @Test
        void rejectsHeaderShorterThanMagic() {
            MockMultipartFile file = fileWithBytes(
                    new byte[]{(byte) 0xFF, (byte) 0xD8});

            assertThatThrownBy(() ->
                    FileValidationUtil.requireImageContent(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("File content is not a supported image format");
        }

        @Test
        void wrapsIoExceptionWhileReading() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getInputStream())
                    .thenThrow(new IOException("stream broken"));

            assertThatThrownBy(() ->
                    FileValidationUtil.requireImageContent(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Unable to read uploaded file")
                    .hasCauseInstanceOf(IOException.class);
        }
    }
}
