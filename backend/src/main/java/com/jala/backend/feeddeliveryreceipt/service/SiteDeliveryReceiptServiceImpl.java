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
import com.jala.backend.user.entity.User;
import com.jala.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    private final UserRepository userRepository;

    @Override
    @Transactional
    public SiteDeliveryReceiptResponse uploadReceipt(
            CreateSiteDeliveryReceiptRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository
                .findByEmployeeCode(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found."));

        SiteDelivery siteDelivery =
                siteDeliveryRepository
                        .findById(request.getSiteDeliveryId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Site delivery not found."));

        SiteDeliveryReceipt receipt =
                mapper.toEntity(request);

        receipt.setSiteDelivery(siteDelivery);

        receipt.setUploadedBy(user);

        receipt.setUploadedAt(LocalDateTime.now());

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

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository
                .findByEmployeeCode(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found."));

        SiteDeliveryReceipt receipt =
                getReceiptOrThrow(receiptId);

        receipt.setStatus(
                FeedDeliveryStatus.CANCELLED);

        receipt.setCancelledBy(user);

        receipt.setCancelledAt(LocalDateTime.now());

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

        receipt.setStatus(
                FeedDeliveryStatus.ACTIVE);

        receipt.setRestoredAt(
                LocalDateTime.now());

        repository.save(receipt);

        log.info("Receipt {} restored.",
                receipt.getId());
    }

    private SiteDeliveryReceipt getReceiptOrThrow(
            UUID id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Receipt not found."));
    }
}