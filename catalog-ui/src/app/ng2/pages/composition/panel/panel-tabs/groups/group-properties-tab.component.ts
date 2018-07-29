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
import { GroupInstance } from 'app/models/graph/zones/group-instance';
import { PropertyBEModel } from 'app/models';
import { PropertyModel } from './../../../../../../models/properties';
import { ModalsHandler } from "app/utils";
import { Component as TopologyTemplate, ComponentInstance, IAppMenu } from "app/models";

@Component({
    selector: 'group-properties-tab',
    templateUrl: './group-properties-tab.component.html',
    styleUrls: ['./../base/base-tab.component.less', 'group-properties-tab.component.less'],
    host: {'class': 'component-details-panel-tab-group-properties'}
})
export class GroupPropertiesTabComponent implements OnChanges {
 
    @Input() group:GroupInstance;
    @Input() topologyTemplate:TopologyTemplate;
    @Input() isViewOnly: boolean;

    private properties:Array<PropertyModel>;

    constructor(private translateService:TranslateService, private ModalsHandler:ModalsHandler) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        console.log("GroupPropertiesTabComponent: ngAfterViewInit: ");        
        console.log("group: " + JSON.stringify(this.group));  
        this.properties = [];
        this.initProperties();
    }

    initProperties = ():void => {
        this.properties= this.group.properties;
    }

    editProperty = (property?:PropertyModel):void => {
        this.ModalsHandler.openEditPropertyModal((property ? property : new PropertyModel()), this.topologyTemplate, this.properties, false, 'group', this.group.uniqueId).then((updatedProperty:PropertyModel) => {
            console.log("ok");
        });
    }

}
