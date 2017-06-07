'use strict';
import {IAppConfigurtaion} from "../models/app-config";
import {Dictionary} from "../utils/dictionary/dictionary";
import {SharingService} from "./sharing-service";

//Method name should be exactly "response" - http://docs.angularjs.org/api/ng/service/$http
export interface IInterceptor {
    request:Function;

}

export class HeaderInterceptor implements IInterceptor {
    public static $inject = [
        '$injector',
        '$q',
        'uuid4',
        'Sdc.Services.SharingService',
        'sdcConfig',
        '$location'
    ];

    public static Factory($injector:ng.auto.IInjectorService,
                          $q:ng.IQService,
                          uuid4:any,
                          sharingService:SharingService,
                          sdcConfig:IAppConfigurtaion,
                          $location:ng.ILocationService) {
        return new HeaderInterceptor($injector, $q, uuid4, sharingService, sdcConfig, $location);
    }

    constructor(private $injector:ng.auto.IInjectorService,
                private $q:ng.IQService,
                private uuid4:any,
                private sharingService:SharingService,
                private sdcConfig:IAppConfigurtaion,
                private $location:ng.ILocationService) {
        console.debug('header-interceptor: initializing AuthenticationInterceptor');
    }

    public request = (requestSuccess):ng.IPromise<any> => {
        requestSuccess.headers['X-ECOMP-RequestID'] = this.uuid4.generate();
        /**
         * For every request to the server, that the service id, or resource id is sent in the URL, need to pass UUID in the header.
         * Check if the unique id exists in uuidMap, and if so get the UUID and add it to the header.
         */
        let map:Dictionary<string, string> = this.sharingService.getUuidMap();
        if (map && requestSuccess.url.indexOf(this.sdcConfig.api.root) === 0) {
            console.log("header-interceptor: url: " + requestSuccess.url);
            map.forEach((key:string) => {
                if (requestSuccess.url.indexOf(key) !== -1) {
                    requestSuccess.headers['X-ECOMP-ServiceID'] = this.sharingService.getUuidValue(key);
                }
            });
        }
        return requestSuccess;
    };

    public response = (responseSuccess):ng.IPromise<any> => {
        let responseData = responseSuccess.data;
        if (responseData) {
            let data = JSON.stringify(responseData);
            if (data && (data.indexOf("Global Logon: Login") > 0)) {
                this.$location.path('dashboard/welcome');
                window.location.reload();
            }
        }
        return responseSuccess;
    }
}
