package com.jala.backend.medicinephoto.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class CreateMedicinePhotoRequest {

    @NotNull
    private UUID medicineEntryId;

    @NotNull
    private MultipartFile file;

    private String remarks;
}