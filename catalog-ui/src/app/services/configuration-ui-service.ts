'use strict'
import {IAppConfigurtaion, IApi} from "../models/app-config";

interface IConfigurationUiService {
    getConfigurationUi():ng.IPromise<any>;
}

export class ConfigurationUiService implements IConfigurationUiService {

    static '$inject' = ['$http', '$q', 'sdcConfig'];
    private api:IApi;

    constructor(private $http:ng.IHttpService, private $q:ng.IQService, sdcConfig:IAppConfigurtaion) {
        this.api = sdcConfig.api;
    }

    public getConfigurationUi = ():ng.IPromise<any> => {
        let defer = this.$q.defer<any>();
        this.$http.get(this.api.root + this.api.GET_configuration_ui)
            .then((result:any) => {
                defer.resolve(result.data);
            });
        return defer.promise;
    }
}
