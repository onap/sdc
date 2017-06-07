'use strict';
import {Distribution} from "../models/distribution";
import {IAppConfigurtaion, IApi} from "../models/app-config";

export interface ISdcVersionService {
    getVersion():ng.IPromise<any>;
}
export class SdcVersionService implements ISdcVersionService {

    static '$inject' = ['$http', '$q', 'sdcConfig'];
    private api:IApi;

    constructor(private $http:ng.IHttpService, private $q:ng.IQService, sdcConfig:IAppConfigurtaion) {
        this.api = sdcConfig.api;
    }

    public getVersion():ng.IPromise<any> {
        let defer = this.$q.defer<Array<Distribution>>();
        this.$http.get(this.api.root + this.api.GET_SDC_Version)
            .then((version:any) => {
                defer.resolve(version.data);
            });
        return defer.promise;
    }
}

