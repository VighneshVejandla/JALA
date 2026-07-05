package com.jala.backend.storage.enums;

import lombok.Getter;

/**
 * Top-level folders inside the Supabase "jala-storage" bucket.
 * The request parameter accepted by the upload API maps directly to one of these.
 */
@Getter
public enum StorageFolder {

    MEDICINE("medicine"),
    RECEIPTS("receipts"),
    HARVEST("harvest"),
    USERS("users");

    private final String folder;

    StorageFolder(String folder) {
        this.folder = folder;
    }
}
