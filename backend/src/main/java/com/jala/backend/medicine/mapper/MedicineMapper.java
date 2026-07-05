package com.jala.backend.medicine.mapper;

import com.jala.backend.medicine.dto.request.CreateMedicineRequest;
import com.jala.backend.medicine.dto.response.MedicineResponse;
import com.jala.backend.medicine.entity.MedicineEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicineMapper {

    @Mapping(target = "pondCycle", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    @Mapping(target = "restoredBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "restoredAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "restorationReason", ignore = true)
    MedicineEntry toEntity(CreateMedicineRequest request);

    @Mapping(source = "pondCycle.id", target = "pondCycleId")
    @Mapping(source = "createdBy.employeeCode", target = "createdBy")
    @Mapping(expression = "java(entity.getStatus().name())", target = "status")
    @Mapping(expression = "java(entity.getUnit() != null ? entity.getUnit().name() : null)", target = "unit")
    MedicineResponse toResponse(MedicineEntry entity);
}