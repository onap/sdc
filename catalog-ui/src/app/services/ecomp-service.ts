'use strict';
import {IAppConfigurtaion, IApi} from "../models/app-config";

interface IEcompHeaderService {
    getMenuItems(userId):ng.IPromise<Array<any>>;
}

export class EcompHeaderService implements IEcompHeaderService {
    static '$inject' = ['$http', '$q', 'sdcConfig'];
    private api:IApi;

    constructor(private $http:ng.IHttpService,
                private $q:ng.IQService,
                private sdcConfig:IAppConfigurtaion) {
        this.api = sdcConfig.api;
    }

    getMenuItems = (userId):ng.IPromise<Array<any>> => {
        let defer = this.$q.defer<Array<any>>();
        //defer.resolve(this.mockData);
        this.$http.get(this.api.root + this.api.GET_ecomp_menu_items.replace(':userId', userId))
            .then((response:any) => {
                defer.resolve(response.data);
            }, (response) => {
                defer.reject(response.data);
            });

        return defer.promise;
    };
}
