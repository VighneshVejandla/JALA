package com.jala.backend.feeddeliveryreceipt.service;

import com.jala.backend.feeddeliveryreceipt.dto.request.CancelSiteDeliveryReceiptRequest;
import com.jala.backend.feeddeliveryreceipt.dto.request.CreateSiteDeliveryReceiptRequest;
import com.jala.backend.feeddeliveryreceipt.dto.response.SiteDeliveryReceiptResponse;

import java.util.List;
import java.util.UUID;

public interface SiteDeliveryReceiptService {

    SiteDeliveryReceiptResponse uploadReceipt(
            CreateSiteDeliveryReceiptRequest request);

    List<SiteDeliveryReceiptResponse> getReceipts(
            UUID siteDeliveryId);

    void cancelReceipt(
            UUID receiptId,
            CancelSiteDeliveryReceiptRequest request);

    void restoreReceipt(
            UUID receiptId);
}