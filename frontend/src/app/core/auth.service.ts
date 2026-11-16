import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginResponse } from './api.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly accessKey = 'eplatform.accessToken';
  private readonly refreshKey = 'eplatform.refreshToken';
  readonly signedIn = signal(!!localStorage.getItem(this.accessKey));
  readonly email = signal(localStorage.getItem('eplatform.email') ?? '');
  constructor(private readonly http: HttpClient) {}

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/v1/auth/login', { email, password }).pipe(tap((session) => this.save(session)));
  }
  register(payload: { email: string; password: string; firstName: string; lastName: string }): Observable<unknown> {
    return this.http.post('/api/v1/auth/register', payload);
  }
  refresh(): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/v1/auth/refresh', { refreshToken: localStorage.getItem(this.refreshKey) }).pipe(tap((session) => this.save(session)));
  }
  logout(): Observable<void> {
    return this.http.post<void>('/api/v1/auth/logout', { refreshToken: localStorage.getItem(this.refreshKey) }).pipe(tap(() => this.clear()));
  }
  token(): string | null { return localStorage.getItem(this.accessKey); }
  clear(): void { localStorage.removeItem(this.accessKey); localStorage.removeItem(this.refreshKey); localStorage.removeItem('eplatform.email'); this.signedIn.set(false); this.email.set(''); }
  private save(session: LoginResponse): void {
    localStorage.setItem(this.accessKey, session.accessToken); localStorage.setItem(this.refreshKey, session.refreshToken); localStorage.setItem('eplatform.email', session.email);
    this.email.set(session.email); this.signedIn.set(true);
  }
}
