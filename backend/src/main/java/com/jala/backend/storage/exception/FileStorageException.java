package com.jala.backend.storage.exception;

/**
 * Thrown for any failure related to validating or uploading a file to Supabase Storage.
 * Wire this into the project's existing global exception handler
 * (e.g. map it to HTTP 400 for validation failures / 502 for upstream Supabase failures).
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
