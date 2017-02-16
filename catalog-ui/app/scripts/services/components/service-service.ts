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
/**
 * Created by obarda on 2/4/2016.
 */
/// <reference path="../../references"/>
module Sdc.Services.Components {
    'use strict';


    export interface IServiceService extends IComponentService {
        getDistributionsList(uuid: string): ng.IPromise<Array<Models.Distribution>>;
        getDistributionComponents(distributionId: string): ng.IPromise<Array<Models.DistributionComponent>>;
        markAsDeployed(serviceId: string, distributionId: string): ng.IPromise<any>;
    }

    export class ServiceService extends ComponentService implements IServiceService {

        static '$inject' = [
            '$log',
            'Restangular',
            'sdcConfig',
            'Sdc.Services.SharingService',
            '$q',
            '$interval',
            '$base64',
            'ComponentInstanceFactory'
        ];

        public distribution: string = "distribution";

        constructor(protected $log: ng.ILogService,
                    protected restangular: restangular.IElement,
                    protected sdcConfig: Models.IAppConfigurtaion,
                    protected sharingService: Sdc.Services.SharingService,
                    protected $q: ng.IQService,
                    protected $interval: any,
                    protected $base64: any,
                    protected ComponentInstanceFactory: Utils.ComponentInstanceFactory) {
            super($log, restangular, sdcConfig, sharingService, $q, $interval, $base64, ComponentInstanceFactory);

            this.restangular = restangular.one("services");
        }

        getDistributionsList = (uuid: string): ng.IPromise<Array<Models.Distribution>> => {
            let defer = this.$q.defer<Array<Models.Distribution>>();
            this.restangular.one(uuid).one("distribution").get().then((distributions: any) => {
                defer.resolve(<Array<Models.Distribution>> distributions.distributionStatusOfServiceList);
            }, (err)=> {
                defer.reject(err);
            });
            return defer.promise;
        };

        getDistributionComponents = (distributionId: string): ng.IPromise<Array<Models.DistributionComponent>> => {
            let defer = this.$q.defer<Array<Models.DistributionComponent>>();
            this.restangular.one("distribution").one(distributionId).get().then((distributions: any) => {
                defer.resolve(<Array<Models.DistributionComponent>> distributions.distributionStatusList);
            }, (err)=> {
                defer.reject(err);
            });
            return defer.promise;
        };

        markAsDeployed = (serviceId: string, distributionId: string): ng.IPromise<any> => {
            let defer = this.$q.defer<any>();
            this.restangular.one(serviceId).one("distribution").one(distributionId).one("markDeployed").customPOST().then((result: any) => {
                defer.resolve(result);
            }, (err)=> {

                defer.reject(err);
            });
            return defer.promise;
        };

        createComponentObject = (component: Models.Components.Component): Models.Components.Component => {
            return new Models.Components.Service(this, this.$q, <Models.Components.Service>component);
        };
    }
}
