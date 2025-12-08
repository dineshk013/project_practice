import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

interface PaymentRequest {
  orderId: number;
  amount: number;
}

interface PaymentResponse {
  status: string;
  paymentId: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = `${environment.apiUrl}/payment`;

  constructor(private httpClient: HttpClient) { }

  /**
   * Process dummy Razorpay payment
   * @param orderId - The order ID
   * @param amount - The payment amount
   * @returns Observable with payment response
   */
  processDummyPayment(orderId: number, amount: number): Observable<ApiResponse<PaymentResponse>> {
    const request: PaymentRequest = {
      orderId,
      amount
    };

    return this.httpClient.post<ApiResponse<PaymentResponse>>(
      `${this.apiUrl}/dummy`,
      request
    );
  }
}



