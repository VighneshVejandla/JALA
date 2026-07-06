package com.jala.backend.storage.util;

import com.jala.backend.storage.enums.StorageModule;
import com.jala.backend.storage.exception.FileStorageException;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Produces file names in the naming convention used across JALA modules.
 *
 * Business entity photos (medicine / receipt / harvest):
 *   SITE1_Vada_Palem_1_2026-07-05_medicine_001.webp
 *
 * User profile photos:
 *   DRV001_BalaRaju_profile.webp
 *
 * Callers (Medicine Photo Module, Site Delivery Receipt Module, etc.) generate
 * the file name with this class BEFORE calling storageService.upload(...).
 */
public final class FileNameGenerator {

    private FileNameGenerator() {
    }

    /**
     * @param siteCode     e.g. "SITE1"
     * @param siteName     e.g. "Vada Palem" (spaces are converted to underscores)
     * @param date         the date the entry was recorded
     * @param module       MEDICINE, RECEIPT, or HARVEST
     * @param sequence     running sequence number for that entity/day, e.g. 1 -> "001"
     * @param extension    jpg, jpeg, png, or webp (with or without leading dot)
     */
    public static String generateEntityFileName(String siteCode,
                                                String siteName,
                                                LocalDate date,
                                                StorageModule module,
                                                int sequence,
                                                String extension) {

        if (siteCode == null || siteCode.isBlank()
                || siteName == null || siteName.isBlank()
                || date == null
                || module == null
                || extension == null || extension.isBlank()) {
            throw new FileStorageException("Missing required fields for file name generation");
        }

        if (sequence < 0) {
            throw new FileStorageException("Sequence number must not be negative");
        }

        String normalizedSiteName = siteName
                .trim()
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");

        String normalizedExtension = normalizeExtension(extension);

        String sequencePart =
                String.format(Locale.ROOT, "%03d", sequence);

        return String.format(
                Locale.ROOT,
                "%s_%s_%s_%s_%s.%s",
                siteCode,
                normalizedSiteName,
                date,
                module.getTag(),
                sequencePart,
                normalizedExtension
        );
    }

    /**
     * @param userCode  e.g. "DRV001"
     * @param userName  e.g. "Bala Raju" (spaces are stripped)
     * @param extension jpg, jpeg, png, or webp (with or without leading dot)
     */
    public static String generateProfileFileName(String userCode,
                                                 String userName,
                                                 String extension) {

        if (userCode == null || userCode.isBlank()
                || userName == null || userName.isBlank()
                || extension == null || extension.isBlank()) {
            throw new FileStorageException("Missing required fields for profile file name generation");
        }

        String normalizedUserName = userName
                .trim()
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
        String normalizedExtension = normalizeExtension(extension);

        return String.format(
                Locale.ROOT,
                "%s_%s_profile.%s",
                userCode,
                normalizedUserName,
                normalizedExtension
        );
    }

    private static String normalizeExtension(String extension) {
        String ext = extension.trim().toLowerCase(Locale.ROOT);
        return ext.startsWith(".") ? ext.substring(1) : ext;
    }
}
