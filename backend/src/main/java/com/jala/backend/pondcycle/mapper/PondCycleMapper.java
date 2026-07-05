package com.jala.backend.pondcycle.mapper;

import com.jala.backend.pondcycle.dto.request.CreatePondCycleRequest;
import com.jala.backend.pondcycle.dto.response.PondCycleResponse;
import com.jala.backend.pondcycle.entity.PondCycle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PondCycleMapper {

    @Mapping(target = "pond", ignore = true)
    @Mapping(target = "status", ignore = true)
    PondCycle toEntity(CreatePondCycleRequest request);

    @Mapping(source = "pond.id", target = "pondId")
    @Mapping(source = "pond.pondName", target = "pondName")
    PondCycleResponse toResponse(PondCycle pondCycle);
}