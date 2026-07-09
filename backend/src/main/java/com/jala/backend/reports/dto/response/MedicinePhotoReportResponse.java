package com.jala.backend.reports.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicinePhotoReportResponse {

    private String fileName;

    private String filePath;

    private String contentType;

    private Long fileSize;
}