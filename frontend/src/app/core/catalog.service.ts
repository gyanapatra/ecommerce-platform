import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ApiResponse, Product, ProductPayload } from './api.models';

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly endpoint = '/api/v1/products';
  constructor(private readonly http: HttpClient) {}
  list(category?: string): Observable<Product[]> {
    return this.http.get<ApiResponse<Product[]>>(this.endpoint, { params: category ? { category } : {} }).pipe(map((response) => response.data));
  }
  create(payload: ProductPayload): Observable<Product> { return this.http.post<ApiResponse<Product>>(this.endpoint, payload).pipe(map((response) => response.data)); }
  update(id: string, payload: ProductPayload): Observable<Product> { return this.http.put<ApiResponse<Product>>(`${this.endpoint}/${id}`, payload).pipe(map((response) => response.data)); }
  remove(id: string): Observable<void> { return this.http.delete<void>(`${this.endpoint}/${id}`); }
}
