import {Injectable} from '@angular/core';
import {Http, XHRBackend, RequestOptions, Request, RequestOptionsArgs, Response, Headers} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {UUID} from 'angular2-uuid';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Dictionary} from "../../utils/dictionary/dictionary";
import {SharingService, CookieService} from "app/services";
import {sdc2Config} from './../../../main';

@Injectable()
export class HttpService extends Http {

    constructor(backend:XHRBackend, options:RequestOptions, private sharingService:SharingService, private cookieService: CookieService) {
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
        return super.request(request, options).catch(this.catchAuthError(this));
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

    private catchAuthError(self:HttpService) {
        // we have to pass HttpService's own instance here as `self`
        return (res:Response) => {
            console.log(res);
            if (res.status === 401 || res.status === 403) {
                // if not authenticated
                console.log(res);
            }
            return Observable.throw(res);
        };
    }
}
