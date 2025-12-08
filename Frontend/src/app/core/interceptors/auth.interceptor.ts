import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('revcart_token') : null;
  const userStr = typeof localStorage !== 'undefined' ? localStorage.getItem('revcart_user') : null;
  const user = userStr ? JSON.parse(userStr) : null;

  const headers: any = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  if (user && user.id) {
    headers['X-User-Id'] = user.id.toString();
  }

  let clonedRequest = req;
  if (Object.keys(headers).length > 0) {
    clonedRequest = req.clone({ setHeaders: headers });
  }

  return next(clonedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        if (typeof localStorage !== 'undefined') {
          localStorage.removeItem('revcart_token');
          localStorage.removeItem('revcart_user');
        }
        authService.logout();
        router.navigate(['/auth/login']);
      } else if (error.status === 403) {
        router.navigate(['/']);
      }
      return throwError(() => error);
    })
  );
};
