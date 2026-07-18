package com.jala.backend.storage.util;

import com.jala.backend.storage.enums.StorageModule;
import com.jala.backend.storage.exception.FileStorageException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileNameGeneratorTest {

    private static final LocalDate DATE = LocalDate.of(2026, 1, 15);

    @Nested
    class GenerateEntityFileName {

        @Test
        void buildsExpectedFileName() {
            String name = FileNameGenerator.generateEntityFileName(
                    "S01", "Green Farm", DATE,
                    StorageModule.MEDICINE, 7, "jpg");

            assertThat(name)
                    .isEqualTo("S01_Green_Farm_2026-01-15_medicine_007.jpg");
        }

        @Test
        void normalizesSpecialCharactersInSiteName() {
            String name = FileNameGenerator.generateEntityFileName(
                    "S01", "  --Green  &  Farm--  ", DATE,
                    StorageModule.HARVEST, 12, "png");

            assertThat(name)
                    .isEqualTo("S01_Green_Farm_2026-01-15_harvest_012.png");
        }

        @Test
        void padsSequenceToThreeDigitsAndAllowsZero() {
            String name = FileNameGenerator.generateEntityFileName(
                    "S01", "Farm", DATE, StorageModule.RECEIPT, 0, "webp");

            assertThat(name).endsWith("_receipt_000.webp");
        }

        @Test
        void sequenceLargerThanThreeDigitsIsNotTruncated() {
            String name = FileNameGenerator.generateEntityFileName(
                    "S01", "Farm", DATE, StorageModule.USER, 1234, "jpg");

            assertThat(name).contains("_user_1234.");
        }

        @Test
        void normalizesExtensionCaseAndLeadingDot() {
            String name = FileNameGenerator.generateEntityFileName(
                    "S01", "Farm", DATE, StorageModule.MEDICINE, 1, ".JPG");

            assertThat(name).endsWith(".jpg");
        }

        @Test
        void throwsForNegativeSequence() {
            assertThatThrownBy(() ->
                    FileNameGenerator.generateEntityFileName(
                            "S01", "Farm", DATE,
                            StorageModule.MEDICINE, -1, "jpg"))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Sequence number must not be negative");
        }

        @Test
        void throwsWhenSiteCodeMissing() {
            assertThatThrownBy(() ->
                    FileNameGenerator.generateEntityFileName(
                            null, "Farm", DATE,
                            StorageModule.MEDICINE, 1, "jpg"))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Missing required fields for file name generation");

            assertThatThrownBy(() ->
                    FileNameGenerator.generateEntityFileName(
                            "  ", "Farm", DATE,
                            StorageModule.MEDICINE, 1, "jpg"))
                    .isInstanceOf(FileStorageException.class);
        }

        @Test
        void throwsWhenSiteNameMissing() {
            assertThatThrownBy(() ->
                    FileNameGenerator.generateEntityFileName(
                            "S01", null, DATE,
                            StorageModule.MEDICINE, 1, "jpg"))
                    .isInstanceOf(FileStorageException.class);

            assertThatThrownBy(() ->
                    FileNameGenerator.generateEntityFileName(
                            "S01", " ", DATE,
                            StorageModule.MEDICINE, 1, "jpg"))
                    .isInstanceOf(FileStorageException.class);
        }

        @Test
        void throwsWhenDateMissing() {
            assertThatThrownBy(() ->
                    FileNameGenerator.generateEntityFileName(
                            "S01", "Farm", null,
                            StorageModule.MEDICINE, 1, "jpg"))
                    .isInstanceOf(FileStorageException.class);
        }

        @Test
        void throwsWhenModuleMissing() {
            assertThatThrownBy(() ->
                    FileNameGenerator.generateEntityFileName(
                            "S01", "Farm", DATE, null, 1, "jpg"))
                    .isInstanceOf(FileStorageException.class);
        }

        @Test
        void throwsWhenExtensionMissing() {
            assertThatThrownBy(() ->
                    FileNameGenerator.generateEntityFileName(
                            "S01", "Farm", DATE,
                            StorageModule.MEDICINE, 1, null))
                    .isInstanceOf(FileStorageException.class);

            assertThatThrownBy(() ->
                    FileNameGenerator.generateEntityFileName(
                            "S01", "Farm", DATE,
                            StorageModule.MEDICINE, 1, ""))
                    .isInstanceOf(FileStorageException.class);
        }
    }

    @Nested
    class GenerateProfileFileName {

        @Test
        void buildsExpectedProfileFileName() {
            String name = FileNameGenerator.generateProfileFileName(
                    "DRV001", "Bala Raju", "webp");

            assertThat(name).isEqualTo("DRV001_Bala_Raju_profile.webp");
        }

        @Test
        void normalizesUserNameAndExtension() {
            String name = FileNameGenerator.generateProfileFileName(
                    "USR9", "  A.  B!  ", ".PNG");

            assertThat(name).isEqualTo("USR9_A_B_profile.png");
        }

        @Test
        void throwsWhenUserCodeMissing() {
            assertThatThrownBy(() ->
                    FileNameGenerator.generateProfileFileName(
                            null, "Bala", "jpg"))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Missing required fields for profile file name generation");

            assertThatThrownBy(() ->
                    FileNameGenerator.generateProfileFileName(
                            "", "Bala", "jpg"))
                    .isInstanceOf(FileStorageException.class);
        }

        @Test
        void throwsWhenUserNameMissing() {
            assertThatThrownBy(() ->
                    FileNameGenerator.generateProfileFileName(
                            "DRV001", null, "jpg"))
                    .isInstanceOf(FileStorageException.class);

            assertThatThrownBy(() ->
                    FileNameGenerator.generateProfileFileName(
                            "DRV001", "   ", "jpg"))
                    .isInstanceOf(FileStorageException.class);
        }

        @Test
        void throwsWhenExtensionMissing() {
            assertThatThrownBy(() ->
                    FileNameGenerator.generateProfileFileName(
                            "DRV001", "Bala", null))
                    .isInstanceOf(FileStorageException.class);

            assertThatThrownBy(() ->
                    FileNameGenerator.generateProfileFileName(
                            "DRV001", "Bala", " "))
                    .isInstanceOf(FileStorageException.class);
        }
    }
}
