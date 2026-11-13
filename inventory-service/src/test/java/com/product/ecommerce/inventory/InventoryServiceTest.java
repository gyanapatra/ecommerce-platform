package com.product.ecommerce.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
  @Mock private InventoryStockRepository stockRepository;
  @Mock private InventoryReservationRepository reservationRepository;
  @Mock private InventoryAdjustmentRepository adjustmentRepository;
  @InjectMocks private InventoryService inventoryService;

  @Test
  void adjustmentAddsStockAndIsIdempotent() {
    UUID productId = UUID.randomUUID();
    UUID adjustmentId = UUID.randomUUID();
    InventoryStock stock = new InventoryStock(productId);
    when(adjustmentRepository.findById(adjustmentId)).thenReturn(Optional.empty(),
        Optional.of(new InventoryAdjustment(adjustmentId, productId, 10)));
    when(stockRepository.findById(productId)).thenReturn(Optional.of(stock));
    when(stockRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(InventoryStock.class))).thenAnswer(call -> call.getArgument(0));
    when(adjustmentRepository.save(any(InventoryAdjustment.class))).thenAnswer(call -> call.getArgument(0));

    InventoryResponse result = inventoryService.adjustStock(productId,
        new StockAdjustmentRequest(adjustmentId, 10));
    InventoryResponse retry = inventoryService.adjustStock(productId,
        new StockAdjustmentRequest(adjustmentId, 10));

    assertEquals(10, result.onHand());
    assertEquals(10, retry.onHand());
    assertEquals(10, retry.available());
    verify(adjustmentRepository, times(1)).save(any(InventoryAdjustment.class));
  }

  @Test
  void reservationReducesAvailableWithoutReducingOnHand() {
    UUID productId = UUID.randomUUID();
    UUID reservationId = UUID.randomUUID();
    InventoryStock stock = new InventoryStock(productId);
    stock.adjust(8);
    when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());
    when(stockRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(InventoryStock.class))).thenAnswer(call -> call.getArgument(0));
    when(reservationRepository.save(any(InventoryReservation.class))).thenAnswer(call -> call.getArgument(0));

    ReservationResponse result = inventoryService.reserve(productId,
        new ReservationRequest(reservationId, 3));

    assertEquals(ReservationStatus.ACTIVE, result.status());
    assertEquals(8, stock.getOnHand());
    assertEquals(3, stock.getReserved());
    assertEquals(5, stock.getAvailable());
  }

  @Test
  void repeatingReservationDoesNotReserveStockTwice() {
    UUID productId = UUID.randomUUID();
    UUID reservationId = UUID.randomUUID();
    InventoryStock stock = new InventoryStock(productId);
    stock.adjust(8);
    InventoryReservation reservation = new InventoryReservation(reservationId, productId, 3);
    when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty(), Optional.of(reservation));
    when(stockRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(InventoryStock.class))).thenAnswer(call -> call.getArgument(0));
    when(reservationRepository.save(any(InventoryReservation.class))).thenAnswer(call -> call.getArgument(0));

    inventoryService.reserve(productId, new ReservationRequest(reservationId, 3));
    ReservationResponse retry = inventoryService.reserve(productId, new ReservationRequest(reservationId, 3));

    assertEquals(ReservationStatus.ACTIVE, retry.status());
    assertEquals(3, stock.getReserved());
    verify(stockRepository, times(1)).findByProductIdForUpdate(productId);
    verify(reservationRepository, times(1)).save(any(InventoryReservation.class));
  }
  @Test
  void reservationCannotExceedAvailableStock() {
    UUID productId = UUID.randomUUID();
    UUID reservationId = UUID.randomUUID();
    InventoryStock stock = new InventoryStock(productId);
    stock.adjust(2);
    when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());
    when(stockRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(stock));

    assertThrows(InsufficientStockException.class,
        () -> inventoryService.reserve(productId, new ReservationRequest(reservationId, 3)));
    verify(reservationRepository, never()).save(any(InventoryReservation.class));
    assertEquals(2, stock.getAvailable());
  }

  @Test
  void commitConsumesReservedStock() {
    UUID productId = UUID.randomUUID();
    UUID reservationId = UUID.randomUUID();
    InventoryStock stock = new InventoryStock(productId);
    stock.adjust(6);
    stock.reserve(2);
    InventoryReservation reservation = new InventoryReservation(reservationId, productId, 2);
    when(reservationRepository.findByReservationIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));
    when(stockRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(InventoryStock.class))).thenAnswer(call -> call.getArgument(0));
    when(reservationRepository.save(any(InventoryReservation.class))).thenAnswer(call -> call.getArgument(0));

    InventoryResponse result = inventoryService.commit(productId, reservationId);

    assertEquals(ReservationStatus.COMMITTED, reservation.getStatus());
    assertEquals(4, result.onHand());
    assertEquals(0, result.reserved());
    assertEquals(4, result.available());
  }

  @Test
  void releaseRestoresAvailableStock() {
    UUID productId = UUID.randomUUID();
    UUID reservationId = UUID.randomUUID();
    InventoryStock stock = new InventoryStock(productId);
    stock.adjust(6);
    stock.reserve(2);
    InventoryReservation reservation = new InventoryReservation(reservationId, productId, 2);
    when(reservationRepository.findByReservationIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));
    when(stockRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(InventoryStock.class))).thenAnswer(call -> call.getArgument(0));
    when(reservationRepository.save(any(InventoryReservation.class))).thenAnswer(call -> call.getArgument(0));

    InventoryResponse result = inventoryService.release(productId, reservationId);

    assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
    assertEquals(6, result.onHand());
    assertEquals(0, result.reserved());
    assertEquals(6, result.available());
  }
}