import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { LucideAngularModule, User, Shield, Truck, ShoppingCart, ToggleLeft, ToggleRight } from 'lucide-angular';

interface UserDto {
  id: number;
  name: string;
  email: string;
  phone?: string;
  role: string;
  active?: boolean;
  createdAt: string;
}

interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  template: `
    <div class="min-h-screen bg-background py-8">
      <div class="container mx-auto px-4">
        <h1 class="text-3xl font-bold mb-6">Manage Users</h1>

        <!-- Users Table -->
        <div class="bg-white rounded-lg border overflow-hidden">
          <table class="w-full">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Phone</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Role</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Verified</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200">
              @if (users.length === 0) {
                <tr>
                  <td colspan="7" class="px-6 py-8 text-center text-gray-500">
                    No users found
                  </td>
                </tr>
              } @else {
                @for (user of users; track user.id) {
                  <tr class="hover:bg-gray-50">
                    <td class="px-6 py-4 font-medium">{{ user.name }}</td>
                    <td class="px-6 py-4">{{ user.email }}</td>
                    <td class="px-6 py-4">{{ user.phone || 'N/A' }}</td>
                    <td class="px-6 py-4">
                      <span
                        class="px-2 py-1 rounded text-xs"
                        [ngClass]="getRoleClass(user.role)"
                      >
                        {{ getRoleLabel(user.role) }}
                      </span>
                    </td>
                    <td class="px-6 py-4">
                      <span
                        class="px-2 py-1 rounded-full text-xs"
                        [ngClass]="(user.active !== false) ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'"
                      >
                        {{ (user.active !== false) ? 'Active' : 'Inactive' }}
                      </span>
                    </td>
                    <td class="px-6 py-4">
                      <span class="text-sm text-gray-600">N/A</span>
                    </td>
                    <td class="px-6 py-4">
                      <button
                        (click)="viewUser(user)"
                        class="text-blue-600 hover:underline text-sm"
                      >
                        View
                      </button>
                    </td>
                  </tr>
                }
              }
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        @if (totalPages > 1) {
          <div class="mt-4 flex justify-center gap-2">
            <button
              (click)="loadPage(currentPage - 1)"
              [disabled]="currentPage === 0"
              class="px-4 py-2 border rounded disabled:opacity-50"
            >
              Previous
            </button>
            <span class="px-4 py-2">{{ currentPage + 1 }} / {{ totalPages }}</span>
            <button
              (click)="loadPage(currentPage + 1)"
              [disabled]="currentPage >= totalPages - 1"
              class="px-4 py-2 border rounded disabled:opacity-50"
            >
              Next
            </button>
          </div>
        }

        <!-- User Detail Modal -->
        @if (selectedUser) {
          <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div class="bg-white rounded-lg max-w-2xl w-full">
              <div class="p-6">
                <div class="flex justify-between items-center mb-4">
                  <h2 class="text-2xl font-bold">{{ selectedUser.name }}</h2>
                  <button (click)="selectedUser = null" class="p-2 hover:bg-gray-100 rounded">×</button>
                </div>

                <div class="space-y-4">
                  <div>
                    <label class="text-sm font-medium text-gray-500">Email</label>
                    <p class="mt-1">{{ selectedUser.email }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-gray-500">Phone</label>
                    <p class="mt-1">{{ selectedUser.phone || 'N/A' }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-gray-500">Role</label>
                    <p class="mt-1">
                      <span [ngClass]="getRoleClass(selectedUser.role)" class="px-2 py-1 rounded text-sm">
                        {{ selectedUser.role }}
                      </span>
                    </p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-gray-500">Status</label>
                    <p class="mt-1">
                      <span [ngClass]="(selectedUser.active !== false) ? 'text-green-600' : 'text-gray-400'" class="text-sm">
                        {{ (selectedUser.active !== false) ? 'Active' : 'Inactive' }}
                      </span>
                    </p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-gray-500">Member Since</label>
                    <p class="mt-1">{{ formatDate(selectedUser.createdAt) }}</p>
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
export class AdminUsersComponent implements OnInit {
  http = inject(HttpClient);

  users: UserDto[] = [];
  selectedUser: UserDto | null = null;
  currentPage = 0;
  totalPages = 1;

  readonly User = User;
  readonly Shield = Shield;
  readonly Truck = Truck;
  readonly ShoppingCart = ShoppingCart;
  readonly ToggleLeft = ToggleLeft;
  readonly ToggleRight = ToggleRight;

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.http.get<ApiResponse<UserDto[]>>(`${environment.userServiceUrl}/api/admin/users`)
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.users = response.data;
            this.totalPages = 1;
          }
        },
        error: (err) => {
          console.error('Failed to load users:', err);
          this.users = [];
        }
      });
  }

  getRoleLabel(role: string): string {
    const labels: { [key: string]: string } = {
      'USER': 'Customer',
      'ADMIN': 'Admin',
      'DELIVERY_AGENT': 'Delivery Agent'
    };
    return labels[role] || role;
  }

  viewUser(user: UserDto): void {
    this.http.get<ApiResponse<UserDto>>(`${environment.userServiceUrl}/api/admin/users/${user.id}`).subscribe({
      next: (response) => {
        this.selectedUser = response.success && response.data ? response.data : user;
      },
      error: () => {
        this.selectedUser = user;
      }
    });
  }

  formatDate(date: string): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString();
  }

  getRoleClass(role: string): string {
    const classes: { [key: string]: string } = {
      'USER': 'bg-blue-100 text-blue-800',
      'ADMIN': 'bg-purple-100 text-purple-800',
      'DELIVERY_AGENT': 'bg-green-100 text-green-800'
    };
    return classes[role] || 'bg-gray-100 text-gray-800';
  }

  getRoleIcon(role: string): string {
    return role === 'ADMIN' ? 'Admin' : role === 'DELIVERY_AGENT' ? 'Delivery Agent' : 'Customer';
  }

  loadPage(page: number): void {
    this.currentPage = page;
    this.loadUsers();
  }
}

