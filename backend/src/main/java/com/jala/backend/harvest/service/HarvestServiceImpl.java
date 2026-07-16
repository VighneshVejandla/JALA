package com.jala.backend.harvest.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.common.util.DateTimeUtil;
import com.jala.backend.harvest.dto.request.CancelHarvestRequest;
import com.jala.backend.harvest.dto.request.CreateHarvestRequest;
import com.jala.backend.harvest.dto.response.HarvestResponse;
import com.jala.backend.harvest.entity.Harvest;
import com.jala.backend.harvest.enums.HarvestStatus;
import com.jala.backend.harvest.mapper.HarvestMapper;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import com.jala.backend.storage.enums.StorageFolder;
import com.jala.backend.storage.enums.StorageModule;
import com.jala.backend.storage.service.StorageService;
import com.jala.backend.storage.util.FileNameGenerator;
import com.jala.backend.user.entity.User;
import com.jala.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class HarvestServiceImpl implements HarvestService {

    private final HarvestRepository repository;
    private final HarvestMapper mapper;
    private final PondCycleRepository pondCycleRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Override
    @Transactional
    public HarvestResponse createHarvest(
            CreateHarvestRequest request) {

        PondCycle pondCycle = pondCycleRepository.findById(
                        request.getPondCycleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pond cycle not found."));
        log.info("Pond Cycle Id : {}", pondCycle.getId());
        log.info("Current Status : {}", pondCycle.getStatus());

        if (pondCycle.getStatus() == PondCycleStatus.HARVESTED) {
            log.info("Harvest validation failed.");

            throw new BadRequestException(
                    "This pond cycle has already been harvested.");
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository
                .findByEmployeeCode(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found."));

        Pond pond = pondCycle.getPond();

        String extension = getExtension(
                request.getBillPhoto());

        long sequence =
                repository.countByPondCycleId(
                        pondCycle.getId()) + 1;

        String fileName =
                FileNameGenerator.generateEntityFileName(
                        pond.getSite().getSiteCode(),
                        pond.getSite().getSiteName(),
                        request.getHarvestDate(),
                        StorageModule.HARVEST,
                        (int) sequence,
                        extension);

        String billUrl =
                storageService.upload(
                        request.getBillPhoto(),
                        StorageFolder.HARVEST,
                        pondCycle.getId().toString(),
                        fileName);

        Harvest harvest =
                mapper.toEntity(request);

        harvest.setPondCycle(pondCycle);

        harvest.setBillPhotoPath(billUrl);

        harvest.setUploadedBy(user);

        harvest.setUploadedAt(DateTimeUtil.now());

        harvest.setStatus(HarvestStatus.ACTIVE);

        if (request.getSellingPricePerKg() != null) {

            harvest.setTotalAmount(
                    request.getSellingPricePerKg()
                            .multiply(
                                    request.getHarvestQuantityKg()));
        }

        Harvest saved =
                repository.save(harvest);

// First deactivate the current cycle
        pondCycle.setStatus(PondCycleStatus.HARVESTED);
        pondCycleRepository.saveAndFlush(pondCycle);

// Now create the next cycle
        createNextCycle(pondCycle);

        log.info(
                "Harvest {} created successfully.",
                saved.getId());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestResponse> getHarvests(
            UUID pondCycleId) {

        return repository
                .findByPondCycleIdOrderByHarvestDateDesc(
                        pondCycleId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public HarvestResponse cancelHarvest(
            UUID harvestId,
            CancelHarvestRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository
                .findByEmployeeCode(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found."));

        Harvest harvest = repository
                .findByIdAndStatus(
                        harvestId,
                        HarvestStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Harvest not found."));

        PondCycle harvestedCycle =
                harvest.getPondCycle();

        PondCycle nextCycle =
                pondCycleRepository
                        .findByPondIdAndStatus(
                                harvestedCycle.getPond().getId(),
                                PondCycleStatus.ACTIVE)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Next pond cycle not found."));

        pondCycleRepository.delete(nextCycle);
        pondCycleRepository.flush();

        harvestedCycle.setStatus(
                PondCycleStatus.ACTIVE);

        pondCycleRepository.saveAndFlush(harvestedCycle);

        harvest.setStatus(
                HarvestStatus.CANCELLED);

        harvest.setCancelledBy(user);

        harvest.setCancelledAt(
                DateTimeUtil.now());

        harvest.setCancellationReason(
                request.getCancellationReason());

        Harvest saved = repository.save(harvest);

        log.info(
                "Harvest {} cancelled successfully.",
                harvestId);

        return mapper.toResponse(saved);
    }

    private void createNextCycle(PondCycle currentCycle) {

        PondCycle nextCycle =
                PondCycle.builder()
                        .pond(currentCycle.getPond())
                        .cycleNumber(currentCycle.getCycleNumber() + 1)
                        .status(PondCycleStatus.ACTIVE)
                        .build();

        pondCycleRepository.save(nextCycle);

        log.info(
                "Created empty Pond Cycle {}",
                nextCycle.getCycleNumber());
    }

    private String getExtension(
            MultipartFile file) {

        String original = file.getOriginalFilename();

        if (original == null || !original.contains(".")) {
            throw new BadRequestException("Invalid file name.");
        }

        return original.substring(original.lastIndexOf('.') + 1);
    }

}