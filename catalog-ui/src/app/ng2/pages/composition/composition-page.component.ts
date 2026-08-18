/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
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
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Component as TopologyTemplate } from 'app/models';
import * as Constants from 'constants';
import { EventListenerService } from '../../../services/event-listener-service';
import { EVENTS } from '../../../utils';
import { WorkspaceService } from '../workspace/workspace.service';

@Component({
    templateUrl: './composition-page.component.html',
    styleUrls: ['composition-page.component.less']
})
export class CompositionPageComponent implements OnInit, OnDestroy {

    topologyTemplate: TopologyTemplate;

    constructor(private workspaceService: WorkspaceService, private eventListenerService: EventListenerService) {
    }

    // Read in ngOnInit, not the constructor: the route resolver that writes
    // WorkspaceService.component is only guaranteed to have run by ngOnInit.
    ngOnInit(): void {
        this.topologyTemplate = this.workspaceService.component;
        this.eventListenerService.registerObserverCallback(EVENTS.ON_CHECKOUT, (comp) => {
            this.topologyTemplate = comp;
        });
    }

    ngOnDestroy(): void {
        this.eventListenerService.unRegisterObserver(EVENTS.ON_CHECKOUT);
    }
}
