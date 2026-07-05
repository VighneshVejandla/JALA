package com.jala.backend.medicinephoto.service;

import com.jala.backend.medicinephoto.dto.request.CreateMedicinePhotoRequest;
import com.jala.backend.medicinephoto.dto.response.MedicinePhotoResponse;
import com.jala.backend.medicinephoto.mapper.MedicinePhotoMapper;
import com.jala.backend.medicinephoto.repository.MedicinePhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.medicine.entity.MedicineEntry;
import com.jala.backend.medicine.enums.MedicineStatus;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.medicinephoto.entity.MedicinePhoto;
import com.jala.backend.user.entity.User;
import com.jala.backend.user.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicinePhotoServiceImpl
        implements MedicinePhotoService {

    private final MedicinePhotoRepository repository;

    private final MedicineRepository medicineRepository;

    private final UserRepository userRepository;

    private final MedicinePhotoMapper mapper;

    @Override
    @Transactional
    public MedicinePhotoResponse uploadPhoto(
            CreateMedicinePhotoRequest request) {

        MedicineEntry medicine = getMedicineOrThrow(
                request.getMedicineEntryId());

        if (medicine.getStatus() == MedicineStatus.CANCELLED) {

            throw new BadRequestException(
                    "Cannot upload photos to a cancelled medicine entry.");
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository
                .findByEmployeeCode(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found."));

        MedicinePhoto photo = mapper.toEntity(request);

        photo.setMedicineEntry(medicine);

        photo.setUploadedBy(user);

        photo.setUploadedAt(LocalDateTime.now());

        MedicinePhoto saved = repository.save(photo);

        log.info("Medicine photo metadata saved successfully.");

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicinePhotoResponse> getPhotos(
            UUID medicineEntryId) {

        return repository
                .findByMedicineEntryIdOrderByUploadedAt(
                        medicineEntryId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    private MedicineEntry getMedicineOrThrow(UUID id) {

        return medicineRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Medicine entry not found."));
    }
}