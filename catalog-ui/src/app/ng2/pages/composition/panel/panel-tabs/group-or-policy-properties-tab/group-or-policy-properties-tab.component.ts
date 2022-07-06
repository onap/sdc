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
import { Component, Inject, Input} from "@angular/core";
import { TranslateService } from './../../../../../shared/translator/translate.service';
import { PolicyInstance } from 'app/models/graph/zones/policy-instance';
import { PropertyModel } from './../../../../../../models/properties';
import { ModalsHandler } from "app/utils";
import { Component as TopologyTemplate, GroupInstance } from "app/models";

@Component({
    selector: 'group-or-policy-properties-tab',
    templateUrl: './group-or-policy-properties-tab.component.html',
    styleUrls: ['./../properties-tab/properties-tab.component.less',
                './group-or-policy-properties-tab.component.less'],
})
export class GroupOrPolicyPropertiesTab {
 
    @Input() component: GroupInstance | PolicyInstance;
    @Input() topologyTemplate:TopologyTemplate;
    @Input() isViewOnly: boolean;
    @Input() input: {type: string};


    constructor(private translateService:TranslateService, private ModalsHandler:ModalsHandler) {
    }


    editProperty = (property?:PropertyModel):void => {
        this.ModalsHandler.openEditPropertyModal((property ? property : new PropertyModel()), this.topologyTemplate, this.component.properties, false, this.input.type, this.component.uniqueId).then((updatedProperty:PropertyModel) => {
            console.log("ok");
        });
    }

}
