import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

interface DummyPaymentRequest {
  orderId: number;
  userId: number;
  amount: number;
  paymentMethod: string;
}

interface PaymentResponse {
  status: string;
  paymentId?: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = `${environment.apiUrl}/payments`;

  constructor(private httpClient: HttpClient) { }

  /**
   * Process dummy payment
   * @param orderId - The order ID
   * @param userId - The user ID
   * @param amount - The payment amount
   * @param paymentMethod - The payment method
   * @returns Observable with payment response
   */
  processDummyPayment(orderId: number, userId: number, amount: number, paymentMethod: string, upiId?: string): Observable<ApiResponse<PaymentResponse>> {
    const request: any = {
      orderId,
      userId,
      amount,
      paymentMethod
    };
    
    if (upiId) {
      request.upiId = upiId;
    }

    console.log('Processing payment:', request);
    
    return this.httpClient.post<ApiResponse<PaymentResponse>>(
      `${this.apiUrl}/dummy`,
      request
    );
  }
}



