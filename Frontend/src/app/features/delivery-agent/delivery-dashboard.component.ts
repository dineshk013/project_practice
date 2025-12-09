import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Order } from '../../core/models/order.model';
import { environment } from '../../../environments/environment';
import { LucideAngularModule, Package, MapPin, Clock, CheckCircle } from 'lucide-angular';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Component({
  selector: 'app-delivery-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  template: `
    <div class="min-h-screen bg-background py-8">
      <div class="container mx-auto px-4">
        <h1 class="text-3xl font-bold mb-8">Delivery Dashboard</h1>

        <!-- Stats -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div class="bg-white p-6 rounded-lg border">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-sm font-medium text-muted-foreground">Assigned</h3>
              <lucide-icon [img]="Package" class="h-5 w-5 text-blue-600"></lucide-icon>
            </div>
            <p class="text-3xl font-bold">{{ assignedOrders.length }}</p>
            <p class="text-xs text-blue-600 mt-1">Orders assigned to you</p>
          </div>

          <div class="bg-white p-6 rounded-lg border">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-sm font-medium text-muted-foreground">In Transit</h3>
              <lucide-icon [img]="MapPin" class="h-5 w-5 text-orange-600"></lucide-icon>
            </div>
            <p class="text-3xl font-bold">{{ inTransitOrders.length }}</p>
            <p class="text-xs text-orange-600 mt-1">On the way</p>
          </div>

          <div class="bg-white p-6 rounded-lg border">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-sm font-medium text-muted-foreground">Delivered Today</h3>
              <lucide-icon [img]="CheckCircle" class="h-5 w-5 text-green-600"></lucide-icon>
            </div>
            <p class="text-3xl font-bold">{{ deliveredToday }}</p>
            <p class="text-xs text-green-600 mt-1">Successfully delivered</p>
          </div>

          <div class="bg-white p-6 rounded-lg border">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-sm font-medium text-muted-foreground">Pending</h3>
              <lucide-icon [img]="Clock" class="h-5 w-5 text-yellow-600"></lucide-icon>
            </div>
            <p class="text-3xl font-bold">{{ pendingOrders.length }}</p>
            <p class="text-xs text-yellow-600 mt-1">Awaiting pickup</p>
          </div>
        </div>

        <!-- Assigned Deliveries -->
        <div class="bg-white rounded-lg border p-6">
          <h2 class="text-xl font-bold mb-4">Assigned Deliveries</h2>

          <div class="space-y-4">
            @for (order of assignedOrders; track order.id) {
              <div class="border rounded-lg p-4 hover:shadow transition-shadow">
                <div class="flex justify-between mb-3">
                  <div>
                    <h3 class="font-semibold">Order #{{ order.id }}</h3>
                    <p class="text-sm text-muted-foreground">{{ order.date }}</p>
                  </div>

                  <span class="px-3 py-1 rounded-full text-sm"
                    [ngClass]="{
                      'bg-yellow-100 text-yellow-800': order.status === 'processing',
                      'bg-blue-100 text-blue-800': order.status === 'in_transit',
                      'bg-green-100 text-green-800': order.status === 'delivered'
                    }">
                    {{ order.status | titlecase }}
                  </span>
                </div>

                <div class="flex items-start gap-2 mb-3">
                  <lucide-icon [img]="MapPin" class="h-4 w-4 text-muted-foreground mt-1"></lucide-icon>
                  <p class="text-sm">{{ order.deliveryAddress }}</p>
                </div>

                <div class="flex justify-between items-center">
                  <p class="font-semibold">₹{{ order.total.toFixed(2) }}</p>

                  <div class="flex gap-2">
                    @if (order.status === 'processing') {
                      <button (click)="updateOrderStatus(order.id, 'in_transit')"
                        class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
                        Start Delivery
                      </button>
                    }

                    @if (order.status === 'in_transit') {
                      <button (click)="updateOrderStatus(order.id, 'delivered')"
                        class="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700">
                        Mark Delivered
                      </button>
                    }
                  </div>
                </div>
              </div>
            }

            @empty {
              <p class="text-center text-muted-foreground py-8">No assigned deliveries</p>
            }
          </div>
        </div>
      </div>
    </div>
  `
})
export class DeliveryDashboardComponent implements OnInit {

  http = inject(HttpClient);

  readonly Package = Package;
  readonly MapPin = MapPin;
  readonly Clock = Clock;
  readonly CheckCircle = CheckCircle;

  assignedOrders: Order[] = [];
  inTransitOrders: Order[] = [];
  pendingOrders: Order[] = [];
  deliveredToday = 0;

  ngOnInit(): void {
    this.loadDeliveries();
  }

  loadDeliveries(): void {
    const userId = localStorage.getItem('userId') || '';
    const headers = { 'X-User-Id': userId };

    // Assigned
    this.http.get<ApiResponse<any[]>>(`${environment.apiUrl}/delivery/orders/assigned`, { headers })
      .subscribe(resp => {
        if (resp.success) this.assignedOrders = resp.data.map(this.mapBackendOrder);
        this.calculateDelivered();
      });

    // In transit
    this.http.get<ApiResponse<any[]>>(`${environment.apiUrl}/delivery/orders/in-transit`, { headers })
      .subscribe(resp => {
        if (resp.success) this.inTransitOrders = resp.data.map(this.mapBackendOrder);
      });

    // Pending
    this.http.get<ApiResponse<any[]>>(`${environment.apiUrl}/delivery/orders/pending`, { headers })
      .subscribe(resp => {
        if (resp.success) this.pendingOrders = resp.data.map(this.mapBackendOrder);
      });
  }

  private calculateDelivered(): void {
    this.deliveredToday = this.assignedOrders.filter(o => o.status === 'delivered').length;
  }

  updateOrderStatus(orderId: string, status: Order['status']) {
    const mapping: any = {
      'in_transit': 'IN_TRANSIT',
      'delivered': 'DELIVERED'
    };

    const backendStatus = mapping[status];

    this.http.post<ApiResponse<any>>(
      `${environment.apiUrl}/delivery/orders/${orderId}/status`,
      { status: backendStatus }
    ).subscribe(() => this.loadDeliveries());
  }

  private mapBackendOrder = (dto: any): Order => ({
    id: String(dto.orderId || dto.id),
    date: dto.createdAt ? new Date(dto.createdAt).toLocaleDateString() : '',
    status: this.mapStatus(dto.status),
    items: [],                                // backend does not send items → empty array
    total: Number(dto.totalAmount || 0),
    deliveryAddress: dto.deliveryAddress || dto.address || ''
  });

  private mapStatus(s: string): Order['status'] {
    return ({
      'ASSIGNED': 'processing',
      'OUT_FOR_DELIVERY': 'in_transit',
      'IN_TRANSIT': 'in_transit',
      'DELIVERED': 'delivered',
      'PICKED_UP': 'in_transit'
    } as any)[s] || 'processing';
  }
}
