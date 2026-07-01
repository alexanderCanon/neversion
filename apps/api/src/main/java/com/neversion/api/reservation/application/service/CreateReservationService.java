package com.neversion.api.reservation.application.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.loyalty.application.port.in.RedeemPointsUseCase;
import com.neversion.api.reservation.application.port.in.CreateReservationUseCase;
import com.neversion.api.reservation.application.port.in.ReservationItemCommand;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.reservation.domain.service.ReservationPricingService;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

import com.neversion.api.shared.domain.model.enums.AccountPreference;

/**
 * UC1: Create Reservation (Checkout) — US-033.
 * <p>
 * Full EPIC-05 implementation:
 * - Resolves client → vendor for multi-tenancy
 * - Fetches service prices from catalog (BR-14)
 * - Validates profile availability per service (US-033 AC)
 * - Computes discount using vendor's discount_cfg tiers (BR-13)
 * - Sets payment method and 1-hour expiration
 * </p>
 */
@Service
public class CreateReservationService implements CreateReservationUseCase {

    private static final int EXPIRATION_MINUTES = 60;

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final ReservationPricingService reservationPricingService;
    private final ClientRepositoryPort clientRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final RedeemPointsUseCase redeemPointsUseCase;

    public CreateReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            ReservationPricingService reservationPricingService,
            ClientRepositoryPort clientRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            RedeemPointsUseCase redeemPointsUseCase) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.reservationPricingService = reservationPricingService;
        this.clientRepositoryPort = clientRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.redeemPointsUseCase = redeemPointsUseCase;
    }

    @Override
    @Transactional
    public Reservation create(UUID clientUuid, List<ReservationItemCommand> items,
                               String paymentMethod, AccountPreference accountPreference, String notes,
                               Long pointsToRedeem) {

        // 1. Resolve client and vendor for multi-tenancy
        var user = userRepositoryPort.findByExternalId(clientUuid.toString())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + clientUuid));

        Client client = clientRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client record not found for userId: " + user.getId()));

        Long vendorId = client.getVendorId();
        if (vendorId == null) {
            throw new BusinessRuleException("Client is not linked to any vendor");
        }

        // 2. Load vendor for discount_cfg
        Vendor vendor = vendorRepositoryPort.findByInternalId(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found for client's vendor_id: " + vendorId));

        // 3. Build reservation details — resolve prices from service catalog (BR-14)
        List<ReservationDetail> detailsToSave = new ArrayList<>();
        int profileItemCount = 0;
        Set<UUID> seenServiceUuids = new HashSet<>();
        boolean hasFullAccount = false;

        for (ReservationItemCommand item : items) {
            com.neversion.api.service.domain.model.Service service =
                    serviceRepositoryPort.findById(item.serviceUuid())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Service not found with uuid: " + item.serviceUuid()));

            // BR-13 v2: no duplicate services in the cart
            if (!seenServiceUuids.add(item.serviceUuid())) {
                throw new BusinessRuleException(
                        "Duplicate service in cart: " + service.getName()
                                + ". Each service can only appear once.");
            }

            SaleMode saleMode = item.saleMode() != null ? item.saleMode() : SaleMode.BY_PROFILE;

            // BR-13 v2: FULL_ACCOUNT must be the only item in the cart
            if (saleMode == SaleMode.FULL_ACCOUNT) {
                if (items.size() > 1) {
                    throw new BusinessRuleException(
                            "A full account purchase cannot be combined with other services "
                                    + "in the same reservation. Service: " + service.getName());
                }
                hasFullAccount = true;
            } else {
                profileItemCount++;
            }

            // BR-US033-01: Validate profile availability
            long availableProfiles = profileRepositoryPort
                    .countAvailableByServiceIdAndVendorId(service.getId(), vendorId);
            if (availableProfiles < item.qty()) {
                throw new BusinessRuleException(
                        "Not enough available profiles for service '" + service.getName()
                                + "'. Available: " + availableProfiles + ", requested: " + item.qty());
            }

            // BR-14: use priceProfile or priceFull depending on sale mode
            BigDecimal unitPrice;
            if (saleMode == SaleMode.FULL_ACCOUNT) {
                unitPrice = service.getPriceFull() != null
                        ? service.getPriceFull()
                        : BigDecimal.ZERO;
            } else {
                unitPrice = service.getPriceProfile() != null
                        ? service.getPriceProfile()
                        : BigDecimal.ZERO;
            }

            detailsToSave.add(new ReservationDetail(
                    null,
                    null,
                    null,
                    service.getId(),
                    item.qty(),
                    unitPrice,
                    null));
        }

        // BR-13 v2: max 4 profile services (hard cap)
        if (profileItemCount > 4) {
            throw new BusinessRuleException(
                    "Cannot purchase more than 4 profile services in a single reservation. "
                            + "For 5 or more, please contact the vendor via WhatsApp.");
        }

        // 4. Calculate pricing using domain service (BR-13 v2)
        // Discount applies only to BY_PROFILE items. FULL_ACCOUNT items pay full price.
        BigDecimal grossTotal = reservationPricingService.calculateGrossTotal(detailsToSave);
        BigDecimal discount;
        if (hasFullAccount) {
            // Single full account — no combo discount
            discount = BigDecimal.ZERO;
        } else {
            discount = reservationPricingService.calculateComboDiscount(
                    grossTotal, profileItemCount, vendor.getDiscountCfg());
        }
        BigDecimal finalTotal = reservationPricingService.calculateFinalTotal(grossTotal, discount);

        // 4b. Validate points redemption request against the reservation total (BR: rewards program)
        long redeemPoints = pointsToRedeem != null ? pointsToRedeem : 0L;
        if (redeemPoints < 0) {
            throw new BusinessRuleException("pointsToRedeem must not be negative.");
        }
        if (redeemPoints > 0 && BigDecimal.valueOf(redeemPoints).compareTo(finalTotal) > 0) {
            throw new BusinessRuleException(
                    "Points redeemed cannot exceed the reservation total. Total: " + finalTotal
                            + ", points requested: " + redeemPoints);
        }

        // 5. Build and persist the reservation
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expirationDate = now.plusMinutes(EXPIRATION_MINUTES);

        Reservation reservation = Reservation.builder()
                .clientId(client.getId())
                .clientUuid(clientUuid)
                .vendorId(vendorId)
                .status(ReservationStatus.PENDING)
                .discount(discount)
                .total(finalTotal)
                .paymentMethod(paymentMethod)
                .accountPreference(accountPreference)
                .notes(notes)
                .expirationDate(expirationDate.toInstant())
                .build();

        Reservation savedReservation = reservationRepositoryPort.save(reservation);

        // 5b. Redeem points (if requested) — recorded against the now-persisted reservation id
        if (redeemPoints > 0) {
            BigDecimal pointsDiscountAmount = redeemPointsUseCase.redeemForReservation(
                    savedReservation.getId(), client.getId(), vendorId, redeemPoints);
            savedReservation.setPointsRedeemed(redeemPoints);
            savedReservation.setPointsDiscount(pointsDiscountAmount);
            savedReservation.setTotal(finalTotal.subtract(pointsDiscountAmount));
            savedReservation = reservationRepositoryPort.update(savedReservation);
        }

        // 6. Persist each detail linked to the saved reservation
        List<ReservationDetail> savedDetails = new ArrayList<>();
        for (ReservationDetail detail : detailsToSave) {
            ReservationDetail linked = new ReservationDetail(
                    null,
                    null, // uuid generated on persist
                    savedReservation.getId(),
                    detail.serviceId(),
                    detail.qty(),
                    detail.unitPrice(),
                    null);
            savedDetails.add(reservationRepositoryPort.saveDetail(linked));
        }

        savedReservation.setDetails(savedDetails);
        return savedReservation;
    }
}
