package com.jala.backend.pondcycle.dto.response;

import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.enums.ShrimpSpecies;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PondCycleResponse {

    private UUID id;

    private UUID pondId;

    private String pondName;

    private ShrimpSpecies species;

    private LocalDate stockingDate;

    private Integer shrimpCount;

    private PondCycleStatus status;
}