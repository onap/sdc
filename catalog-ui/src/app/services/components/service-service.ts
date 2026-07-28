/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
import {Inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {IComponentService, ComponentService} from "./component-service";
import {Distribution, DistributionComponent, Service, PropertyModel, Component, IAppConfigurtaion} from "../../models";
// Direct imports to avoid loading the services-ng2 barrel which causes a circular dep
// at module-load time once @Injectable emits type metadata (emitDecoratorMetadata).
import {SharingService} from "../../ng2/services/sharing.service";
import {DataTypesService} from "../data-types-service";
import {SdcConfigToken} from "../../ng2/config/sdc-config.config";

export interface IServiceService extends IComponentService {
    getDistributionsList(uuid:string):ng.IPromise<Array<Distribution>>;
    getDistributionComponents(distributionId:string):ng.IPromise<Array<DistributionComponent>>;
    markAsDeployed(serviceId:string, distributionId:string):ng.IPromise<any>;
    updateGroupInstanceProperties(serviceId:string, resourceInstanceId:string, groupInstanceId:string, groupInstanceProperties:Array<PropertyModel>):ng.IPromise<Array<PropertyModel>>;
}

@Injectable()
export class ServiceService extends ComponentService implements IServiceService {

    public distribution: string = "distribution";

    constructor(@Inject(SdcConfigToken) sdcConfig: IAppConfigurtaion,
                http: HttpClient,
                sharingService: SharingService,
                dataTypeService: DataTypesService,
                @Inject('$q') $q: ng.IQService) {
        super(sdcConfig, http, sharingService, dataTypeService, $q);
        this.typeName = 'services';
    }

    createComponentObject = (component: Component): Component => {
        return new Service(this, this.$q, <Service>component);
    };

    getDistributionsList = (uuid: string): ng.IPromise<Array<Distribution>> => {
        return this.http.get(this.url(uuid, 'distribution')).toPromise()
            .then((distributions: any) => <Array<Distribution>> distributions.distributionStatusOfServiceList) as any;
    };

    getDistributionComponents = (distributionId: string): ng.IPromise<Array<DistributionComponent>> => {
        return this.http.get(this.url('distribution', distributionId)).toPromise()
            .then((distributions: any) => <Array<DistributionComponent>> distributions.distributionStatusList) as any;
    };

    markAsDeployed = (serviceId: string, distributionId: string): ng.IPromise<any> => {
        return this.http.post(this.url(serviceId, 'distribution', distributionId, 'markDeployed'), null).toPromise() as any;
    };

    updateGroupInstanceProperties = (serviceId: string, resourceInstanceId: string, groupInstanceId: string, groupInstanceProperties: Array<PropertyModel>): ng.IPromise<Array<PropertyModel>> => {
        return this.http.put(this.url(serviceId, 'resourceInstance', resourceInstanceId, 'groupInstance', groupInstanceId), JSON.stringify(groupInstanceProperties)).toPromise()
            .then((updated: any) => _.map(updated, (p: PropertyModel) => new PropertyModel(p))) as any;
    };
}
