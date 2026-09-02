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
import {IComponentService, ComponentService} from "./component.service";
import {IAppConfigurtaion} from '../../models/app-config';
import {Component} from '../../models/components/component';
import {Resource} from '../../models/components/resource';
import {PropertyModel} from '../../models/properties';
// Direct imports to avoid loading the services-ng2 barrel which causes a circular dep
// at module-load time once @Injectable emits type metadata (emitDecoratorMetadata).
import {SharingService} from "../sharing.service";
import {DataTypesService} from "../data-types.service";
import {SdcConfigToken} from "../../config/sdc-config.config";

export interface IResourceService extends IComponentService {
    updateResourceGroupProperties(uniqueId:string, groupId:string, properties:Array<PropertyModel>):Promise<Array<PropertyModel>>
}

/**
 * @deprecated Superseded by ResourceServiceNg2 (services/component-services/resource.service.ts).
 * Injected in utils/component-factory.ts and handed to models/components/resource.ts, which calls its
 * promise-based API. Both sites move to the Observable API before this can go. Tracked by SDC-4913.
 */
@Injectable()
export class ResourceService extends ComponentService implements IResourceService {

    constructor(@Inject(SdcConfigToken) sdcConfig: IAppConfigurtaion,
                http: HttpClient,
                sharingService: SharingService,
                dataTypeService: DataTypesService) {
        super(sdcConfig, http, sharingService, dataTypeService);
        this.typeName = 'resources';
    }

    createComponentObject = (component: Component): Component => {
        return new Resource(this, <Resource>component);
    };

    updateResourceGroupProperties = (uniqueId: string, groupId: string, properties: Array<PropertyModel>): Promise<Array<PropertyModel>> => {
        return this.http.put(this.url(uniqueId, 'groups', groupId, 'properties'), JSON.stringify(properties)).toPromise()
            .then((updated: any) => _.map(updated, (p: PropertyModel) => new PropertyModel(p))) as any;
    };
}
