import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
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

        <!-- Stats -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">

          <div class="bg-white p-6 rounded-lg border">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-sm font-medium text-muted-foreground">Total Orders</h3>
              <lucide-icon [img]="Package" class="h-5 w-5 text-primary"></lucide-icon>
            </div>
            <p class="text-3xl font-bold">{{ stats.totalOrders }}</p>
          </div>

          <div class="bg-white p-6 rounded-lg border">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-sm font-medium text-muted-foreground">Total Revenue</h3>
              <lucide-icon [img]="DollarSign" class="h-5 w-5 text-primary"></lucide-icon>
            </div>
            <p class="text-3xl font-bold">₹{{ stats.totalRevenue.toFixed(2) }}</p>
          </div>

          <div class="bg-white p-6 rounded-lg border">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-sm font-medium text-muted-foreground">Total Products</h3>
              <lucide-icon [img]="Package" class="h-5 w-5 text-primary"></lucide-icon>
            </div>
            <p class="text-3xl font-bold">{{ stats.totalProducts }}</p>
            <p class="text-xs text-blue-600 mt-1">{{ stats.inStock }} in stock</p>
          </div>

          <div class="bg-white p-6 rounded-lg border">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-sm font-medium text-muted-foreground">Active Users</h3>
              <lucide-icon [img]="Users" class="h-5 w-5 text-primary"></lucide-icon>
            </div>
            <p class="text-3xl font-bold">{{ stats.activeUsers }}</p>
          </div>
        </div>

        <!-- Action Cards -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-10">

          <a routerLink="/admin/products" class="bg-white p-6 rounded-lg border hover:shadow-md transition cursor-pointer">
            <h3 class="font-medium mb-2 text-lg">Manage Products</h3>
            <p class="text-sm text-muted-foreground">Add, edit, or remove products</p>
          </a>

          <a routerLink="/admin/categories" class="bg-white p-6 rounded-lg border hover:shadow-md transition cursor-pointer">
            <h3 class="font-medium mb-2 text-lg">Manage Categories</h3>
            <p class="text-sm text-muted-foreground">Add, edit, or remove categories</p>
          </a>

          <a routerLink="/admin/orders" class="bg-white p-6 rounded-lg border hover:shadow-md transition cursor-pointer">
            <h3 class="font-medium mb-2 text-lg">Manage Orders</h3>
            <p class="text-sm text-muted-foreground">View and update order status</p>
          </a>

          <a routerLink="/admin/users" class="bg-white p-6 rounded-lg border hover:shadow-md transition cursor-pointer">
            <h3 class="font-medium mb-2 text-lg">Manage Users</h3>
            <p class="text-sm text-muted-foreground">View and manage user accounts</p>
          </a>

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
                        [ngClass]="getStatusStyle(order.status)">
                        {{ order.status }}
                      </span>
                    </td>
                    <td class="py-3 px-4">₹{{ order.total.toFixed(2) }}</td>
                    <td class="py-3 px-4">
                      <a routerLink="/admin/orders" class="text-primary hover:underline">
                        View
                      </a>
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

  getStatusStyle(status: string): string {
    const styleMap: any = {
      'processing': 'bg-yellow-100 text-yellow-800',
      'packed': 'bg-purple-100 text-purple-800',
      'in_transit': 'bg-blue-100 text-blue-800',
      'delivered': 'bg-green-100 text-green-800',
      'cancelled': 'bg-red-100 text-red-800'
    };
    return styleMap[status] || 'bg-gray-100 text-gray-800';
  }
}
