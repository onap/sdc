import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { HeadersInterceptor } from './headers-interceptor';
import IStateService = angular.ui.IStateService;

export const httpInterceptorProviders = [
    { provide: HTTP_INTERCEPTORS, useClass: HeadersInterceptor, multi: true },
];
