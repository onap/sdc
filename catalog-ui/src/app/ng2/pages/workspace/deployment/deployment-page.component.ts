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

import {Component} from "@angular/core";
import {HierarchyTabComponent} from "./panel/panel-tabs/hierarchy-tab/hierarchy-tab.component";
import {ComponentGenericResponse} from "../../../services/responses/component-generic-response";
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";
import {WorkspaceService} from "../workspace.service";
import {Module} from "app/models";
import {SdcUiServices} from "onap-ui-angular";
import {Select} from "@ngxs/store";
import {WorkspaceState} from "../../../store/states/workspace.state";
import {DeploymentGraphService} from "../../composition/deployment/deployment-graph.service";

const tabs =
    {
        hierarchyTab: {
            titleIcon: 'composition-o',
            component: HierarchyTabComponent,
            input: {},
            isActive: true,
            tooltipText: 'Hierarchy'
        }
    };

@Component({
    selector: 'deployment-page',
    templateUrl: './deployment-page.component.html',
    styleUrls: ['deployment-page.component.less']
})

export class DeploymentPageComponent {
    public tabs: Array<any>;
    public resourceType: string;
    public modules: Array<Module>;
    public isDataAvailable: boolean;

    @Select(WorkspaceState.isViewOnly) isViewOnly$: boolean;

    constructor(private topologyTemplateService: TopologyTemplateService,
                private workspaceService: WorkspaceService,
                private deploymentService: DeploymentGraphService,
                private loaderService: SdcUiServices.LoaderService) {
        this.tabs = [];
        this.isDataAvailable = false;
    }

    ngOnInit(): void {
        this.topologyTemplateService.getDeploymentGraphData(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId).subscribe((response: ComponentGenericResponse) => {
            this.deploymentService.componentInstances = response.componentInstances;
            this.deploymentService.componentInstancesRelations = response.componentInstancesRelations;
            this.deploymentService.modules = response.modules;
            this.isDataAvailable = true;
            this.loaderService.deactivate();
        });

        this.loaderService.activate();
        this.resourceType = this.workspaceService.getMetadataType();
        this.tabs.push(tabs.hierarchyTab);
    }
}
