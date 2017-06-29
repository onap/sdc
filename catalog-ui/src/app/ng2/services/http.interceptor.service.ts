import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import 'rxjs/Rx';
import { sdc2Config } from './../../../main';
import { Interceptor, InterceptedRequest, InterceptedResponse } from 'ng2-interceptors';
import {SharingService} from "../../services/sharing-service";
import {ReflectiveInjector} from '@angular/core';
import {Cookie2Service} from "./cookie.service";
import {UUID} from "angular2-uuid";
import {Dictionary} from "../../utils/dictionary/dictionary";

export class HttpInterceptor implements Interceptor {

    private cookieService: Cookie2Service;
    private sharingService:SharingService;
    constructor() {
        let injector = ReflectiveInjector.resolveAndCreate([Cookie2Service,SharingService]);
        this.cookieService = injector.get(Cookie2Service);
        this.sharingService = injector.get(SharingService);
    }

    public interceptBefore(request: InterceptedRequest): InterceptedRequest {

        /**
         * For every request to the server, that the service id, or resource id is sent in the URL, need to pass UUID in the header.
         * Check if the unique id exists in uuidMap, and if so get the UUID and add it to the header.
         */

        request.options.headers.append(this.cookieService.getUserIdSuffix(), this.cookieService.getUserId());
            var uuidValue = this.getUuidValue(request.url);
            if(uuidValue!= ''){
                request.options.headers.set('X-ECOMP-ServiceID',uuidValue);
            }
            request.options.headers.set('X-ECOMP-RequestID', UUID.UUID());
        return request;

    }

    public interceptAfter(response: InterceptedResponse): InterceptedResponse {
            
        return response;

    }

    private getUuidValue = (url: string) :string => {
        let map:Dictionary<string, string> = this.sharingService.getUuidMap();
        if (map && url.indexOf(sdc2Config.api.root) > 0) {
            map.forEach((key:string) => {
                if (url.indexOf(key) !== -1) {
                    return this.sharingService.getUuidValue(key);
                }
            });
        }
        return '';
    }

}
