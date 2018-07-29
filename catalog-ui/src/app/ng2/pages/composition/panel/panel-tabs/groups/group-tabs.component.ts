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
import { Component, Inject, Input, Output, EventEmitter, SimpleChanges, OnChanges } from "@angular/core";
import { TranslateService } from './../../../../../shared/translator/translate.service';
import { Component as TopologyTemplate, ComponentInstance, IAppMenu } from "app/models";
import { GroupsService } from '../../../../../services/groups.service';
import { GroupInstance } from "app/models/graph/zones/group-instance";

@Component({
    selector: 'group-tabs',
    templateUrl: './group-tabs.component.html'
})
export class GroupTabsComponent implements OnChanges {
 
    @Input() topologyTemplate:TopologyTemplate;
    @Input() selectedZoneInstanceType:string;
    @Input() selectedZoneInstanceId:string;
    @Input() isViewOnly: boolean;
    @Output() isLoading: EventEmitter<boolean> = new EventEmitter<boolean>();

    private group:GroupInstance;

    constructor(private translateService:TranslateService,
                private groupsService:GroupsService
        ) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.initGroup();
    }

    private initGroup = ():void => {
        this.isLoading.emit(true);
        this.groupsService.getSpecificGroup(this.topologyTemplate.componentType, this.topologyTemplate.uniqueId, this.selectedZoneInstanceId).subscribe(
            group => {
                this.group = group;
                console.log(JSON.stringify(group));
            },
            error => console.log("Error getting group!"),
            () => this.isLoading.emit(false)
        );
    }

    private setIsLoading = (value) :void => {
        this.isLoading.emit(value);
    }

}
