package com.jala.backend.feeddeliveryreceipt.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.feeddelivery.entity.SiteDelivery;
import com.jala.backend.feeddelivery.enums.FeedDeliveryStatus;
import com.jala.backend.feeddelivery.repository.SiteDeliveryRepository;
import com.jala.backend.feeddeliveryreceipt.dto.request.CancelSiteDeliveryReceiptRequest;
import com.jala.backend.feeddeliveryreceipt.dto.request.CreateSiteDeliveryReceiptRequest;
import com.jala.backend.feeddeliveryreceipt.dto.response.SiteDeliveryReceiptResponse;
import com.jala.backend.feeddeliveryreceipt.entity.SiteDeliveryReceipt;
import com.jala.backend.feeddeliveryreceipt.mapper.SiteDeliveryReceiptMapper;
import com.jala.backend.feeddeliveryreceipt.repository.SiteDeliveryReceiptRepository;
import com.jala.backend.security.service.CurrentUserService;
import com.jala.backend.site.entity.Site;
import com.jala.backend.siteaccess.service.SiteAccessService;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteDeliveryReceiptServiceImplTest {

    private static final byte[] PNG =
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A};

    @Mock
    private SiteDeliveryReceiptRepository repository;

    @Mock
    private SiteDeliveryRepository siteDeliveryRepository;

    @Mock
    private SiteDeliveryReceiptMapper mapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private SiteAccessService siteAccessService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private SiteDeliveryReceiptServiceImpl service;

    private Site site;
    private SiteDelivery siteDelivery;
    private SiteDeliveryReceipt receipt;
    private User user;

    @BeforeEach
    void setUp() {
        site = Site.builder()
                .id(UUID.randomUUID()).siteCode("S-001").siteName("Site").build();
        siteDelivery = new SiteDelivery();
        siteDelivery.setId(UUID.randomUUID());
        siteDelivery.setSite(site);
        receipt = new SiteDeliveryReceipt();
        receipt.setId(UUID.randomUUID());
        receipt.setSiteDelivery(siteDelivery);
        receipt.setStatus(FeedDeliveryStatus.ACTIVE);
        user = User.builder().id(UUID.randomUUID()).build();
    }

    private CreateSiteDeliveryReceiptRequest uploadRequest() {
        CreateSiteDeliveryReceiptRequest r =
                new CreateSiteDeliveryReceiptRequest();
        r.setSiteDeliveryId(siteDelivery.getId());
        r.setFile(new MockMultipartFile(
                "file", "receipt.png", "image/png", PNG));
        return r;
    }

    @Test
    @DisplayName("uploadReceipt validates access, stores and persists")
    void uploadReceipt_success() {
        CreateSiteDeliveryReceiptRequest request = uploadRequest();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(siteDeliveryRepository.findById(siteDelivery.getId()))
                .thenReturn(Optional.of(siteDelivery));
        when(repository.countBySiteDeliveryIdAndStatus(
                siteDelivery.getId(), FeedDeliveryStatus.ACTIVE)).thenReturn(0L);
        when(storageService.upload(any(), any(), any(), any()))
                .thenReturn("https://storage/receipts/x.png");
        when(mapper.toEntity(request)).thenReturn(receipt);
        when(repository.save(receipt)).thenReturn(receipt);
        when(mapper.toResponse(receipt))
                .thenReturn(new SiteDeliveryReceiptResponse());

        service.uploadReceipt(request);

        verify(siteAccessService).checkSiteAccess(site.getId());
        verify(repository).save(receipt);
    }

    @Test
    @DisplayName("uploadReceipt rejects an unknown site delivery")
    void uploadReceipt_deliveryNotFound() {
        CreateSiteDeliveryReceiptRequest request = uploadRequest();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(siteDeliveryRepository.findById(siteDelivery.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.uploadReceipt(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Site delivery not found.");

        verifyNoInteractions(storageService);
    }

    @Test
    @DisplayName("cancelReceipt records who/why and flips status")
    void cancelReceipt_success() {
        CancelSiteDeliveryReceiptRequest request =
                new CancelSiteDeliveryReceiptRequest();
        request.setCancellationReason("blurry");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(repository.findById(receipt.getId()))
                .thenReturn(Optional.of(receipt));

        service.cancelReceipt(receipt.getId(), request);

        verify(siteAccessService).checkSiteAccess(site.getId());
        assertThat(receipt.getStatus()).isEqualTo(FeedDeliveryStatus.CANCELLED);
        assertThat(receipt.getCancellationReason()).isEqualTo("blurry");
    }

    @Test
    @DisplayName("restoreReceipt reactivates a cancelled receipt")
    void restoreReceipt_success() {
        receipt.setStatus(FeedDeliveryStatus.CANCELLED);
        when(repository.findById(receipt.getId()))
                .thenReturn(Optional.of(receipt));

        service.restoreReceipt(receipt.getId());

        assertThat(receipt.getStatus()).isEqualTo(FeedDeliveryStatus.ACTIVE);
        verify(repository).save(receipt);
    }

    @Test
    @DisplayName("getReceipts rejects an unknown site delivery")
    void getReceipts_notFound() {
        UUID id = UUID.randomUUID();
        when(siteDeliveryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReceipts(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Site delivery not found.");
    }

    @Test
    @DisplayName("uploadReceipt is rejected after the 6-hour window")
    void uploadReceipt_windowClosed() {
        var fd = com.jala.backend.feeddelivery.entity.FeedDelivery.builder()
                .id(UUID.randomUUID())
                .deliveredAt(
                        com.jala.backend.common.util.DateTimeUtil.now()
                                .minusHours(7))
                .build();
        siteDelivery.setFeedDelivery(fd);

        when(siteDeliveryRepository.findById(siteDelivery.getId()))
                .thenReturn(Optional.of(siteDelivery));

        var req = new CreateSiteDeliveryReceiptRequest();
        req.setSiteDeliveryId(siteDelivery.getId());

        assertThatThrownBy(() -> service.uploadReceipt(req))
                .isInstanceOf(
                        com.jala.backend.common.exception.BadRequestException.class)
                .hasMessageContaining("6 hours");
    }
}
