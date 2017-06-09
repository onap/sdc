'use strict';
import {IAppConfigurtaion} from "../models/app-config";

export class AngularJSBridge {
    private static _$filter:ng.IFilterService;
    private static _sdcConfig:IAppConfigurtaion;

    public static getFilter(filterName:string) {
        return AngularJSBridge._$filter(filterName);
    }

    public static getAngularConfig() {
        return AngularJSBridge._sdcConfig;
    }


    constructor($filter:ng.IFilterService, sdcConfig:IAppConfigurtaion) {
        AngularJSBridge._$filter = $filter;
        AngularJSBridge._sdcConfig = sdcConfig;
    }
}

AngularJSBridge.$inject = ['$filter', 'sdcConfig']
