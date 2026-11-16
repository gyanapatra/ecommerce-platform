import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ApiResponse, Cart, MemberProfile, Notification, Order, Payment } from './api.models';

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
  payOrder(orderId: string): Observable<Payment> {
    return this.http.post<ApiResponse<Payment>>('/api/v1/payments', { orderId, paymentMethod: 'MOCK' }, { headers: new HttpHeaders({ 'Idempotency-Key': orderId }) }).pipe(map((r) => r.data));
  }
  listOrders(): Observable<Order[]> { return this.http.get<ApiResponse<Order[]>>('/api/v1/orders').pipe(map((r) => r.data)); }
  getOrder(id: string): Observable<Order> { return this.http.get<ApiResponse<Order>>(`/api/v1/orders/${id}`).pipe(map((r) => r.data)); }
  cancelOrder(id: string): Observable<Order> { return this.http.post<ApiResponse<Order>>(`/api/v1/orders/${id}/cancel`, {}).pipe(map((r) => r.data)); }
  getMemberProfile(): Observable<MemberProfile> { return this.http.get<ApiResponse<MemberProfile>>('/api/v1/members/me').pipe(map((r) => r.data)); }
  saveMemberProfile(profile: Pick<MemberProfile, 'firstName' | 'lastName' | 'phoneNumber'>): Observable<MemberProfile> {
    return this.http.put<ApiResponse<MemberProfile>>('/api/v1/members/me', profile).pipe(map((r) => r.data));
  }
  notifications(): Observable<Notification[]> { return this.http.get<ApiResponse<Notification[]>>('/api/v1/notifications').pipe(map((r) => r.data)); }
  markRead(id: string): Observable<Notification> { return this.http.put<ApiResponse<Notification>>(`/api/v1/notifications/${id}/read`, {}).pipe(map((r) => r.data)); }
}
