package com.jala.backend.storage.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.storage.dto.FileUploadResponse;
import com.jala.backend.storage.enums.StorageFolder;
import com.jala.backend.storage.service.StorageService;
import com.jala.backend.storage.util.FileValidationUtil;
import com.jala.backend.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.STORAGE_BASE_URL)
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") StorageFolder folder,
            @RequestParam("entityId") String entityId) {

        // Never trust the client-supplied file name: validate its extension
        // and generate the stored name server-side.
        String extension =
                FileValidationUtil.extractExtension(
                        file.getOriginalFilename());

        String fileName = UUID.randomUUID() + "." + extension;

        String fileUrl =
                storageService.upload(
                        file,
                        folder,
                        entityId,
                        fileName
                );

        FileUploadResponse response = FileUploadResponse.builder()
                .fileName(fileName)
                .fileUrl(fileUrl)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<FileUploadResponse>builder()
                                .success(true)
                                .message("File uploaded successfully")
                                .data(response)
                                .build()
                );
    }
}
