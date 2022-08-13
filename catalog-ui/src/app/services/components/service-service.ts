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
'use strict';
import * as _ from "lodash";
import {IComponentService, ComponentService} from "./component-service";
import {Distribution, DistributionComponent, Service, PropertyModel, Component, IAppConfigurtaion} from "app/models";
import {SharingService} from "app/services-ng2";
import { DataTypesService } from "app/services";

export interface IServiceService extends IComponentService {
    getDistributionsList(uuid:string):ng.IPromise<Array<Distribution>>;
    getDistributionComponents(distributionId:string):ng.IPromise<Array<DistributionComponent>>;
    markAsDeployed(serviceId:string, distributionId:string):ng.IPromise<any>;
    updateGroupInstanceProperties(serviceId:string, resourceInstanceId:string, groupInstanceId:string, groupInstanceProperties:Array<PropertyModel>):ng.IPromise<Array<PropertyModel>>;
}

export class ServiceService extends ComponentService implements IServiceService {

    static '$inject' = [
        'Restangular',
        'sdcConfig',
        'Sdc.Services.SharingService',
        'Sdc.Services.DataTypesService',
        '$q',
        '$base64'
    ];

    public distribution:string = "distribution";

    constructor(protected restangular:restangular.IElement,
                protected sdcConfig:IAppConfigurtaion,
                protected sharingService:SharingService,
                protected dataTypeService:DataTypesService,
                protected $q:ng.IQService,
                protected $base64:any) {
        super(restangular, sdcConfig, sharingService, dataTypeService, $q, $base64);

        this.restangular = restangular.one("services");
    }

    getDistributionsList = (uuid:string):ng.IPromise<Array<Distribution>> => {
        let defer = this.$q.defer<Array<Distribution>>();
        this.restangular.one(uuid).one("distribution").get().then((distributions:any) => {
            defer.resolve(<Array<Distribution>> distributions.distributionStatusOfServiceList);
        }, (err)=> {
            defer.reject(err);
        });
        return defer.promise;
    };

    getDistributionComponents = (distributionId:string):ng.IPromise<Array<DistributionComponent>> => {
        let defer = this.$q.defer<Array<DistributionComponent>>();
        this.restangular.one("distribution").one(distributionId).get().then((distributions:any) => {
            defer.resolve(<Array<DistributionComponent>> distributions.distributionStatusList);
        }, (err)=> {
            defer.reject(err);
        });
        return defer.promise;
    };

    markAsDeployed = (serviceId:string, distributionId:string):ng.IPromise<any> => {
        let defer = this.$q.defer<any>();
        this.restangular.one(serviceId).one("distribution").one(distributionId).one("markDeployed").customPOST().then((result:any) => {
            defer.resolve(result);
        }, (err)=> {

            defer.reject(err);
        });
        return defer.promise;
    };

    createComponentObject = (component:Component):Component => {
        return new Service(this, this.$q, <Service>component);
    };

    updateGroupInstanceProperties = (serviceId:string, resourceInstanceId:string, groupInstanceId:string, groupInstanceProperties:Array<PropertyModel>):ng.IPromise<Array<PropertyModel>> => {
        let defer = this.$q.defer<Array<PropertyModel>>();
        this.restangular.one(serviceId).one("resourceInstance").one(resourceInstanceId).one('groupInstance').one(groupInstanceId).customPUT(JSON.stringify(groupInstanceProperties)).then((updatedProperties:any) => {
            let propertiesArray:Array<PropertyModel> = new Array<PropertyModel>();
            _.forEach(updatedProperties, (propertyObj:PropertyModel) => {
                propertiesArray.push(new PropertyModel(propertyObj));
            });
            defer.resolve(propertiesArray);
        }, (err)=> {
            defer.reject(err);
        });
        return defer.promise;
    };
}
