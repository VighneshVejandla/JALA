package com.jala.backend.history.mapper;

import com.jala.backend.feedentry.entity.FeedEntry;
import com.jala.backend.harvest.entity.Harvest;
import com.jala.backend.history.dto.response.*;
import com.jala.backend.medicine.entity.MedicineEntry;
import com.jala.backend.medicinephoto.entity.MedicinePhoto;
import com.jala.backend.pondcycle.entity.PondCycle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HistoryMapper {

    @Mapping(target = "cycleId", source = "id")
    @Mapping(target = "harvestDate", ignore = true)
    @Mapping(target = "totalFeedEntries", ignore = true)
    @Mapping(target = "totalMedicineEntries", ignore = true)
    @Mapping(target = "totalHarvests", ignore = true)
    PondCycleHistoryResponse toResponse(
            PondCycle pondCycle);

    @Mapping(target = "harvestId", source = "id")
    @Mapping(target = "cycleNumber", source = "pondCycle.cycleNumber")
    HarvestHistoryResponse toHarvestResponse(
            Harvest harvest);

    @Mapping(target = "feedEntryId", source = "id")
    @Mapping(target = "cycleNumber", source = "pondCycle.cycleNumber")
    @Mapping(target = "sessionNumber", source = "feedSchedule.sessionNumber")
    @Mapping(target = "feedSize", expression = "java(feedEntry.getFeedSize().name())")
    @Mapping(target = "createdBy", source = "createdBy.employeeCode")
    FeedHistoryResponse toFeedHistoryResponse(
            FeedEntry feedEntry);

    @Mapping(target = "medicineId", source = "id")
    @Mapping(target = "cycleNumber", source = "pondCycle.cycleNumber")
    @Mapping(target = "unit", expression = "java(medicineEntry.getUnit().name())")
    @Mapping(target = "status", expression = "java(medicineEntry.getStatus().name())")
    @Mapping(target = "createdBy", source = "createdBy.employeeCode")
    @Mapping(target = "photos", ignore = true)
    MedicineHistoryResponse toMedicineHistoryResponse(
            MedicineEntry medicineEntry);

    @Mapping(target = "photoId", source = "id")
    MedicineHistoryPhotoResponse toMedicinePhotoResponse(
            MedicinePhoto medicinePhoto);
}