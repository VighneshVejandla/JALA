package com.jala.backend.medicinephoto.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MedicinePhotoResponse {

    private UUID id;

    private UUID medicineEntryId;

    private String fileName;

    private String filePath;

    private String contentType;

    private Long fileSize;

    private String uploadedBy;

    private LocalDateTime uploadedAt;
}