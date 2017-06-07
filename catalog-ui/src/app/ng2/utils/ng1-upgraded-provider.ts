/**
 * Created by rc2122 on 4/6/2017.
 */
import {DataTypesService} from "../../services/data-types-service";
import ICacheObject = angular.ICacheObject;
import {SharingService} from "../../services/sharing-service";
import {CookieService} from "../../services/cookie-service";

/** Services we need to upgrade from angular1 to angular2 - in the future we need to rewrite them all to angular2 **/

export function dataTypesServiceFactory(cacheObj: ICacheObject) {
    return cacheObj.get('Sdc.Services.DataTypesService');
}

export function sharingServiceFactory(cacheObj: ICacheObject) {
    return cacheObj.get('Sdc.Services.SharingService');
}

export function cookieServiceFactory(cacheObj: ICacheObject) {
    return cacheObj.get('Sdc.Services.CookieService');
}

export function stateParamsServiceFactory(cacheObj: ICacheObject) {
    return cacheObj.get('$stateParams');
}

export const DataTypesServiceProvider = {
    provide: DataTypesService,
    useFactory: dataTypesServiceFactory,
    deps: ['$injector']
};


export const SharingServiceProvider = {
    provide: SharingService,
    useFactory: sharingServiceFactory,
    deps: ['$injector']
};


export const CookieServiceProvider = {
    provide: CookieService,
    useFactory: cookieServiceFactory,
    deps: ['$injector']
};

export const StateParamsServiceFactory = {
    provide: '$stateParams',
    useFactory: stateParamsServiceFactory,
    deps: ['$injector']
};
