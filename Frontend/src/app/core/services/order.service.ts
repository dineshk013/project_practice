import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, catchError, map, throwError } from 'rxjs';
import { Order, OrderItem, OrderStatus } from '../models/order.model';
import { environment } from '../../../environments/environment';

interface BackendOrderDto {
  id: number;
  orderNumber: string;
  status: string;
  paymentStatus: string;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
  shippingAddress: {
    line1: string;
    line2?: string;
    city: string;
    state: string;
    postalCode: string;
    country?: string;
  };
  items: BackendOrderItemDto[];
  deliveryAgentName?: string;
}

interface BackendOrderItemDto {
  productId: number;
  productName: string;
  productImageUrl?: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = `${environment.apiUrl}/orders`;

  constructor(private httpClient: HttpClient) { }

  private mapBackendOrderToFrontend = (backendOrder: BackendOrderDto): Order => {
  const map: { [k: string]: OrderStatus } = {
    'PENDING': 'processing',
    'PROCESSING': 'processing',
    'CONFIRMED': 'processing',
    'PACKED': 'packed',
    'OUT_FOR_DELIVERY': 'in_transit',
    'SHIPPED': 'in_transit',
    'DELIVERED': 'delivered',
    'COMPLETED': 'delivered',
    'CANCELLED': 'cancelled'
  };

  return {
    id: String(backendOrder.id),
    date: new Date(backendOrder.createdAt).toISOString().split('T')[0],
    status: map[backendOrder.status] ?? 'processing',
    items: backendOrder.items.map(item => ({
      id: String(item.productId),
      name: item.productName,
      quantity: item.quantity,
      price: Number(item.unitPrice)
    })),
    total: Number(backendOrder.totalAmount),
    deliveryAddress: backendOrder.shippingAddress ?
      `${backendOrder.shippingAddress.line1}, ${backendOrder.shippingAddress.city}, ${backendOrder.shippingAddress.state} ${backendOrder.shippingAddress.postalCode}`
      : '-'
  };
};


  getAllOrders(): Observable<Order[]> {
    const headers = {
      'Authorization': 'Bearer ' + localStorage.getItem('token'),
      'X-User-Id': localStorage.getItem('userId') || ''
    };

    return this.httpClient.get<ApiResponse<BackendOrderDto[]>>(`${this.apiUrl}/all`, { headers }).pipe(
      map(response => (response.data || []).map(this.mapBackendOrderToFrontend)),
      catchError(error => {
        console.warn('Backend failed, returning empty list:', error);
        return of([]);
      })
    );
  }

  getUserOrders(userId: string): Observable<Order[]> {
    const headers = {
      'Authorization': 'Bearer ' + localStorage.getItem('token'),
      'X-User-Id': userId
    };

    return this.httpClient.get<ApiResponse<BackendOrderDto[]>>(`${this.apiUrl}/user`, { headers }).pipe(
      map(response => {
        const orders = response.data || [];
        return orders.map(this.mapBackendOrderToFrontend);
      }),
      catchError(error => {
        console.warn('Backend user order fetch failed:', error);
        return of([]);
      })
    );
  }

  getOrderById(orderId: string): Observable<Order | undefined> {
    const headers = {
      'Authorization': 'Bearer ' + localStorage.getItem('token'),
      'X-User-Id': localStorage.getItem('userId') || ''
    };

    return this.httpClient.get<ApiResponse<BackendOrderDto>>(`${this.apiUrl}/${orderId}`, { headers }).pipe(
      map(response => this.mapBackendOrderToFrontend(response.data)),
      catchError(error => {
        console.warn('Order fetch failed:', error);
        return of(undefined);
      })
    );
  }

  createOrder(orderData: {
    items: OrderItem[];
    total: number;
    deliveryAddress: string;
  }): Observable<Order> {
    console.warn('Use checkout flow. createOrder() is deprecated.');
    return throwError(() => new Error('Deprecated API'));
  }

  updateOrderStatus(orderId: string, status: Order['status']): Observable<Order> {
    const headers = {
      'Authorization': 'Bearer ' + localStorage.getItem('token'),
      'X-User-Id': localStorage.getItem('userId') || ''
    };

    const statusMap: { [key: string]: string } = {
      'processing': 'PACKED',
      'packed': 'OUT_FOR_DELIVERY',
      'in_transit': 'DELIVERED',
      'delivered': 'COMPLETED',
      'cancelled': 'CANCELLED'
    };

    return this.httpClient.post<ApiResponse<BackendOrderDto>>(
      `${environment.apiUrl}/admin/orders/${orderId}/status`,
      { status: statusMap[status] },
      { headers }
    ).pipe(
      map(response => this.mapBackendOrderToFrontend(response.data)),
      catchError(error => {
        console.error('Failed to update order status:', error);
        return throwError(() => error);
      })
    );
  }

  cancelOrder(orderId: string): Observable<boolean> {
    const headers = {
      'Authorization': 'Bearer ' + localStorage.getItem('token'),
      'X-User-Id': localStorage.getItem('userId') || ''
    };

    return this.httpClient.post<{ success: boolean; message: string; data: BackendOrderDto }>(
      `${this.apiUrl}/${orderId}/cancel`,
      null,
      { headers }
    ).pipe(
      map(response => response.success),
      catchError(error => {
        console.error('Failed to cancel order:', error);
        return throwError(() => error);
      })
    );
  }
}
