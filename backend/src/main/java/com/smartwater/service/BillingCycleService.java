package com.smartwater.service;

import com.smartwater.dto.BillingCycleDto;
import com.smartwater.dto.CreateBillingCycleRequest;
import com.smartwater.dto.HouseholdBillDto;
import com.smartwater.entity.*;
import com.smartwater.exception.InvalidReadingException;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingCycleService {

    private final BillingCycleRepository billingCycleRepository;
    private final BulkWaterPurchaseRepository purchaseRepository;
    private final HouseholdRepository householdRepository;
    private final WaterUsageLogRepository waterUsageLogRepository;
    private final HouseholdBillRepository householdBillRepository;
    private final BillItemRepository billItemRepository;
    private final TariffPlanService tariffPlanService;
    private final SharedCostAllocationService sharedCostAllocationService;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Transactional
    public BillingCycleDto createBillingCycle(CreateBillingCycleRequest request) {
        billingCycleRepository.findByApartmentIdAndPeriodStartAndPeriodEnd(
                request.getApartmentId(), request.getPeriodStart(), request.getPeriodEnd()
        ).ifPresent(c -> {
            throw new InvalidReadingException("A billing cycle already exists for this period.");
        });

        BillingCycle cycle = BillingCycle.builder()
                .apartmentId(request.getApartmentId())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .status(BillingCycleStatus.OPEN)
                .totalWaterProcuredCost(BigDecimal.ZERO)
                .totalConsumptionKl(BigDecimal.ZERO)
                .build();

        BillingCycle saved = billingCycleRepository.save(cycle);
        return mapToDto(saved);
    }

    public BillingCycleDto getBillingCycle(Long id) {
        BillingCycle cycle = billingCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing cycle not found with ID: " + id));
        return mapToDto(cycle);
    }

    public List<BillingCycleDto> getBillingCyclesByApartment(Long apartmentId) {
        return billingCycleRepository.findByApartmentIdOrderByPeriodStartDesc(apartmentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BillingCycleDto calculateBills(Long cycleId) {
        BillingCycle cycle = billingCycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing cycle not found with ID: " + cycleId));

        if (cycle.getStatus() == BillingCycleStatus.FINALIZED || cycle.getStatus() == BillingCycleStatus.ARCHIVED) {
            throw new InvalidReadingException("Cannot recalculate bills: Billing cycle is already finalized or archived.");
        }

        // Clean up any previously calculated unfinalized bills
        householdBillRepository.deleteByBillingCycleId(cycleId);

        // Fetch bulk water purchases in period
        List<BulkWaterPurchase> purchases = purchaseRepository.findByApartmentIdAndPurchaseDateBetween(
                cycle.getApartmentId(), cycle.getPeriodStart(), cycle.getPeriodEnd()
        );

        BigDecimal totalProcuredCost = purchases.stream()
                .map(BulkWaterPurchase::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cycle.setTotalWaterProcuredCost(totalProcuredCost);

        // Allocate shared costs
        List<SharedCostAllocation> allocations = sharedCostAllocationService.allocateSharedWaterCosts(cycle, totalProcuredCost);

        List<Household> households = householdRepository.findByApartmentId(cycle.getApartmentId());
        BigDecimal totalConsumption = BigDecimal.ZERO;
        LocalDate dueDate = LocalDate.now().plusDays(15);

        for (Household hh : households) {
            BigDecimal meteredUsage = waterUsageLogRepository.calculateConsumptionByHouseholdAndDateRange(
                    hh.getId(), cycle.getPeriodStart(), cycle.getPeriodEnd()
            );
            if (meteredUsage == null) {
                meteredUsage = BigDecimal.ZERO;
            }
            totalConsumption = totalConsumption.add(meteredUsage);

            // Progressive tariff charge calculation
            BigDecimal tariffCharge = tariffPlanService.calculateProgressiveTariffCharge(
                    cycle.getApartmentId(), cycle.getPeriodEnd(), meteredUsage
            );

            // Shared cost allocation
            BigDecimal sharedCost = allocations.stream()
                    .filter(a -> a.getHouseholdId().equals(hh.getId()))
                    .map(SharedCostAllocation::getAllocatedAmount)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

            BigDecimal totalPayable = tariffCharge.add(sharedCost).setScale(2, RoundingMode.HALF_UP);

            HouseholdBill bill = HouseholdBill.builder()
                    .billingCycleId(cycle.getId())
                    .householdId(hh.getId())
                    .meteredConsumptionKl(meteredUsage)
                    .progressiveTariffCharge(tariffCharge)
                    .sharedCostAllocation(sharedCost)
                    .sharedAreaAllocation(BigDecimal.ZERO)
                    .adjustments(BigDecimal.ZERO)
                    .totalPayable(totalPayable)
                    .dueDate(dueDate)
                    .status(BillStatus.UNPAID)
                    .items(new ArrayList<>())
                    .build();

            HouseholdBill savedBill = householdBillRepository.save(bill);

            List<BillItem> items = new ArrayList<>();
            items.add(BillItem.builder()
                    .bill(savedBill)
                    .itemType("TARIFF_CHARGE")
                    .description("Progressive Water Tariff Charge (" + meteredUsage + " kL)")
                    .quantity(meteredUsage)
                    .unitPrice(meteredUsage.compareTo(BigDecimal.ZERO) > 0 ? tariffCharge.divide(meteredUsage, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                    .totalAmount(tariffCharge)
                    .build());

            if (sharedCost.compareTo(BigDecimal.ZERO) > 0) {
                items.add(BillItem.builder()
                        .bill(savedBill)
                        .itemType("SHARED_COST")
                        .description("Shared Bulk Water Procurement Cost Allocation")
                        .quantity(BigDecimal.ONE)
                        .unitPrice(sharedCost)
                        .totalAmount(sharedCost)
                        .build());
            }

            billItemRepository.saveAll(items);
            savedBill.setItems(items);
        }

        cycle.setTotalConsumptionKl(totalConsumption);
        cycle.setStatus(BillingCycleStatus.CALCULATING);

        BillingCycle savedCycle = billingCycleRepository.save(cycle);
        return mapToDto(savedCycle);
    }

    @Transactional
    public BillingCycleDto finalizeBillingCycle(Long cycleId) {
        BillingCycle cycle = billingCycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing cycle not found with ID: " + cycleId));

        if (cycle.getStatus() == BillingCycleStatus.FINALIZED || cycle.getStatus() == BillingCycleStatus.ARCHIVED) {
            throw new InvalidReadingException("Billing cycle is already finalized or archived.");
        }

        // If cycle was still OPEN, calculate bills first
        if (cycle.getStatus() == BillingCycleStatus.OPEN) {
            calculateBills(cycleId);
            cycle = billingCycleRepository.findById(cycleId).orElseThrow();
        }

        cycle.setStatus(BillingCycleStatus.FINALIZED);
        BillingCycle finalizedCycle = billingCycleRepository.save(cycle);

        // Dispatch notifications and emails to all residents whose bills were finalized
        List<HouseholdBill> bills = householdBillRepository.findByBillingCycleId(cycleId);
        for (HouseholdBill bill : bills) {
            Household hh = householdRepository.findById(bill.getHouseholdId()).orElse(null);
            String flatNum = hh != null ? hh.getFlatNumber() : "Unit";
            List<User> residents = userRepository.findByHouseholdId(bill.getHouseholdId());

            for (User resident : residents) {
                String title = "📄 Water Bill Published - " + finalizedCycle.getPeriodStart() + " to " + finalizedCycle.getPeriodEnd();
                String message = String.format("Your water bill for Flat %s is now available. Total Payable: ₹%.2f (Due Date: %s). Metered Consumption: %.2f kL.",
                        flatNum, bill.getTotalPayable(), bill.getDueDate(), bill.getMeteredConsumptionKl());

                notificationService.createNotification(
                        resident.getId(),
                        finalizedCycle.getApartmentId(),
                        bill.getHouseholdId(),
                        "BILL_PUBLISHED",
                        title,
                        message
                );

                if (resident.getEmail() != null && !resident.getEmail().isBlank()) {
                    String residentName = resident.getName() != null ? resident.getName() : "Resident";
                    String emailBody = String.format("Dear %s,\n\n" +
                            "Your water bill for Flat %s covering billing cycle %s to %s has been finalized.\n\n" +
                            "Consumption: %.2f kL\n" +
                            "Tariff Charges: ₹%.2f\n" +
                            "Shared Bulk Water Charges: ₹%.2f\n" +
                            "Total Payable: ₹%.2f\n" +
                            "Due Date: %s\n\n" +
                            "You can log in to your resident portal to view and download your itemized PDF invoice.\n\n" +
                            "Smart Water Usage Monitoring and Automated Billing Management Platform",
                            residentName,
                            flatNum,
                            finalizedCycle.getPeriodStart(),
                            finalizedCycle.getPeriodEnd(),
                            bill.getMeteredConsumptionKl(),
                            bill.getProgressiveTariffCharge(),
                            bill.getSharedCostAllocation(),
                            bill.getTotalPayable(),
                            bill.getDueDate());

                    emailService.sendEmail(resident.getEmail(), title, emailBody);
                }
            }
        }

        return mapToDto(finalizedCycle);
    }

    @Transactional
    public BillingCycleDto archiveBillingCycle(Long cycleId) {
        BillingCycle cycle = billingCycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing cycle not found with ID: " + cycleId));

        cycle.setStatus(BillingCycleStatus.ARCHIVED);
        BillingCycle archivedCycle = billingCycleRepository.save(cycle);
        return mapToDto(archivedCycle);
    }

    public List<HouseholdBillDto> getBillsByCycle(Long cycleId) {
        return householdBillRepository.findByBillingCycleId(cycleId).stream()
                .map(this::mapBillToDto)
                .collect(Collectors.toList());
    }

    public List<HouseholdBillDto> getBillsByHousehold(Long householdId) {
        return householdBillRepository.findByHouseholdIdOrderByCreatedAtDesc(householdId).stream()
                .map(this::mapBillToDto)
                .collect(Collectors.toList());
    }

    public HouseholdBillDto getBillById(Long billId) {
        HouseholdBill bill = householdBillRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));
        return mapBillToDto(bill);
    }

    private BillingCycleDto mapToDto(BillingCycle c) {
        return BillingCycleDto.builder()
                .id(c.getId())
                .apartmentId(c.getApartmentId())
                .periodStart(c.getPeriodStart())
                .periodEnd(c.getPeriodEnd())
                .status(c.getStatus())
                .totalWaterProcuredCost(c.getTotalWaterProcuredCost())
                .totalConsumptionKl(c.getTotalConsumptionKl())
                .tariffSnapshotJson(c.getTariffSnapshotJson())
                .build();
    }

    private HouseholdBillDto mapBillToDto(HouseholdBill b) {
        Household hh = householdRepository.findById(b.getHouseholdId()).orElse(null);
        String flatNum = hh != null ? hh.getFlatNumber() : "N/A";
        String apName = "Apartment";
        if (hh != null && hh.getApartment() != null) {
            apName = apartmentRepository.findById(hh.getApartment().getId()).map(Apartment::getName).orElse("Apartment");
        }

        return HouseholdBillDto.builder()
                .id(b.getId())
                .billingCycleId(b.getBillingCycleId())
                .householdId(b.getHouseholdId())
                .flatNumber(flatNum)
                .apartmentName(apName)
                .meteredConsumptionKl(b.getMeteredConsumptionKl())
                .progressiveTariffCharge(b.getProgressiveTariffCharge())
                .sharedCostAllocation(b.getSharedCostAllocation())
                .sharedAreaAllocation(b.getSharedAreaAllocation())
                .adjustments(b.getAdjustments())
                .totalPayable(b.getTotalPayable())
                .dueDate(b.getDueDate())
                .status(b.getStatus())
                .items(b.getItems() != null ? b.getItems().stream().map(i -> com.smartwater.dto.BillItemDto.builder()
                        .id(i.getId())
                        .itemType(i.getItemType())
                        .description(i.getDescription())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .totalAmount(i.getTotalAmount())
                        .build()).collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }
}
