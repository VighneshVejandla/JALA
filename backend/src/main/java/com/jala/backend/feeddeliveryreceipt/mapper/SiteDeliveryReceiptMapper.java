package com.jala.backend.feeddeliveryreceipt.mapper;

import com.jala.backend.feeddeliveryreceipt.dto.request.CreateSiteDeliveryReceiptRequest;
import com.jala.backend.feeddeliveryreceipt.dto.response.SiteDeliveryReceiptResponse;
import com.jala.backend.feeddeliveryreceipt.entity.SiteDeliveryReceipt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SiteDeliveryReceiptMapper {

    SiteDeliveryReceipt toEntity(
            CreateSiteDeliveryReceiptRequest request);

    @Mapping(target = "siteDeliveryId",
            source = "siteDelivery.id")
    @Mapping(target = "uploadedByEmployeeCode",
            source = "uploadedBy.employeeCode")
    SiteDeliveryReceiptResponse toResponse(
            SiteDeliveryReceipt receipt);
}