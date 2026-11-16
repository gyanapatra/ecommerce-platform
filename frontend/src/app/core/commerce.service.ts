import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ApiResponse, Cart, Notification, Order } from './api.models';

@Injectable({ providedIn: 'root' })
export class CommerceService {
  constructor(private readonly http: HttpClient) {}
  getCart(): Observable<Cart> { return this.http.get<ApiResponse<Cart>>('/api/v1/cart').pipe(map((r) => r.data)); }
  addItem(productId: string, quantity = 1): Observable<Cart> { return this.http.post<ApiResponse<Cart>>('/api/v1/cart/items', { productId, quantity }).pipe(map((r) => r.data)); }
  setQuantity(productId: string, quantity: number): Observable<Cart> { return this.http.put<ApiResponse<Cart>>(`/api/v1/cart/items/${productId}`, { quantity }).pipe(map((r) => r.data)); }
  removeItem(productId: string): Observable<Cart> { return this.http.delete<ApiResponse<Cart>>(`/api/v1/cart/items/${productId}`).pipe(map((r) => r.data)); }
  clearCart(): Observable<Cart> { return this.http.delete<ApiResponse<Cart>>('/api/v1/cart/items').pipe(map((r) => r.data)); }
  checkout(): Observable<Order> {
    const idempotencyKey = crypto.randomUUID();
    return this.http.post<ApiResponse<Order>>('/api/v1/orders', null, { headers: new HttpHeaders({ 'Idempotency-Key': idempotencyKey }) }).pipe(map((r) => r.data));
  }
  listOrders(): Observable<Order[]> { return this.http.get<ApiResponse<Order[]>>('/api/v1/orders').pipe(map((r) => r.data)); }
  getOrder(id: string): Observable<Order> { return this.http.get<ApiResponse<Order>>(`/api/v1/orders/${id}`).pipe(map((r) => r.data)); }
  cancelOrder(id: string): Observable<Order> { return this.http.post<ApiResponse<Order>>(`/api/v1/orders/${id}/cancel`, {}).pipe(map((r) => r.data)); }
  notifications(): Observable<Notification[]> { return this.http.get<ApiResponse<Notification[]>>('/api/v1/notifications').pipe(map((r) => r.data)); }
  markRead(id: string): Observable<Notification> { return this.http.put<ApiResponse<Notification>>(`/api/v1/notifications/${id}/read`, {}).pipe(map((r) => r.data)); }
}
