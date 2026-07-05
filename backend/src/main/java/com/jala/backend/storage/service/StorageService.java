package com.jala.backend.storage.service;

import com.jala.backend.storage.enums.StorageFolder;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /**
     * Uploads a file to Supabase Storage under {folder}/{entityId}/{fileName}
     * and returns its public URL.
     *
     * @param file     the image to upload
     * @param folder   top-level bucket folder (medicine / receipts / harvest / users)
     * @param entityId id of the owning business entity (e.g. medicineEntryId), used as the sub-folder
     * @param fileName final file name, typically produced by FileNameGenerator
     * @return the public URL of the uploaded object
     */
    String upload(MultipartFile file, StorageFolder folder, String entityId, String fileName);
}
