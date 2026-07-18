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
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.security.service.CurrentUserService;
import com.jala.backend.site.entity.Site;
import com.jala.backend.siteaccess.service.SiteAccessService;
import com.jala.backend.storage.enums.StorageFolder;
import com.jala.backend.storage.service.StorageService;
import com.jala.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicinePhotoServiceImplTest {

    // 4-byte PNG signature so FileValidationUtil.requireImageContent passes.
    private static final byte[] PNG =
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A};

    @Mock
    private MedicinePhotoRepository repository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private SiteAccessService siteAccessService;

    @Mock
    private MedicinePhotoMapper mapper;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private MedicinePhotoServiceImpl service;

    private MedicineEntry medicine;
    private PondCycle cycle;

    @BeforeEach
    void setUp() {
        Site site = Site.builder()
                .id(UUID.randomUUID())
                .siteCode("S-001")
                .siteName("Site One")
                .build();

        Pond pond = Pond.builder()
                .id(UUID.randomUUID())
                .site(site)
                .pondCode("P-001")
                .build();

        cycle = PondCycle.builder()
                .id(UUID.randomUUID())
                .pond(pond)
                .cycleNumber(1)
                .build();

        medicine = MedicineEntry.builder()
                .id(UUID.randomUUID())
                .pondCycle(cycle)
                .status(MedicineStatus.ACTIVE)
                .build();
    }

    private CreateMedicinePhotoRequest request() {
        CreateMedicinePhotoRequest request = new CreateMedicinePhotoRequest();
        request.setMedicineEntryId(medicine.getId());
        request.setFile(new MockMultipartFile(
                "file", "scan.png", "image/png", PNG));
        return request;
    }

    @Test
    @DisplayName("uploadPhoto validates, stores and persists the photo")
    void uploadPhoto_success() {
        when(medicineRepository.findById(medicine.getId()))
                .thenReturn(Optional.of(medicine));
        when(currentUserService.getCurrentUser())
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(repository.countByMedicineEntryId(medicine.getId())).thenReturn(0L);
        when(storageService.upload(any(), eq(StorageFolder.MEDICINE), any(), any()))
                .thenReturn("https://storage/medicine/x.png");
        when(repository.save(any(MedicinePhoto.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any()))
                .thenReturn(MedicinePhotoResponse.builder().build());

        service.uploadPhoto(request());

        verify(siteAccessService).checkPondCycleAccess(cycle.getId());
        verify(repository).save(any(MedicinePhoto.class));
    }

    @Test
    @DisplayName("uploadPhoto rejects an unknown medicine entry")
    void uploadPhoto_medicineNotFound() {
        CreateMedicinePhotoRequest request = request();
        when(medicineRepository.findById(medicine.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.uploadPhoto(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Medicine entry not found.");

        verifyNoInteractions(storageService);
    }

    @Test
    @DisplayName("uploadPhoto rejects a cancelled medicine entry")
    void uploadPhoto_cancelled() {
        medicine.setStatus(MedicineStatus.CANCELLED);
        CreateMedicinePhotoRequest request = request();
        when(medicineRepository.findById(medicine.getId()))
                .thenReturn(Optional.of(medicine));

        assertThatThrownBy(() -> service.uploadPhoto(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cancelled");

        verify(repository, never()).save(any());
        verifyNoInteractions(storageService);
    }

    @Test
    @DisplayName("getPhotos checks cycle access and maps the page")
    void getPhotos_success() {
        when(medicineRepository.findById(medicine.getId()))
                .thenReturn(Optional.of(medicine));
        when(repository.findByMedicineEntryIdOrderByUploadedAt(
                eq(medicine.getId()), any()))
                .thenReturn(List.of());

        service.getPhotos(medicine.getId(), null, null);

        verify(siteAccessService).checkPondCycleAccess(cycle.getId());
    }
}
