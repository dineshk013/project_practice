import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { LucideAngularModule, Eye, Package } from 'lucide-angular';

interface OrderDto {
  id: number;
  orderNumber?: string;
  customerName?: string;
  status: string;
  paymentStatus: string;
  totalAmount: number;
  createdAt: string;
  user?: {
    id: number;
    fullName: string;
    email: string;
    phone?: string;
  };
  items: Array<{
    productId: number;
    productName: string;
    productImageUrl?: string;
    quantity: number;
    unitPrice: number;
    subtotal: number;
  }>;
  shippingAddress?: {
    line1: string;
    city: string;
    state: string;
    postalCode: string;
  };
}

interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  template: `
  <div class="min-h-screen bg-background py-8">
      <div class="container mx-auto px-4">
        <h1 class="text-3xl font-bold mb-6">Manage Orders</h1>

        <div class="bg-white rounded-lg border overflow-hidden">
          <table class="w-full">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left">Order #</th>
                <th class="px-6 py-3 text-left">Customer</th>
                <th class="px-6 py-3 text-left">Date</th>
                <th class="px-6 py-3 text-left">Items</th>
                <th class="px-6 py-3 text-left">Total</th>
                <th class="px-6 py-3 text-left">Status</th>
                <th class="px-6 py-3 text-left">Payment</th>
                <th class="px-6 py-3 text-left">Actions</th>
              </tr>
            </thead>

            <tbody>
              @for (order of orders; track order.id) {
                <tr class="hover:bg-gray-50">
                  <td class="px-6 py-4 font-medium">#{{ order.orderNumber || order.id }}</td>

                  <td class="px-6 py-4">
                    <div class="font-medium">
                      {{ order.user?.fullName || 'N/A' }}
                    </div>
                    <div class="text-xs text-gray-500">
                      {{ order.user?.email || '' }}
                    </div>
                  </td>

                  <td class="px-6 py-4">{{ formatDate(order.createdAt) }}</td>
                  <td class="px-6 py-4">{{ order.items.length }} items</td>
                  <td class="px-6 py-4 font-medium">₹{{ order.totalAmount.toFixed(2) }}</td>

                  <td class="px-6 py-4">
                    <select
                      [value]="order.status"
                      (change)="updateStatus(order.id, $event)"
                      class="px-2 py-1 rounded text-xs border"
                      [ngClass]="getStatusClass(order.status)"
                    >
                      <option value="PENDING">Pending</option>
                      <option value="PROCESSING">Processing</option>
                      <option value="PACKED">Packed</option>
                      <option value="OUT_FOR_DELIVERY">Out for Delivery</option>
                      <option value="SHIPPED">In Transit</option>
                      <option value="DELIVERED">Delivered</option>
                      <option value="COMPLETED">Completed</option>
                      <option value="CANCELLED">Cancelled</option>
                    </select>
                  </td>

                  <td class="px-6 py-4">
                    <span
                      class="px-2 py-1 rounded-full text-xs"
                      [ngClass]="getPaymentStatusClass(order.paymentStatus)"
                    >
                      {{ order.paymentStatus }}
                    </span>
                  </td>

                  <td class="px-6 py-4">
                    <button (click)="viewOrder(order)"
                      class="p-2 text-blue-600 hover:bg-blue-50 rounded">
                      <lucide-icon [img]="Eye" class="h-4 w-4"></lucide-icon>
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        <!-- MODAL -->
        @if(selectedOrder) {
          <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div class="bg-white rounded-lg max-w-3xl w-full p-6 overflow-y-auto max-h-[90vh]">

              <div class="flex justify-between mb-4">
                <h2 class="text-2xl font-bold">
                  Order #{{ selectedOrder.orderNumber || selectedOrder.id }}
                </h2>
                <button class="p-2" (click)="selectedOrder = null">✖</button>
              </div>

              <div class="space-y-4">
                
                <div>
                  <h3 class="font-semibold mb-1">Customer Information</h3>
                  <p>
                    {{ selectedOrder.user?.fullName || 'N/A' }} <br>
                    {{ selectedOrder.user?.email || '' }}
                  </p>
                </div>

                <div>
                  <h3 class="font-semibold mb-1">Shipping Address</h3>
                  @if(selectedOrder.shippingAddress) {
                    <p>
                      {{ selectedOrder.shippingAddress.line1 }},
                      {{ selectedOrder.shippingAddress.city }},
                      {{ selectedOrder.shippingAddress.state }}
                      {{ selectedOrder.shippingAddress.postalCode }}
                    </p>
                  } @else {
                    <p>No address provided</p>
                  }
                </div>

                <div>
                  <h3 class="font-semibold mb-1">Order Items</h3>
                  <div class="space-y-2">
                      @for (item of selectedOrder.items; track item.productId) {
                        <div class="flex justify-between items-center bg-gray-50 p-2 rounded">
                          <div>
                            <p class="font-medium">{{ item.productName }}</p>
                            <p class="text-sm text-gray-500">
                              Qty: {{ item.quantity }} × ₹{{ item.unitPrice }}
                            </p>
                          </div>
                          <div class="font-medium">
                            ₹{{ item.subtotal.toFixed(2) }}
                          </div>
                        </div>
                      }
                  </div>
                </div>

                <div class="border-t pt-4 font-bold">
                  <div class="flex justify-between">
                    <span>Total:</span>
                    <span>₹{{ selectedOrder.totalAmount.toFixed(2) }}</span>
                  </div>
                </div>

              </div>

            </div>
          </div>
        }

      </div>
    </div>
  `
})
export class AdminOrdersComponent implements OnInit {
  http = inject(HttpClient);

  orders: OrderDto[] = [];
  selectedOrder: OrderDto | null = null;

  readonly Eye = Eye;
  readonly Package = Package;

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.http.get<PagedResponse<OrderDto>>(
      `${environment.apiUrl}/admin/orders?page=0&size=20`
    ).subscribe({
      next: (res) => {
        this.orders = res.content || [];
      },
      error: (err) => console.error('Load failed:', err)
    });
  }

  viewOrder(order: OrderDto): void {
    this.http.get<OrderDto>(`${environment.apiUrl}/admin/orders/${order.id}`).subscribe({
      next: (full) => this.selectedOrder = full,
      error: () => this.selectedOrder = order
    });
  }

  updateStatus(orderId: number, event: Event): void {
    const newStatus = (event.target as HTMLSelectElement).value;
    this.http.post(
      `${environment.apiUrl}/admin/orders/${orderId}/status`,
      { status: newStatus.toUpperCase() }
    ).subscribe(() => this.loadOrders());
  }

  formatDate(date: string): string {
    return date ? new Date(date).toLocaleDateString() : 'N/A';
  }

  getStatusClass(status: string): string {
    return {
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'PROCESSING': 'bg-blue-100 text-blue-800',
      'PACKED': 'bg-purple-100 text-purple-800',
      'OUT_FOR_DELIVERY': 'bg-orange-100 text-orange-800',
      'DELIVERED': 'bg-green-100 text-green-800',
      'COMPLETED': 'bg-green-100 text-green-800',
      'CANCELLED': 'bg-red-100 text-red-800'
    }[status] || 'bg-gray-200';
  }

  getPaymentStatusClass(status: string): string {
    return {
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'SUCCESS': 'bg-green-100 text-green-800',
      'FAILED': 'bg-red-100 text-red-800',
      'REFUNDED': 'bg-gray-100 text-gray-800'
    }[status] || 'bg-gray-200';
  }
}
