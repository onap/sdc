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
import {ILegacyComponentService, LegacyComponentService} from "./component.service";
import {IAppConfigurtaion} from '../../models/app-config';
import {Component} from '../../models/components/component';
import {Service} from '../../models/components/service';
import {Distribution, DistributionComponent} from '../../models/distribution';
import {PropertyModel} from '../../models/properties';
// Direct imports to avoid loading the services-ng2 barrel which causes a circular dep
// at module-load time once @Injectable emits type metadata (emitDecoratorMetadata).
import {SharingService} from "../sharing.service";
import {DataTypesService} from "../data-types.service";
import {SdcConfigToken} from "../../config/sdc-config.config";

export interface ILegacyServiceService extends ILegacyComponentService {
    getDistributionsList(uuid:string):Promise<Array<Distribution>>;
    getDistributionComponents(distributionId:string):Promise<Array<DistributionComponent>>;
    markAsDeployed(serviceId:string, distributionId:string):Promise<any>;
    updateGroupInstanceProperties(serviceId:string, resourceInstanceId:string, groupInstanceId:string, groupInstanceProperties:Array<PropertyModel>):Promise<Array<PropertyModel>>;
}

/**
 * @deprecated Superseded by ServiceService (services/component-services/service.service.ts).
 * Injected in utils/component-factory.ts and handed to models/components/service.ts, which calls its
 * promise-based API. Both sites move to the Observable API before this can go. Tracked by SDC-4913.
 *
 * The file keeps the counterpart's name (`service.service.ts`) while the class carries the
 * `Legacy` prefix: the directory already supplies the qualifier, and keeping the basenames
 * paired makes the eventual dedup diff a one-directory delete.
 */
@Injectable()
export class LegacyServiceService extends LegacyComponentService implements ILegacyServiceService {

    public distribution: string = "distribution";

    constructor(@Inject(SdcConfigToken) sdcConfig: IAppConfigurtaion,
                http: HttpClient,
                sharingService: SharingService,
                dataTypeService: DataTypesService) {
        super(sdcConfig, http, sharingService, dataTypeService);
        this.typeName = 'services';
    }

    createComponentObject = (component: Component): Component => {
        return new Service(this, <Service>component);
    };

    getDistributionsList = (uuid: string): Promise<Array<Distribution>> => {
        return this.http.get(this.url(uuid, 'distribution')).toPromise()
            .then((distributions: any) => <Array<Distribution>> distributions.distributionStatusOfServiceList) as any;
    };

    getDistributionComponents = (distributionId: string): Promise<Array<DistributionComponent>> => {
        return this.http.get(this.url('distribution', distributionId)).toPromise()
            .then((distributions: any) => <Array<DistributionComponent>> distributions.distributionStatusList) as any;
    };

    markAsDeployed = (serviceId: string, distributionId: string): Promise<any> => {
        return this.http.post(this.url(serviceId, 'distribution', distributionId, 'markDeployed'), null).toPromise() as any;
    };

    updateGroupInstanceProperties = (serviceId: string, resourceInstanceId: string, groupInstanceId: string, groupInstanceProperties: Array<PropertyModel>): Promise<Array<PropertyModel>> => {
        return this.http.put(this.url(serviceId, 'resourceInstance', resourceInstanceId, 'groupInstance', groupInstanceId), JSON.stringify(groupInstanceProperties)).toPromise()
            .then((updated: any) => _.map(updated, (p: PropertyModel) => new PropertyModel(p))) as any;
    };
}
