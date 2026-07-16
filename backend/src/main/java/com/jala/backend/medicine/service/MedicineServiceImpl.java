package com.jala.backend.medicine.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.common.util.DateTimeUtil;
import com.jala.backend.medicine.dto.request.CreateMedicineRequest;
import com.jala.backend.medicine.dto.request.UpdateMedicineRequest;
import com.jala.backend.medicine.dto.response.MedicineResponse;
import com.jala.backend.medicine.entity.MedicineEntry;
import com.jala.backend.medicine.enums.MedicineStatus;
import com.jala.backend.medicine.mapper.MedicineMapper;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import com.jala.backend.user.entity.User;
import com.jala.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineServiceImpl
        implements MedicineService {

    private final MedicineRepository repository;

    private final PondCycleRepository pondCycleRepository;

    private final UserRepository userRepository;

    private final MedicineMapper mapper;

    private static final String USER_NOT_FOUND = "User not found.";

    @Override
    @Transactional
    public MedicineResponse createMedicine(
            CreateMedicineRequest request) {

        PondCycle pondCycle = pondCycleRepository
                .findById(request.getPondCycleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pond cycle not found."));

        if (pondCycle.getStatus() != PondCycleStatus.ACTIVE) {

            throw new BadRequestException(
                    "Medicine can only be added to an active pond cycle.");
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository
                .findByEmployeeCode(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(USER_NOT_FOUND));

        MedicineEntry entry = mapper.toEntity(request);

        entry.setPondCycle(pondCycle);

        entry.setCreatedBy(user);

        entry.setCreatedAt(DateTimeUtil.now());

        entry.setStatus(MedicineStatus.ACTIVE);

        MedicineEntry saved = repository.save(entry);

        log.info("Medicine entry created successfully.");

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> getMedicines(
            UUID pondCycleId) {

        return repository
                .findByPondCycleIdAndStatusOrderByCreatedAtDesc(
                        pondCycleId,
                        MedicineStatus.ACTIVE)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MedicineResponse updateMedicine(
            UUID id,
            UpdateMedicineRequest request) {

        MedicineEntry entry = getMedicineOrThrow(id);

        if (entry.getStatus() == MedicineStatus.CANCELLED) {

            throw new BadRequestException(
                    "Cannot update a cancelled medicine entry.");
        }

        if (request.getQuantity() != null) {

            entry.setQuantity(request.getQuantity());
        }

        if (request.getUnit() != null) {

            entry.setUnit(request.getUnit());
        }

        if (request.getRemarks() != null) {

            entry.setRemarks(request.getRemarks());
        }

        MedicineEntry updated = repository.save(entry);

        log.info("Medicine entry updated successfully.");

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void cancelMedicine(
            UUID id,
            String reason) {

        MedicineEntry entry = getMedicineOrThrow(id);

        if (entry.getStatus() == MedicineStatus.CANCELLED) {

            throw new BadRequestException(
                    "Medicine entry is already cancelled.");
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository
                .findByEmployeeCode(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(USER_NOT_FOUND));

        entry.setStatus(MedicineStatus.CANCELLED);

        entry.setCancelledBy(user);

        entry.setCancelledAt(DateTimeUtil.now());

        entry.setCancellationReason(reason);

        repository.save(entry);

        log.info("Medicine entry cancelled.");
    }

    @Override
    @Transactional
    public void restoreMedicine(UUID id) {

        MedicineEntry entry = getMedicineOrThrow(id);

        if (entry.getStatus() == MedicineStatus.ACTIVE) {

            throw new BadRequestException(
                    "Medicine entry is already active.");
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository
                .findByEmployeeCode(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(USER_NOT_FOUND));

        entry.setStatus(MedicineStatus.ACTIVE);

        entry.setRestoredBy(user);

        entry.setRestoredAt(DateTimeUtil.now());

        entry.setRestorationReason(null);

        repository.save(entry);

        log.info("Medicine entry restored.");
    }

    private MedicineEntry getMedicineOrThrow(UUID id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Medicine entry not found."));
    }
}