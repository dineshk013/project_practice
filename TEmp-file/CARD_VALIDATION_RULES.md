# Card Validation Rules - Quick Reference

## 📋 Validation Rules Summary

| Field | Rule | Error Message | Example Valid Input |
|-------|------|---------------|---------------------|
| **Card Number** | Exactly 16 digits, numeric only | "Card number must be exactly 16 digits" | 4532 1234 5678 9010 |
| **CVV** | Exactly 3 digits, numeric only | "CVV must be exactly 3 digits" | 123 |
| **Expiry Month** | 01-12 | "Invalid month (01-12)" | 12 |
| **Expiry Year** | Current year to +20 years | "Card has expired" | 2025 |
| **Cardholder Name** | Letters and spaces only, min 2 chars | "Name must contain only letters" | John Doe |

---

## ✅ Valid Test Cases

### Test Case 1: Valid Visa Card
```
Card Holder Name: John Doe
Card Number: 4532 1234 5678 9010
Expiry Month: 12
Expiry Year: 2025
CVV: 123
```
**Result**: ✅ Form submits successfully

### Test Case 2: Valid Mastercard
```
Card Holder Name: Jane Smith
Card Number: 5425 2334 3010 9903
Expiry Month: 06
Expiry Year: 2026
CVV: 456
```
**Result**: ✅ Form submits successfully

### Test Case 3: Valid with Current Year
```
Card Holder Name: Alice Johnson
Card Number: 4111 1111 1111 1111
Expiry Month: 12 (must be >= current month)
Expiry Year: 2024 (current year)
CVV: 789
```
**Result**: ✅ Form submits if month >= current month

---

## ❌ Invalid Test Cases

### Test Case 4: Short Card Number
```
Card Number: 1234 5678 9012
```
**Result**: ❌ "Card number must be exactly 16 digits"
**Submit Button**: Disabled

### Test Case 5: Long Card Number
```
Card Number: 1234 5678 9012 3456 7890
```
**Result**: ❌ Auto-truncated to 16 digits (1234 5678 9012 3456)

### Test Case 6: Alphabetic Card Number
```
Card Number: abcd efgh ijkl mnop
```
**Result**: ❌ Auto-filtered, only numbers allowed

### Test Case 7: Short CVV
```
CVV: 12
```
**Result**: ❌ "CVV must be exactly 3 digits"
**Submit Button**: Disabled

### Test Case 8: Long CVV
```
CVV: 1234
```
**Result**: ❌ Auto-truncated to 3 digits (123)

### Test Case 9: Alphabetic CVV
```
CVV: abc
```
**Result**: ❌ Auto-filtered, only numbers allowed

### Test Case 10: Invalid Month
```
Expiry Month: 13
```
**Result**: ❌ Not possible (dropdown only shows 01-12)

### Test Case 11: Past Year
```
Expiry Year: 2023
```
**Result**: ❌ "Card has expired"
**Submit Button**: Disabled

### Test Case 12: Past Month (Current Year)
```
Expiry Month: 01
Expiry Year: 2024 (if current month is > 01)
```
**Result**: ❌ "Card has expired"
**Submit Button**: Disabled

### Test Case 13: Far Future Year
```
Expiry Year: 2050 (if current year is 2024)
```
**Result**: ❌ "Invalid expiry year"
**Submit Button**: Disabled

### Test Case 14: Numeric Name
```
Card Holder Name: John123
```
**Result**: ❌ "Name must contain only letters"
**Submit Button**: Disabled

### Test Case 15: Special Characters in Name
```
Card Holder Name: John@Doe
```
**Result**: ❌ "Name must contain only letters"
**Submit Button**: Disabled

### Test Case 16: Empty Name
```
Card Holder Name: (empty)
```
**Result**: ❌ "Card holder name is required"
**Submit Button**: Disabled

### Test Case 17: Short Name
```
Card Holder Name: J
```
**Result**: ❌ "Name must be at least 2 characters"
**Submit Button**: Disabled

---

## 🎯 Auto-Formatting Behavior

### Card Number Formatting:
```
User Types: 1234567890123456
Display: 1234 5678 9012 3456
Stored: 1234 5678 9012 3456
```

### CVV Formatting:
```
User Types: 123
Display: 123
Stored: 123
Max Length: 3 digits
```

### Name Formatting:
```
User Types: John Doe
Display: John Doe
Stored: John Doe
Allowed: Letters and spaces only
```

---

## 🔄 Real-Time Validation

### When Validation Occurs:
1. **On Input**: Auto-formatting applied (card number spacing, digit filtering)
2. **On Blur**: Field-level validation (not implemented, but can be added)
3. **On Submit**: Full form validation before submission

### Visual Feedback:
- ✅ Valid field: Normal border (gray)
- ❌ Invalid field: Red border + error message below
- 🔒 Submit button: Disabled until all fields valid

---

## 📱 User Experience

### Good UX Features:
1. **Auto-spacing**: Card number automatically formatted as user types
2. **Digit filtering**: Non-numeric characters automatically removed
3. **Max length**: Cannot exceed maximum length
4. **Dropdown selection**: Month and year use dropdowns (no typing errors)
5. **Clear errors**: Red text below each invalid field
6. **Disabled submit**: Cannot submit invalid form
7. **Processing state**: Button shows "Processing..." during payment

### Error Message Placement:
```
[Input Field]
❌ Error message appears here in red text
```

---

## 🧪 Testing Matrix

| Field | Valid Input | Invalid Input | Expected Behavior |
|-------|-------------|---------------|-------------------|
| Card Number | 4532123456789010 | 123456 | Red error, submit disabled |
| Card Number | 4532123456789010 | abcd1234 | Auto-filter letters |
| CVV | 123 | 12 | Red error, submit disabled |
| CVV | 456 | abc | Auto-filter letters |
| Month | 12 | 13 | Not possible (dropdown) |
| Year | 2025 | 2023 | Red error, submit disabled |
| Name | John Doe | John123 | Red error, submit disabled |

---

## 🚀 Quick Test Commands

### Test Valid Card (via Frontend):
1. Open http://localhost:4200
2. Add items to cart
3. Go to checkout
4. Select "Credit/Debit Card"
5. Enter:
   - Name: John Doe
   - Card: 4532 1234 5678 9010
   - Month: 12
   - Year: 2025
   - CVV: 123
6. Click "Pay Now"

**Expected**: Payment processed successfully

### Test Invalid Card (via Frontend):
1. Follow steps 1-4 above
2. Enter:
   - Name: John123 (invalid)
   - Card: 1234 5678 (too short)
   - Month: 12
   - Year: 2023 (expired)
   - CVV: 12 (too short)
3. Try to click "Pay Now"

**Expected**: Submit button disabled, red errors shown

---

## 📊 Validation Flow Diagram

```
User enters card details
         ↓
Auto-formatting applied (card number, CVV)
         ↓
User clicks "Pay Now"
         ↓
validateForm() called
         ↓
Check each field:
  - Card number: 16 digits?
  - CVV: 3 digits?
  - Month: 01-12?
  - Year: >= current year?
  - Name: letters only?
         ↓
All valid? → Submit payment
Any invalid? → Show errors, disable submit
```

---

## 🎓 Implementation Details

### Frontend Component:
- **File**: `Frontend/src/app/shared/components/payment-form-modal/payment-form-modal.component.ts`
- **Validation Method**: `validateForm(): boolean`
- **Formatting Methods**: `formatCardNumber()`, `formatCVV()`
- **Error Display**: `errors` signal with reactive updates

### Validation Logic:
```typescript
// Card Number
const cardNumberDigits = details.cardNumber.replace(/\s/g, '');
if (cardNumberDigits.length !== 16) {
    newErrors.cardNumber = 'Card number must be exactly 16 digits';
}

// CVV
if (details.cvv.length !== 3) {
    newErrors.cvv = 'CVV must be exactly 3 digits';
}

// Expiry
const currentYear = new Date().getFullYear();
if (year < currentYear) {
    newErrors.expiryYear = 'Card has expired';
}

// Name
if (!/^[a-zA-Z\s]+$/.test(details.cardHolderName)) {
    newErrors.cardHolderName = 'Name must contain only letters';
}
```

---

**Use this guide for quick reference during testing! 📝**
