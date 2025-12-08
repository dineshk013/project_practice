# Profile API Implementation

## Overview
Created ProfileController and ProfileService in user-service to handle `/api/profile/**` endpoints.

## Files Created

### 1. ProfileController.java
**Location**: `user-service/src/main/java/com/revcart/userservice/controller/ProfileController.java`

**Endpoints**:
- `GET /api/profile` - Get user profile
- `PUT /api/profile` - Update user profile
- `GET /api/profile/addresses` - Get all addresses
- `POST /api/profile/addresses` - Add new address
- `PUT /api/profile/addresses/{id}` - Update address
- `DELETE /api/profile/addresses/{id}` - Delete address

### 2. ProfileService.java
**Location**: `user-service/src/main/java/com/revcart/userservice/service/ProfileService.java`

**Features**:
- Extracts user from JWT token (via SecurityContext)
- Handles profile CRUD operations
- Handles address CRUD operations
- Validates user ownership of addresses
- Returns ApiResponse format

## DTOs Used

### UserDto (Already Exists)
```java
- Long id
- String email
- String name
- String phone
- User.Role role
- LocalDateTime createdAt
```

### AddressDto (Already Exists)
```java
- Long id
- String street
- String city
- String state
- String zipCode
- String country
- Boolean isDefault
```

## Gateway Routing

Gateway already configured to route `/api/profile/**` to user-service (port 8081) with path rewriting:
- `/api/profile` → `/api/profile` (handled by ProfileController)
- `/api/profile/addresses` → `/api/profile/addresses` (handled by ProfileController)

## Security

- All `/api/profile/**` endpoints require JWT authentication
- User identity extracted from JWT token via SecurityContextHolder
- Address operations validate user ownership

## Testing

### 1. Get Profile
```powershell
$token = "your-jwt-token"
Invoke-RestMethod -Uri "http://localhost:8080/api/profile" `
    -Method Get `
    -Headers @{Authorization="Bearer $token"}
```

### 2. Update Profile
```powershell
$token = "your-jwt-token"
$body = @{
    name = "Updated Name"
    phone = "9876543210"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/profile" `
    -Method Put `
    -Headers @{Authorization="Bearer $token"} `
    -ContentType "application/json" `
    -Body $body
```

### 3. Get Addresses
```powershell
$token = "your-jwt-token"
Invoke-RestMethod -Uri "http://localhost:8080/api/profile/addresses" `
    -Method Get `
    -Headers @{Authorization="Bearer $token"}
```

### 4. Add Address
```powershell
$token = "your-jwt-token"
$body = @{
    street = "123 Main St"
    city = "Mumbai"
    state = "Maharashtra"
    zipCode = "400001"
    country = "India"
    isDefault = $true
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/profile/addresses" `
    -Method Post `
    -Headers @{Authorization="Bearer $token"} `
    -ContentType "application/json" `
    -Body $body
```

### 5. Update Address
```powershell
$token = "your-jwt-token"
$body = @{
    street = "456 Updated St"
    city = "Mumbai"
    state = "Maharashtra"
    zipCode = "400002"
    country = "India"
    isDefault = $false
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/profile/addresses/1" `
    -Method Put `
    -Headers @{Authorization="Bearer $token"} `
    -ContentType "application/json" `
    -Body $body
```

### 6. Delete Address
```powershell
$token = "your-jwt-token"
Invoke-RestMethod -Uri "http://localhost:8080/api/profile/addresses/1" `
    -Method Delete `
    -Headers @{Authorization="Bearer $token"}
```

## Response Format

All endpoints return `ApiResponse<T>`:
```json
{
    "success": true,
    "message": "Profile retrieved successfully",
    "data": {
        "id": 1,
        "email": "user@example.com",
        "name": "John Doe",
        "phone": "1234567890",
        "role": "CUSTOMER",
        "createdAt": "2024-01-01T10:00:00"
    }
}
```

## Next Steps

1. **Restart user-service** to load the new controller
   ```powershell
   cd user-service
   mvn clean install
   mvn spring-boot:run
   ```

2. **Test from frontend** - The Angular app should now work with profile management

3. **Verify endpoints** - All profile operations should work through gateway at `http://localhost:8080/api/profile/**`

## Implementation Complete ✅

- ✅ ProfileController with all required endpoints
- ✅ ProfileService with business logic
- ✅ JWT authentication integration
- ✅ ApiResponse format
- ✅ Address ownership validation
- ✅ Reuses existing DTOs (UserDto, AddressDto)
- ✅ No gateway or frontend changes needed
