package com.jala.backend.history.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MedicineHistoryPhotoResponse {

    private UUID photoId;

    private String fileName;

    private String filePath;
}