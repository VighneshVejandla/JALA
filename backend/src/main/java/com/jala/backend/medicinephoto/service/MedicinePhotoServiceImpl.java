package com.jala.backend.medicinephoto.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.medicine.entity.MedicineEntry;
import com.jala.backend.medicine.enums.MedicineStatus;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.medicinephoto.dto.request.CreateMedicinePhotoRequest;
import com.jala.backend.medicinephoto.dto.response.MedicinePhotoResponse;
import com.jala.backend.medicinephoto.entity.MedicinePhoto;
import com.jala.backend.medicinephoto.mapper.MedicinePhotoMapper;
import com.jala.backend.medicinephoto.repository.MedicinePhotoRepository;
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

import java.time.LocalDate;
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

    private final StorageService storageService;

    @Override
    @Transactional
    public MedicinePhotoResponse uploadPhoto(
            CreateMedicinePhotoRequest request) {

        MedicineEntry medicine =
                getMedicineOrThrow(request.getMedicineEntryId());

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

        var pondCycle = medicine.getPondCycle();

        var pond = pondCycle.getPond();

        var site = pond.getSite();

        long sequence =
                repository.countByMedicineEntryId(
                        medicine.getId()) + 1;

        String extension =
                getExtension(
                        request.getFile().getOriginalFilename());

        String fileName =
                FileNameGenerator.generateEntityFileName(
                        site.getSiteCode(),
                        site.getSiteName(),
                        LocalDate.now(),
                        StorageModule.MEDICINE,
                        (int) sequence,
                        extension
                );

        log.info("Generated filename : {}", fileName);

        String photoUrl =
                storageService.upload(
                        request.getFile(),
                        StorageFolder.MEDICINE,
                        medicine.getId().toString(),
                        fileName
                );

        log.info("Uploaded to Storage : {}", photoUrl);

        MedicinePhoto photo =
                new MedicinePhoto();

        photo.setMedicineEntry(medicine);

        photo.setFileName(fileName);

        photo.setFilePath(photoUrl);

        photo.setContentType(
                request.getFile().getContentType());

        photo.setFileSize(
                request.getFile().getSize());

        photo.setUploadedBy(user);

        photo.setUploadedAt(LocalDateTime.now());

        MedicinePhoto saved =
                repository.save(photo);

        log.info("Medicine photo {} uploaded successfully.",
                saved.getId());

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

    private MedicineEntry getMedicineOrThrow(
            UUID id) {

        return medicineRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Medicine entry not found."));
    }

    private String getExtension(
            String fileName) {

        int index = fileName.lastIndexOf('.');

        if (index == -1) {
            return "jpg";
        }

        return fileName.substring(index + 1);
    }
}