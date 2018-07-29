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

import * as _ from "lodash";
import { Component, Inject, Input, Output, EventEmitter, OnChanges, SimpleChanges } from "@angular/core";
import { TranslateService } from './../../../../../shared/translator/translate.service';
import { PolicyInstance } from 'app/models/graph/zones/policy-instance';
import { PropertyBEModel } from 'app/models';
import { PropertyModel } from './../../../../../../models/properties';
import { ModalsHandler } from "app/utils";
import { Component as TopologyTemplate, ComponentInstance, IAppMenu } from "app/models";

@Component({
    selector: 'policy-properties-tab',
    templateUrl: './policy-properties-tab.component.html',
    styleUrls: ['./../base/base-tab.component.less', 'policy-properties-tab.component.less'],
    host: {'class': 'component-details-panel-tab-policy-properties'}
})
export class PolicyPropertiesTabComponent implements OnChanges {
 
    @Input() policy:PolicyInstance;
    @Input() topologyTemplate:TopologyTemplate;
    @Input() isViewOnly: boolean;

    private properties:Array<PropertyModel>;

    constructor(private translateService:TranslateService, private ModalsHandler:ModalsHandler) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        console.log("PolicyPropertiesTabComponent: ngAfterViewInit: ");        
        console.log("policy: " + this.policy);  
        this.properties = [];
        this.initProperties();
    }

    initProperties = ():void => {
        this.properties= this.policy.properties;
    }

    editProperty = (property?:PropertyModel):void => {
        this.ModalsHandler.openEditPropertyModal((property ? property : new PropertyModel()), this.topologyTemplate, this.properties, false, 'policy', this.policy.uniqueId).then((updatedProperty:PropertyModel) => {
            console.log("ok");
        });
    }

}
