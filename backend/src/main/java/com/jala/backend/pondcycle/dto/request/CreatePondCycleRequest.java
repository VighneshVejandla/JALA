package com.jala.backend.pondcycle.dto.request;

import com.jala.backend.pondcycle.enums.ShrimpSpecies;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreatePondCycleRequest {

    @NotNull
    private UUID pondId;

    @NotNull
    private ShrimpSpecies species;

    @NotNull
    private LocalDate stockingDate;

    @NotNull
    private Integer shrimpCount;
}