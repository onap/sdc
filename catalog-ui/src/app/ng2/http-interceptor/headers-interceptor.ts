import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable, Injector } from '@angular/core';
import { SdcUiComponents, SdcUiServices } from 'onap-ui-angular';
import { ButtonType } from 'onap-ui-angular/dist/common';
import { Observable } from 'rxjs/Observable';
import { ServerErrorResponse } from '../../models/server-error-response';
import { Cookie2Service } from '../services/cookie.service';
import { HttpHelperService } from '../services/http-hepler.service';
import { TranslateService } from '../shared/translator/translate.service';

@Injectable()
export class HeadersInterceptor implements HttpInterceptor {

    constructor(private injector: Injector, private cookieService: Cookie2Service, private httpHelperService: HttpHelperService) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let authReq = req.clone({ headers: req.headers.set(this.cookieService.getUserIdSuffix(), this.cookieService.getUserId())
            .set('Content-Type', 'application/json; charset=UTF-8')
            .set(this.cookieService.getUserIdSuffix(), this.cookieService.getUserId())
            .set(this.cookieService.getUserIdSuffix(), this.cookieService.getUserId())
        });

        const uuidValue = this.httpHelperService.getUuidValue(authReq.url);
        if (uuidValue !== '') {
            authReq = authReq.clone({ headers: authReq.headers.set(this.cookieService.getUserIdSuffix(), this.cookieService.getUserId())});
        }
        return next.handle(authReq).do(

            (event: HttpEvent<any>) => { /* Do Nothing */ },

            (err: any) => {
                if (err instanceof HttpErrorResponse) {
                    const errorResponse: ServerErrorResponse = new ServerErrorResponse(err);
                    const modalService = this.injector.get(SdcUiServices.ModalService);
                    const translateService = this.injector.get(TranslateService);

                    const errorDetails = {
                        'Error Code': errorResponse.messageId,
                        'Status Code': errorResponse.status
                    };

                    if (errorResponse.ecompRequestId) {
                        errorDetails['Transaction ID'] = errorResponse.ecompRequestId;
                    }

                    if (errorResponse.messageId === 'POL5005') {
                        // Session and Role expiration special handling
                        modalService.openWarningModal(
                            'Warning',
                            translateService.translate('ERROR_MODAL_TEXT', errorResponse),
                            'warn-modal',
                            [ ] );
                    } else {
                        modalService.openErrorDetailModal('Error', errorResponse.message, 'error-modal', errorDetails);
                    }

                    return Observable.throwError(err);
                }
            });
    }
}
