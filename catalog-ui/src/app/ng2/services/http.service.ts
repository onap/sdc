/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import {Injectable, Inject} from '@angular/core';
import {Http, XHRBackend, RequestOptions, Request, RequestOptionsArgs, Response, Headers} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {UUID} from 'angular2-uuid';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import {Dictionary} from "../../utils/dictionary/dictionary";
import {SharingService, CookieService} from "app/services";
import { ModalService } from "app/ng2/services/modal.service";
import { ServerErrorResponse } from "app/models";
import {ErrorMessageComponent} from "../components/ui/modal/error-message/error-message.component";
import {SdcConfigToken, ISdcConfig} from "../config/sdc-config.config";

@Injectable()
export class HttpService extends Http {

    constructor(backend: XHRBackend, options: RequestOptions, private sharingService: SharingService, private cookieService: CookieService, private modalService: ModalService, @Inject(SdcConfigToken) private sdcConfig:ISdcConfig) {
        super(backend, options);
        this._defaultOptions.withCredentials = true;
        this._defaultOptions.headers.append(cookieService.getUserIdSuffix(), cookieService.getUserId());
    }

    request(request:string|Request, options?:RequestOptionsArgs):Observable<Response> {
        /**
         * For every request to the server, that the service id, or resource id is sent in the URL, need to pass UUID in the header.
         * Check if the unique id exists in uuidMap, and if so get the UUID and add it to the header.
         */
        if (typeof request === 'string') { // meaning we have to add the token to the options, not in url
            if (!options) {
                // make option object
                options = {headers: new Headers()};
            }

            var uuidValue = this.getUuidValue(request);
            if(uuidValue!= ''){
                options.headers['X-ECOMP-ServiceID'] = uuidValue;

            }
            options.headers.set('X-ECOMP-RequestID', UUID.UUID());

        } else {
            // we have to add the token to the url object
            var uuidValue = this.getUuidValue((<Request>request).url);
            if(uuidValue!= ''){
                 request.headers.set('X-ECOMP-ServiceID',uuidValue);

            }
            request.headers.set('X-ECOMP-RequestID', UUID.UUID());
        }
        return super.request(request, options).catch((err) => this.catchError(err));
    }

    private getUuidValue = (url: string) :string => {
        let map:Dictionary<string, string> = this.sharingService.getUuidMap();
        if (map && url.indexOf(this.sdcConfig.api.root) > 0) {
            map.forEach((key:string) => {
                if (url.indexOf(key) !== -1) {
                    return this.sharingService.getUuidValue(key);
                }
            });
        }
        return '';
    }

    private catchError = (response: Response): Observable<any> => {
        
        let modalInstance = this.modalService.createErrorModal("OK");
        let errorResponse: ServerErrorResponse = new ServerErrorResponse(response);
        this.modalService.addDynamicContentToModal(modalInstance, ErrorMessageComponent, errorResponse);
        modalInstance.instance.open();
        
        return Observable.throw(response);
    };

    public static replaceUrlParams(url:string, urlParams:{[index:string]:any}):string {
        return url.replace(/:(\w+)/g, (m, p1):string => urlParams[p1] || '');
    }

}
