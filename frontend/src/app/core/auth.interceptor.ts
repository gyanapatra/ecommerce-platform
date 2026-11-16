import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, catchError, finalize, shareReplay, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { LoginResponse } from './api.models';

let refreshInFlight: Observable<LoginResponse> | undefined;

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const authenticated = token ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request;
  return next(authenticated).pipe(catchError((error: unknown) => {
    const response = error as { status?: number };
    const isAuthRequest = request.url.includes('/api/v1/auth/');
    const hasRefreshToken = !!localStorage.getItem('eplatform.refreshToken');
    if (response.status !== 401 || isAuthRequest || !hasRefreshToken) return throwError(() => error);

    refreshInFlight ??= auth.refresh().pipe(
      shareReplay({ bufferSize: 1, refCount: false }),
      finalize(() => { refreshInFlight = undefined; })
    );
    return refreshInFlight.pipe(
      switchMap(() => {
        const freshToken = auth.token();
        return next(request.clone({ setHeaders: { Authorization: `Bearer ${freshToken}` } }));
      }),
      catchError((refreshError: unknown) => { auth.clear(); return throwError(() => refreshError); })
    );
  }));
};
