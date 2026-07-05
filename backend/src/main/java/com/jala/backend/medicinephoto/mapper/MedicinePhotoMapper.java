package com.jala.backend.medicinephoto.mapper;

import com.jala.backend.medicinephoto.dto.request.CreateMedicinePhotoRequest;
import com.jala.backend.medicinephoto.dto.response.MedicinePhotoResponse;
import com.jala.backend.medicinephoto.entity.MedicinePhoto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicinePhotoMapper {

    @Mapping(target = "medicineEntry", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    MedicinePhoto toEntity(CreateMedicinePhotoRequest request);

    @Mapping(source = "medicineEntry.id", target = "medicineEntryId")
    @Mapping(source = "uploadedBy.employeeCode", target = "uploadedBy")
    MedicinePhotoResponse toResponse(MedicinePhoto entity);
}