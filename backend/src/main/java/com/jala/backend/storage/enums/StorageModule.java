package com.jala.backend.storage.enums;

import lombok.Getter;

/**
 * Used only by FileNameGenerator to build the module-specific tag that
 * appears inside a generated file name (e.g. "..._medicine_001.webp").
 */
@Getter
public enum StorageModule {

    MEDICINE("medicine"),
    RECEIPT("receipt"),
    HARVEST("harvest"),
    USER("user");

    private final String tag;

    StorageModule(String tag) {
        this.tag = tag;
    }
}
