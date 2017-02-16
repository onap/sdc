module Sdc.Services {
    export class AngularJSBridge{
        private static _$filter: ng.IFilterService;
        private static _sdcConfig: Models.IAppConfigurtaion;

        public static getFilter(filterName: string){
            return AngularJSBridge._$filter(filterName);
        }

        public static getAngularConfig(){
            return AngularJSBridge._sdcConfig;
        }


        constructor($filter: ng.IFilterService, sdcConfig: Models.IAppConfigurtaion){
            AngularJSBridge._$filter = $filter;
            AngularJSBridge._sdcConfig = sdcConfig;
        }
    }

    AngularJSBridge.$inject = ['$filter', 'sdcConfig']
}