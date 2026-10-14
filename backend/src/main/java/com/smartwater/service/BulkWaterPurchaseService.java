package com.smartwater.service;

import com.smartwater.dto.BulkWaterPurchaseDto;
import com.smartwater.dto.CreateBulkPurchaseRequest;
import com.smartwater.entity.BulkWaterPurchase;
import com.smartwater.entity.PurchaseType;
import com.smartwater.repository.BulkWaterPurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BulkWaterPurchaseService {

    private final BulkWaterPurchaseRepository purchaseRepository;

    @Transactional
    public BulkWaterPurchaseDto createPurchase(CreateBulkPurchaseRequest request, Long userId) {
        BigDecimal volume = request.getVolumeKl();
        BigDecimal totalCost = request.getTotalCost();
        BigDecimal unitCost = (volume.compareTo(BigDecimal.ZERO) > 0)
                ? totalCost.divide(volume, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BulkWaterPurchase purchase = BulkWaterPurchase.builder()
                .apartmentId(request.getApartmentId())
                .billingCycleId(request.getBillingCycleId())
                .purchaseDate(request.getPurchaseDate())
                .supplier(request.getSupplier())
                .purchaseType(request.getPurchaseType() != null ? request.getPurchaseType() : PurchaseType.TANKER)
                .volumeKl(volume)
                .totalCost(totalCost)
                .unitCost(unitCost)
                .invoiceNumber(request.getInvoiceNumber())
                .notes(request.getNotes())
                .createdById(userId)
                .build();

        BulkWaterPurchase saved = purchaseRepository.save(purchase);
        return mapToDto(saved);
    }

    public List<BulkWaterPurchaseDto> getPurchasesByApartment(Long apartmentId) {
        return purchaseRepository.findByApartmentIdOrderByPurchaseDateDesc(apartmentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<BulkWaterPurchaseDto> getPurchasesByCycle(Long billingCycleId) {
        return purchaseRepository.findByBillingCycleId(billingCycleId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private BulkWaterPurchaseDto mapToDto(BulkWaterPurchase p) {
        return BulkWaterPurchaseDto.builder()
                .id(p.getId())
                .apartmentId(p.getApartmentId())
                .billingCycleId(p.getBillingCycleId())
                .purchaseDate(p.getPurchaseDate())
                .supplier(p.getSupplier())
                .purchaseType(p.getPurchaseType())
                .volumeKl(p.getVolumeKl())
                .totalCost(p.getTotalCost())
                .unitCost(p.getUnitCost())
                .invoiceNumber(p.getInvoiceNumber())
                .notes(p.getNotes())
                .createdById(p.getCreatedById())
                .build();
    }
}
