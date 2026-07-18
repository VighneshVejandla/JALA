package com.jala.backend.feeddeliveryreceipt.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.common.util.DateTimeUtil;
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
import com.jala.backend.siteaccess.service.SiteAccessService;
import com.jala.backend.storage.enums.StorageFolder;
import com.jala.backend.storage.enums.StorageModule;
import com.jala.backend.storage.service.StorageService;
import com.jala.backend.storage.util.FileNameGenerator;
import com.jala.backend.storage.util.FileValidationUtil;
import com.jala.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteDeliveryReceiptServiceImpl
        implements SiteDeliveryReceiptService {

    private final SiteDeliveryReceiptRepository repository;

    private final SiteDeliveryRepository siteDeliveryRepository;

    private final SiteDeliveryReceiptMapper mapper;

    private final CurrentUserService currentUserService;

    private final SiteAccessService siteAccessService;

    private final StorageService storageService;

    @Override
    @Transactional
    public SiteDeliveryReceiptResponse uploadReceipt(
            CreateSiteDeliveryReceiptRequest request) {

        User user = currentUserService.getCurrentUser();

        SiteDelivery siteDelivery =
                getSiteDeliveryOrThrow(request.getSiteDeliveryId());

        var site = siteDelivery.getSite();

        siteAccessService.checkSiteAccess(site.getId());

        String extension =
                FileValidationUtil.extractExtension(
                        request.getFile().getOriginalFilename());

        FileValidationUtil.requireImageContent(request.getFile());

        long sequence =
                repository.countBySiteDeliveryIdAndStatus(
                        siteDelivery.getId(),
                        FeedDeliveryStatus.ACTIVE
                ) + 1;

        String fileName =
                FileNameGenerator.generateEntityFileName(
                        site.getSiteCode(),
                        site.getSiteName(),
                        DateTimeUtil.today(),
                        StorageModule.RECEIPT,
                        (int) sequence,
                        extension
                );
        log.info("Generated file name : {}", fileName);

        String photoUrl =
                storageService.upload(
                        request.getFile(),
                        StorageFolder.RECEIPTS,
                        siteDelivery.getId().toString(),
                        fileName
                );

        log.info("Receipt uploaded to Storage : {}", photoUrl);

        SiteDeliveryReceipt receipt =
                mapper.toEntity(request);

        receipt.setPhotoPath(photoUrl);

        receipt.setSiteDelivery(siteDelivery);

        receipt.setUploadedBy(user);

        receipt.setUploadedAt(DateTimeUtil.now());

        receipt.setStatus(FeedDeliveryStatus.ACTIVE);

        SiteDeliveryReceipt saved =
                repository.save(receipt);

        log.info("Receipt {} uploaded successfully.",
                saved.getId());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SiteDeliveryReceiptResponse> getReceipts(
            UUID siteDeliveryId) {

        SiteDelivery siteDelivery =
                getSiteDeliveryOrThrow(siteDeliveryId);

        siteAccessService.checkSiteAccess(
                siteDelivery.getSite().getId());

        return repository
                .findBySiteDeliveryIdAndStatusOrderByUploadedAtAsc(
                        siteDeliveryId,
                        FeedDeliveryStatus.ACTIVE)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancelReceipt(
            UUID receiptId,
            CancelSiteDeliveryReceiptRequest request) {

        User user = currentUserService.getCurrentUser();

        SiteDeliveryReceipt receipt =
                getReceiptOrThrow(receiptId);

        siteAccessService.checkSiteAccess(
                receipt.getSiteDelivery().getSite().getId());

        receipt.setStatus(
                FeedDeliveryStatus.CANCELLED);

        receipt.setCancelledBy(user);

        receipt.setCancelledAt(DateTimeUtil.now());

        receipt.setCancellationReason(
                request.getCancellationReason());

        repository.save(receipt);

        log.info("Receipt {} cancelled.",
                receipt.getId());
    }

    @Override
    @Transactional
    public void restoreReceipt(
            UUID receiptId) {

        SiteDeliveryReceipt receipt =
                getReceiptOrThrow(receiptId);

        siteAccessService.checkSiteAccess(
                receipt.getSiteDelivery().getSite().getId());

        receipt.setStatus(
                FeedDeliveryStatus.ACTIVE);

        receipt.setRestoredAt(
                DateTimeUtil.now());

        repository.save(receipt);

        log.info("Receipt {} restored.",
                receipt.getId());
    }

    private SiteDelivery getSiteDeliveryOrThrow(
            UUID id) {

        return siteDeliveryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Site delivery not found."));
    }

    private SiteDeliveryReceipt getReceiptOrThrow(
            UUID id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Receipt not found."));
    }
}
