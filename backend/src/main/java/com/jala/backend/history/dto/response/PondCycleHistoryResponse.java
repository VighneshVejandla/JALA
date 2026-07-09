package com.jala.backend.history.dto.response;

import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.enums.ShrimpSpecies;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PondCycleHistoryResponse {

    private UUID cycleId;

    private Integer cycleNumber;

    private Boolean currentCycle;

    private PondCycleStatus status;

    private ShrimpSpecies species;

    private LocalDate stockingDate;

    private Integer shrimpCount;

    private LocalDate harvestDate;

    private Integer totalFeedEntries;

    private Integer totalMedicineEntries;

    private Integer totalHarvests;
}