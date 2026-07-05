package com.jala.backend.pond.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.pond.dto.request.CreatePondRequest;
import com.jala.backend.pond.dto.request.UpdatePondRequest;
import com.jala.backend.pond.dto.response.PondResponse;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pond.mapper.PondMapper;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.site.entity.Site;
import com.jala.backend.site.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PondServiceImpl implements PondService {

    private final PondRepository pondRepository;
    private final SiteRepository siteRepository;
    private final PondMapper pondMapper;

    @Override
    @Transactional
    public PondResponse createPond(CreatePondRequest request) {

        log.info("Creating pond {}", request.getPondCode());

        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Site not found"));

        if (pondRepository.existsBySiteIdAndPondCode(
                request.getSiteId(),
                request.getPondCode())) {

            throw new BadRequestException(
                    "Pond code already exists for this site");
        }

        Pond pond = pondMapper.toEntity(request);

        pond.setSite(site);

        Pond savedPond = pondRepository.save(pond);

        log.info("Pond {} created successfully",
                savedPond.getPondCode());

        return pondMapper.toResponse(savedPond);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PondResponse> getAllPonds() {

        return pondRepository.findAll()
                .stream()
                .map(pondMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PondResponse> getPondsBySite(UUID siteId) {

        return pondRepository.findBySiteIdOrderByPondCode(siteId)
                .stream()
                .map(pondMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PondResponse getPondById(UUID id) {

        Pond pond = getPondOrThrow(id);

        return pondMapper.toResponse(pond);
    }

    @Override
    @Transactional
    public PondResponse patchPond(UUID id,
                                  UpdatePondRequest request) {

        Pond pond = getPondOrThrow(id);

        if (request.getPondCode() != null &&
                !request.getPondCode().isBlank()) {

            if (!request.getPondCode().equals(pond.getPondCode()) &&
                    pondRepository.existsBySiteIdAndPondCode(
                            pond.getSite().getId(),
                            request.getPondCode())) {

                throw new BadRequestException(
                        "Pond code already exists for this site");
            }

            pond.setPondCode(request.getPondCode());
        }

        if (request.getPondName() != null &&
                !request.getPondName().isBlank()) {

            pond.setPondName(request.getPondName());
        }

        if (request.getPondAcres() != null) {

            pond.setPondAcres(request.getPondAcres());
        }

        if (request.getIsActive() != null) {

            pond.setIsActive(request.getIsActive());
        }

        Pond updatedPond = pondRepository.save(pond);

        log.info("Pond {} updated successfully",
                updatedPond.getPondCode());

        return pondMapper.toResponse(updatedPond);
    }

    @Override
    @Transactional
    public void activatePond(UUID id) {

        Pond pond = getPondOrThrow(id);

        pond.setIsActive(true);

        pondRepository.save(pond);

        log.info("Pond {} activated",
                pond.getPondCode());
    }

    @Override
    @Transactional
    public void deactivatePond(UUID id) {

        Pond pond = getPondOrThrow(id);

        pond.setIsActive(false);

        pondRepository.save(pond);

        log.info("Pond {} deactivated",
                pond.getPondCode());
    }

    private Pond getPondOrThrow(UUID id) {

        return pondRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pond not found"));
    }
}