# RevCart Frontend Structure

## Technology Stack
- **Framework**: Angular 18 (Standalone Components)
- **Styling**: Tailwind CSS
- **Icons**: Lucide Angular
- **State Management**: Angular Signals
- **HTTP**: HttpClient with RxJS

---

## Project Structure

```
Frontend/
├── src/
│   ├── app/
│   │   ├── core/                      # Core functionality
│   │   │   ├── guards/                # Route guards (auth, role-based)
│   │   │   ├── interceptors/          # HTTP interceptors (auth, error)
│   │   │   ├── models/                # TypeScript interfaces/types
│   │   │   │   ├── user.model.ts
│   │   │   │   ├── product.model.ts
│   │   │   │   ├── cart.model.ts
│   │   │   │   └── order.model.ts
│   │   │   └── services/              # Core services
│   │   │       ├── auth.service.ts    # Authentication & user management
│   │   │       ├── product.service.ts # Product & category APIs
│   │   │       ├── cart.service.ts    # Shopping cart
│   │   │       ├── order.service.ts   # Order management
│   │   │       └── notification.service.ts
│   │   │
│   │   ├── features/                  # Feature modules
│   │   │   ├── admin/                 # Admin dashboard
│   │   │   │   ├── dashboard/
│   │   │   │   ├── products/
│   │   │   │   ├── orders/
│   │   │   │   └── users/
│   │   │   │
│   │   │   ├── auth/                  # Authentication
│   │   │   │   ├── login/
│   │   │   │   │   └── login.component.ts
│   │   │   │   └── signup/
│   │   │   │       └── signup.component.ts
│   │   │   │
│   │   │   ├── home/                  # Home page
│   │   │   │   └── home.component.ts
│   │   │   │
│   │   │   ├── products/              # Product listing & details
│   │   │   │   ├── product-list/
│   │   │   │   └── product-detail/
│   │   │   │
│   │   │   ├── categories/            # Category pages
│   │   │   │   └── category.component.ts
│   │   │   │
│   │   │   ├── cart/                  # Shopping cart
│   │   │   │   └── cart.component.ts
│   │   │   │
│   │   │   ├── checkout/              # Checkout flow
│   │   │   │   └── checkout.component.ts
│   │   │   │
│   │   │   ├── orders/                # Order history
│   │   │   │   ├── order-list/
│   │   │   │   └── order-detail/
│   │   │   │
│   │   │   ├── profile/               # User profile
│   │   │   │   ├── profile.component.ts
│   │   │   │   └── addresses/
│   │   │   │
│   │   │   ├── best-sellers/          # Best selling products
│   │   │   ├── new-arrivals/          # New products
│   │   │   ├── deals/                 # Special deals
│   │   │   ├── wishlist/              # User wishlist
│   │   │   ├── payment/               # Payment processing
│   │   │   └── delivery-agent/        # Delivery agent dashboard
│   │   │
│   │   ├── shared/                    # Shared components
│   │   │   ├── components/
│   │   │   │   ├── header/            # Navigation header
│   │   │   │   ├── footer/            # Footer
│   │   │   │   ├── product-card/      # Product display card
│   │   │   │   ├── category-card/     # Category display card
│   │   │   │   ├── notification-toast/ # Toast notifications
│   │   │   │   └── loading-spinner/   # Loading indicator
│   │   │   ├── directives/            # Custom directives
│   │   │   └── pipes/                 # Custom pipes
│   │   │
│   │   ├── app.component.ts           # Root component
│   │   ├── app.routes.ts              # Route configuration
│   │   └── app.config.ts              # App configuration
│   │
│   ├── assets/
│   │   └── data/
│   │       └── mock-data.ts           # Mock data for fallback
│   │
│   ├── environments/
│   │   ├── environment.ts             # Dev environment
│   │   └── environment.prod.ts        # Prod environment
│   │
│   ├── styles.css                     # Global styles
│   └── index.html                     # HTML entry point
│
├── angular.json                       # Angular configuration
├── tailwind.config.js                 # Tailwind CSS config
├── package.json                       # Dependencies
└── tsconfig.json                      # TypeScript config
```

---

## Key Files

### 1. Environment Configuration
**File**: `src/environments/environment.ts`
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### 2. App Routes
**File**: `src/app/app.routes.ts`
```typescript
export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'auth/login', component: LoginComponent },
  { path: 'auth/signup', component: SignupComponent },
  { path: 'products', component: ProductListComponent },
  { path: 'products/:id', component: ProductDetailComponent },
  { path: 'cart', component: CartComponent },
  { path: 'checkout', component: CheckoutComponent },
  { path: 'orders', component: OrderListComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'admin', component: AdminDashboardComponent, canActivate: [AdminGuard] },
  { path: 'delivery', component: DeliveryAgentComponent, canActivate: [DeliveryGuard] }
];
```

### 3. Core Services

#### Auth Service
**File**: `src/app/core/services/auth.service.ts`
- `login(credentials)` - User login
- `signup(data)` - User registration
- `logout()` - User logout
- `user` - Signal with current user
- `isAuthenticated` - Computed signal for auth state

#### Product Service
**File**: `src/app/core/services/product.service.ts`
- `getProducts(filters?)` - Get all products
- `getProductById(id)` - Get single product
- `getCategories()` - Get all categories
- `getBestSellers(limit?)` - Get best selling products
- `getNewArrivals(limit?)` - Get new products

#### Cart Service
**File**: `src/app/core/services/cart.service.ts`
- `addToCart(product, quantity)` - Add item to cart
- `removeFromCart(productId)` - Remove item
- `updateQuantity(productId, quantity)` - Update quantity
- `clearCart()` - Clear all items
- `cart` - Signal with cart items
- `totalItems` - Computed total items
- `totalPrice` - Computed total price

---

## Component Architecture

### Standalone Components
All components use Angular 18 standalone architecture:
```typescript
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `...`
})
export class LoginComponent { }
```

### Signal-Based State
Uses Angular Signals for reactive state:
```typescript
private userSignal = signal<User | null>(null);
user = this.userSignal.asReadonly();
isAuthenticated = computed(() => this.userSignal() !== null);
```

---

## API Integration

### Backend Communication
All services communicate with backend through API Gateway at `http://localhost:8080/api`

**Response Format**:
```typescript
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}
```

### Example API Calls

**Login**:
```typescript
POST /api/users/login
Body: { email, password }
Response: { success, message, data: { token, userId, email, name, role } }
```

**Get Products**:
```typescript
GET /api/products
Response: { success, message, data: [products] }
```

**Add to Cart**:
```typescript
POST /api/cart/items
Headers: { X-User-Id: userId }
Body: { productId, quantity }
```

---

## Styling

### Tailwind CSS
- Utility-first CSS framework
- Custom theme in `tailwind.config.js`
- Primary color: Green (#10b981)
- Responsive design with mobile-first approach

### Global Styles
**File**: `src/styles.css`
- Tailwind directives
- Custom CSS variables
- Global component styles

---

## Features

### User Features
- ✅ User registration & login
- ✅ Browse products by category
- ✅ Search products
- ✅ Add to cart
- ✅ Checkout process
- ✅ Order history
- ✅ Profile management
- ✅ Address management

### Admin Features
- ✅ Dashboard with analytics
- ✅ Product management (CRUD)
- ✅ Order management
- ✅ User management

### Delivery Agent Features
- ✅ View assigned deliveries
- ✅ Update delivery status
- ✅ Delivery history

---

## Development

### Start Development Server
```bash
cd Frontend
npm install
npm start
# Opens at http://localhost:4200
```

### Build for Production
```bash
npm run build
# Output in dist/ folder
```

### Run Tests
```bash
npm test
```

---

## Key Dependencies

```json
{
  "@angular/core": "^18.2.0",
  "@angular/common": "^18.2.0",
  "@angular/router": "^18.2.0",
  "tailwindcss": "^3.4.0",
  "lucide-angular": "^0.447.0",
  "rxjs": "^7.8.0"
}
```

---

## Notes

- **Standalone Components**: No NgModules, all components are standalone
- **Signals**: Modern reactive state management
- **Type Safety**: Full TypeScript with strict mode
- **Responsive**: Mobile-first design with Tailwind
- **Fallback**: Mock data used when backend unavailable
- **Error Handling**: Graceful error handling with user feedback
