package com.jala.backend.harvest.mapper;

import com.jala.backend.harvest.dto.request.CreateHarvestRequest;
import com.jala.backend.harvest.dto.response.HarvestResponse;
import com.jala.backend.harvest.entity.Harvest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HarvestMapper {

    Harvest toEntity(
            CreateHarvestRequest request);

    @Mapping(target = "pondCycleId",
            source = "pondCycle.id")

    @Mapping(target = "uploadedByEmployeeCode",
            source = "uploadedBy.employeeCode")

    @Mapping(target = "quantityDisplay",
            expression = "java(getQuantityDisplay(harvest.getHarvestQuantityKg()))")

    HarvestResponse toResponse(
            Harvest harvest);

    default String getQuantityDisplay(java.math.BigDecimal quantityKg) {

        if (quantityKg == null) {
            return null;
        }

        if (quantityKg.compareTo(new java.math.BigDecimal("1000")) < 0) {

            return quantityKg.stripTrailingZeros().toPlainString()
                    + " KG";
        }

        java.math.BigDecimal tons =
                quantityKg.divide(
                        new java.math.BigDecimal("1000"));

        return tons.stripTrailingZeros().toPlainString()
                + " Tons ("
                + quantityKg.stripTrailingZeros().toPlainString()
                + " KG)";
    }
}