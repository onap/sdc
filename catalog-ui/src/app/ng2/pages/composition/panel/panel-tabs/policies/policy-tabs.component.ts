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
import { Component, Inject, Input, Output, EventEmitter, AfterViewInit, OnChanges } from "@angular/core";
import { TranslateService } from './../../../../../shared/translator/translate.service';
import { PoliciesService } from "../../../../../services/policies.service";
import { Component as TopologyTemplate, ComponentInstance, IAppMenu } from "app/models";
import { PolicyInstance } from 'app/models/graph/zones/policy-instance';
import { GRAPH_EVENTS } from './../../../../../../utils/constants';
import { EventListenerService } from 'app/services/event-listener-service';
import { ZoneInstance } from 'app/models/graph/zones/zone-instance';
import { SimpleChanges } from "@angular/core/src/metadata/lifecycle_hooks";

@Component({
    selector: 'policy-tabs',
    templateUrl: './policy-tabs.component.html'
})
export class PolicyTabsComponent implements OnChanges {
 
    @Input() topologyTemplate:TopologyTemplate;
    @Input() selectedZoneInstanceType:string;
    @Input() selectedZoneInstanceId:string;
    @Input() isViewOnly: boolean;
    @Output() isLoading: EventEmitter<boolean> = new EventEmitter<boolean>();

    private policy:PolicyInstance;

    constructor(private translateService:TranslateService, 
                private policiesService:PoliciesService
            ) {

    }

    ngOnChanges(changes: SimpleChanges): void {
        this.initPolicy();
    }

    private initPolicy = ():void => {
        this.isLoading.emit(true);
        this.policiesService.getSpecificPolicy(this.topologyTemplate.componentType, this.topologyTemplate.uniqueId, this.selectedZoneInstanceId).subscribe(
            policy => {
                this.policy = policy;
                console.log(JSON.stringify(policy));
            },
            error => console.log("Error getting policy!"),
            () => this.isLoading.emit(false)
        );
    }

    private setIsLoading = (value) :void => {
        this.isLoading.emit(value);
    }

}
