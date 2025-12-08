import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { OrderService } from '../../../core/services/order.service';
import { ProductService } from '../../../core/services/product.service';
import { Order } from '../../../core/models/order.model';
import { environment } from '../../../../environments/environment';
import { LucideAngularModule, Package, Users, DollarSign } from 'lucide-angular';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  template: `
    <div class="min-h-screen bg-background py-8">
      <div class="container mx-auto px-4">
        <h1 class="text-3xl font-bold mb-8">Admin Dashboard</h1>

        <!-- Stats Grid -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          
          <!-- TOTAL ORDERS -->
          <div class="bg-white p-6 rounded-lg border">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-sm font-medium text-muted-foreground">Total Orders</h3>
              <lucide-icon [img]="Package" class="h-5 w-5 text-primary"></lucide-icon>
            </div>
            <p class="text-3xl font-bold">{{ stats.totalOrders }}</p>
          </div>

          <!-- TOTAL REVENUE -->
          <div class="bg-white p-6 rounded-lg border">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-sm font-medium text-muted-foreground">Total Revenue</h3>
              <lucide-icon [img]="DollarSign" class="h-5 w-5 text-primary"></lucide-icon>
            </div>
            <p class="text-3xl font-bold">₹{{ stats.totalRevenue.toFixed(2) }}</p>
          </div>

          <!-- TOTAL PRODUCTS -->
          <div class="bg-white p-6 rounded-lg border">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-sm font-medium text-muted-foreground">Total Products</h3>
              <lucide-icon [img]="Package" class="h-5 w-5 text-primary"></lucide-icon>
            </div>
            <p class="text-3xl font-bold">{{ stats.totalProducts }}</p>
            <p class="text-xs text-blue-600 mt-1">{{ stats.inStock }} in stock</p>
          </div>

          <!-- ACTIVE USERS -->
          <div class="bg-white p-6 rounded-lg border">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-sm font-medium text-muted-foreground">Active Users</h3>
              <lucide-icon [img]="Users" class="h-5 w-5 text-primary"></lucide-icon>
            </div>
            <p class="text-3xl font-bold">{{ stats.activeUsers }}</p>
          </div>
        </div>

        <!-- Recent Orders -->
        <div class="bg-white rounded-lg border p-6">
          <h2 class="text-xl font-bold mb-4">Recent Orders</h2>
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="border-b">
                  <th class="text-left py-3 px-4">Order ID</th>
                  <th class="text-left py-3 px-4">Date</th>
                  <th class="text-left py-3 px-4">Status</th>
                  <th class="text-left py-3 px-4">Total</th>
                  <th class="text-left py-3 px-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (order of recentOrders; track order.id) {
                  <tr class="border-b hover:bg-gray-50">
                    <td class="py-3 px-4 font-medium">{{ order.id }}</td>
                    <td class="py-3 px-4">{{ order.date }}</td>
                    <td class="py-3 px-4">
                      <span class="px-2 py-1 rounded-full text-xs"
                        [ngClass]="{
                          'bg-yellow-100 text-yellow-800': order.status === 'processing',
                          'bg-purple-100 text-purple-800': order.status === 'packed',
                          'bg-blue-100 text-blue-800': order.status === 'in_transit',
                          'bg-green-100 text-green-800': order.status === 'delivered',
                          'bg-red-100 text-red-800': order.status === 'cancelled'
                        }">
                        {{ order.status }}
                      </span>
                    </td>
                    <td class="py-3 px-4">₹{{ order.total.toFixed(2) }}</td>
                    <td class="py-3 px-4">
                      <button routerLink="/admin/orders" class="text-primary hover:underline">
                        View
                      </button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `
})
export class AdminDashboardComponent implements OnInit {

  http = inject(HttpClient);
  productService = inject(ProductService);

  readonly Package = Package;
  readonly Users = Users;
  readonly DollarSign = DollarSign;

  stats = {
    totalOrders: 0,
    totalRevenue: 0,
    totalProducts: 0,
    inStock: 0,
    activeUsers: 0
  };

  recentOrders: Order[] = [];

  ngOnInit(): void {
    this.loadStats();
    this.loadRecentOrders();

    // Dashboard refresh trigger from Manage Orders
    window.addEventListener('orderUpdated', () => {
      this.loadStats();
      this.loadRecentOrders();
    });
  }

  loadStats(): void {
    this.http.get<any>(`${environment.apiUrl}/admin/dashboard/stats`)
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.stats.totalOrders = response.data.totalOrders;
            this.stats.totalRevenue = response.data.totalRevenue;
            this.stats.totalProducts = response.data.totalProducts;
            this.stats.activeUsers = response.data.activeUsers;
          }
        },
        error: err => console.error('Dashboard stats error:', err)
      });

    this.productService.getProducts()
      .subscribe((products) => {
        this.stats.inStock = products.filter(p => p.inStock).length;
      });
  }

  loadRecentOrders(): void {
    const params = new HttpParams().set('page', 0).set('size', 5);
    this.http.get<any>(`${environment.apiUrl}/admin/orders`, { params })
      .subscribe({
        next: (response) => {
          this.recentOrders = response.content.map((order: any) => ({
            id: String(order.id),
            date: order.createdAt.split('T')[0],
            status: this.mapStatus(order.status),
            total: order.totalAmount
          }));
        },
        error: err => console.error('Recent orders error:', err)
      });
  }

  private mapStatus(status: string): Order['status'] {
    const lookup: { [key: string]: Order['status'] } = {
      'PENDING': 'processing',
      'PROCESSING': 'processing',
      'PACKED': 'packed',
      'OUT_FOR_DELIVERY': 'in_transit',
      'SHIPPED': 'in_transit',
      'DELIVERED': 'delivered',
      'COMPLETED': 'delivered',
      'CANCELLED': 'cancelled'
    };
    return lookup[status.toUpperCase()] || 'processing';
  }
}
