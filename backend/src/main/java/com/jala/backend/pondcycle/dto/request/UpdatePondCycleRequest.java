package com.jala.backend.pondcycle.dto.request;

import com.jala.backend.pondcycle.enums.ShrimpSpecies;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePondCycleRequest {

    private ShrimpSpecies species;

    private LocalDate stockingDate;

    private Integer shrimpCount;
}