package com.product.ecommerce.inventory;

import com.product.ecommerce.common.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {
  private final InventoryStockRepository stockRepository;
  private final InventoryReservationRepository reservationRepository;
  private final InventoryAdjustmentRepository adjustmentRepository;

  public InventoryService(
      InventoryStockRepository stockRepository,
      InventoryReservationRepository reservationRepository,
      InventoryAdjustmentRepository adjustmentRepository) {
    this.stockRepository = stockRepository;
    this.reservationRepository = reservationRepository;
    this.adjustmentRepository = adjustmentRepository;
  }

  @Transactional(readOnly = true)
  public InventoryResponse getStock(UUID productId) {
    return InventoryResponse.from(
        stockRepository
            .findById(productId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Inventory not found for product: " + productId)));
  }

  public InventoryResponse adjustStock(UUID productId, StockAdjustmentRequest request) {
    if (request.quantityDelta() == 0) {
      throw new IllegalArgumentException("Quantity delta must not be zero");
    }
    InventoryAdjustment existing =
        adjustmentRepository.findById(request.adjustmentId()).orElse(null);
    if (existing != null) {
      if (!existing.getProductId().equals(productId)
          || existing.getQuantityDelta() != request.quantityDelta()) {
        throw new InventoryConflictException(
            "Adjustment id was already used with different details");
      }
      return getStock(productId);
    }

    InventoryStock stock =
        stockRepository
            .findByProductIdForUpdate(productId)
            .orElseGet(() -> new InventoryStock(productId));
    stock.adjust(request.quantityDelta());
    stockRepository.save(stock);
    try {
      adjustmentRepository.save(
          new InventoryAdjustment(request.adjustmentId(), productId, request.quantityDelta()));
    } catch (DataIntegrityViolationException exception) {
      throw new InventoryConflictException("Adjustment id was already used");
    }
    return InventoryResponse.from(stock);
  }

  public ReservationResponse reserve(UUID productId, ReservationRequest request) {
    InventoryReservation existing =
        reservationRepository.findById(request.reservationId()).orElse(null);
    if (existing != null) {
      if (!existing.getProductId().equals(productId)
          || existing.getQuantity() != request.quantity()) {
        throw new InventoryConflictException(
            "Reservation id was already used with different details");
      }
      return ReservationResponse.from(existing);
    }

    InventoryStock stock = lockStock(productId);
    stock.reserve(request.quantity());
    stockRepository.save(stock);
    InventoryReservation reservation =
        new InventoryReservation(request.reservationId(), productId, request.quantity());
    try {
      return ReservationResponse.from(reservationRepository.save(reservation));
    } catch (DataIntegrityViolationException exception) {
      throw new InventoryConflictException("Reservation id was already used");
    }
  }

  public InventoryResponse release(UUID productId, UUID reservationId) {
    InventoryReservation reservation = lockReservation(reservationId);
    validateReservationProduct(productId, reservation);
    if (reservation.getStatus() == ReservationStatus.RELEASED) {
      return getStock(productId);
    }
    if (reservation.getStatus() == ReservationStatus.COMMITTED) {
      throw new InventoryConflictException("Committed reservation cannot be released");
    }
    InventoryStock stock = lockStock(productId);
    stock.release(reservation.getQuantity());
    reservation.release();
    stockRepository.save(stock);
    reservationRepository.save(reservation);
    return InventoryResponse.from(stock);
  }

  public InventoryResponse commit(UUID productId, UUID reservationId) {
    InventoryReservation reservation = lockReservation(reservationId);
    validateReservationProduct(productId, reservation);
    if (reservation.getStatus() == ReservationStatus.COMMITTED) {
      return getStock(productId);
    }
    if (reservation.getStatus() == ReservationStatus.RELEASED) {
      throw new InventoryConflictException("Released reservation cannot be committed");
    }
    InventoryStock stock = lockStock(productId);
    stock.commit(reservation.getQuantity());
    reservation.commit();
    stockRepository.save(stock);
    reservationRepository.save(reservation);
    return InventoryResponse.from(stock);
  }

  private InventoryStock lockStock(UUID productId) {
    return stockRepository
        .findByProductIdForUpdate(productId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Inventory not found for product: " + productId));
  }

  private InventoryReservation lockReservation(UUID reservationId) {
    return reservationRepository
        .findByReservationIdForUpdate(reservationId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Reservation not found: " + reservationId));
  }

  private void validateReservationProduct(UUID productId, InventoryReservation reservation) {
    if (!reservation.getProductId().equals(productId)) {
      throw new ResourceNotFoundException("Reservation not found for product: " + productId);
    }
  }
}
