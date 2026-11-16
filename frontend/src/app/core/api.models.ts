export interface Product {
  id: string; sku: string; name: string; category: string; description: string | null;
  price: number; active: boolean; createdAt: string; updatedAt: string;
}
export interface ApiResponse<T> { success: boolean; message: string; data: T; timestamp: string; }
export interface LoginResponse { accessToken: string; refreshToken: string; tokenType: string; expiresIn: number; userId: string; email: string; }
export interface ProductPayload { sku: string; name: string; category: string; description: string; price: number; active?: boolean; }
export interface CartItem { productId: string; quantity: number; }
export interface Cart { cartId: string | null; userId: string; items: CartItem[]; updatedAt: string | null; }
export interface OrderItem { productId: string; productName: string; quantity: number; unitPrice: number; lineTotal: number; }
export interface Order { id: string; userId: string; status: string; items: OrderItem[]; totalAmount: number; createdAt: string; updatedAt: string; }
export interface Notification { id: string; orderId: string; eventType: string; message: string; read: boolean; createdAt: string; }
export type ProductCategory = 'All' | string;
