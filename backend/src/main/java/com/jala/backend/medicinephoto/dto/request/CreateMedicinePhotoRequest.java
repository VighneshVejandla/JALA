package com.jala.backend.medicinephoto.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateMedicinePhotoRequest {

    private UUID medicineEntryId;

    private String fileName;

    private String filePath;

    private String contentType;

    private Long fileSize;
}