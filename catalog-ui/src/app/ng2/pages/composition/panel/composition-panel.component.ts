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

import { Component, HostBinding, Input } from '@angular/core';
import { Select, Store } from '@ngxs/store';
import { Component as TopologyTemplate, ComponentInstance, FullComponentInstance, GroupInstance, PolicyInstance, Resource, Service } from 'app/models';
import { ArtifactsTabComponent } from 'app/ng2/pages/composition/panel/panel-tabs/artifacts-tab/artifacts-tab.component';
import { GroupMembersTabComponent } from 'app/ng2/pages/composition/panel/panel-tabs/group-members-tab/group-members-tab.component';
import { GroupOrPolicyPropertiesTab } from 'app/ng2/pages/composition/panel/panel-tabs/group-or-policy-properties-tab/group-or-policy-properties-tab.component';
import { InfoTabComponent } from 'app/ng2/pages/composition/panel/panel-tabs/info-tab/info-tab.component';
import { PolicyTargetsTabComponent } from 'app/ng2/pages/composition/panel/panel-tabs/policy-targets-tab/policy-targets-tab.component';
import { PropertiesTabComponent } from 'app/ng2/pages/composition/panel/panel-tabs/properties-tab/properties-tab.component';
import { ReqAndCapabilitiesTabComponent } from 'app/ng2/pages/composition/panel/panel-tabs/req-capabilities-tab/req-capabilities-tab.component';
import { ComponentType, ResourceType } from 'app/utils';
import * as _ from 'lodash';
import { Subscription } from 'rxjs';
import { Observable } from 'rxjs/Observable';
import { ArtifactGroupType, COMPONENT_FIELDS } from '../../../../utils/constants';
import { WorkspaceState } from '../../../store/states/workspace.state';
import { OnSidebarOpenOrCloseAction } from '../common/store/graph.actions';
import { CompositionStateModel, GraphState } from '../common/store/graph.state';
import { ServiceConsumptionTabComponent } from './panel-tabs/service-consumption-tab/service-consumption-tab.component';
import { ServiceDependenciesTabComponent } from './panel-tabs/service-dependencies-tab/service-dependencies-tab.component';

const tabs = {
    infoTab : {titleIcon: 'info-circle', component: InfoTabComponent, input: {}, isActive: true, tooltipText: 'Information'},
    policyProperties: {titleIcon: 'settings-o', component: GroupOrPolicyPropertiesTab, input: {type: 'policy'}, isActive: false, tooltipText: 'Properties'},
    policyTargets: {titleIcon: 'inputs-o', component: PolicyTargetsTabComponent, input: {}, isActive: false, tooltipText: 'Targets'},
    groupMembers: {titleIcon: 'inputs-o', component: GroupMembersTabComponent, input: {}, isActive: false, tooltipText: 'Members'},
    groupProperties: {titleIcon: 'settings-o', component: GroupOrPolicyPropertiesTab, input: {type: 'group'}, isActive: false, tooltipText: 'Properties'},
    deploymentArtifacts: {titleIcon: 'deployment-artifacts-o', component: ArtifactsTabComponent, input: { type: ArtifactGroupType.DEPLOYMENT}, isActive: false, tooltipText: 'Deployment Artifacts'},
    apiArtifacts: {titleIcon: 'api-o', component: ArtifactsTabComponent, input: { type:  ArtifactGroupType.SERVICE_API}, isActive: false, tooltipText: 'API Artifacts'},
    infoArtifacts: {titleIcon: 'info-square-o', component: ArtifactsTabComponent, input: { type: ArtifactGroupType.INFORMATION}, isActive: false, tooltipText: 'Information Artifacts'},
    properties: {titleIcon: 'settings-o', component: PropertiesTabComponent, input: {title: 'Properties and Attributes'}, isActive: false, tooltipText: 'Properties'},
    reqAndCapabilities : { titleIcon: 'req-capabilities-o', component: ReqAndCapabilitiesTabComponent, input: {}, isActive: false, tooltipText: 'Requirements and Capabilities'},
    inputs: {titleIcon: 'inputs-o', component: PropertiesTabComponent, input: {title: 'Inputs'}, isActive: false, tooltipText: 'Inputs'},
    settings: {titleIcon: 'settings-o', component: PropertiesTabComponent, input: {}, isActive: false, tooltipText: 'Settings'},
    consumption: {titleIcon: 'api-o', component: ServiceConsumptionTabComponent, input: {title: 'OPERATION CONSUMPTION'}, isActive: false, tooltipText: 'Service Consumption'},
    dependencies: {titleIcon: 'archive', component: ServiceDependenciesTabComponent, input: {title: 'SERVICE DEPENDENCIES'}, isActive: false, tooltipText: 'Service Dependencies'}
};

@Component({
    selector: 'ng2-composition-panel',
    templateUrl: './composition-panel.component.html',
    styleUrls: ['./composition-panel.component.less', './panel-tabs/panel-tabs.less'],
})
export class CompositionPanelComponent {

    @Input() topologyTemplate: TopologyTemplate;
    @HostBinding('class') classes = 'component-details-panel';
    @Select(GraphState) compositionState$: Observable<CompositionStateModel>;
    @Select(GraphState.withSidebar) withSidebar$: boolean;
    @Select(WorkspaceState.isViewOnly) isViewOnly$: boolean;
    tabs: any[];
    subscription: Subscription;

    private selectedComponent;

    constructor(public store: Store) {
    }

    ngOnInit() {
        this.subscription = this.store.select(GraphState.getSelectedComponent).subscribe((component) => {
            this.selectedComponent = component;
            this.initTabs(component);
            this.activatePreviousActiveTab();
        });
    }

    ngOnDestroy() {
        if (this.subscription) {
            this.subscription.unsubscribe();
        }
    }

    public setActive = (tabToSelect) => {
        this.tabs.map((tab) => tab.isActive = (tab.titleIcon === tabToSelect.titleIcon) ? true : false);
    }

    public activatePreviousActiveTab = () => { // sets the info tab to active if no other tab selected

        this.setActive(this.tabs.find((tab) => tab.isActive) || tabs.infoTab);

    }

    private initTabs = (component) => {
        this.tabs = [];

        // Information
        this.tabs.push(tabs.infoTab);

        if (component instanceof PolicyInstance) {
            this.tabs.push(tabs.policyTargets);
            this.tabs.push(tabs.policyProperties);
            return;
        }

        if (component instanceof GroupInstance) {
            this.tabs.push(tabs.groupMembers);
            this.tabs.push(tabs.groupProperties);
            return;
        }

        // Deployment artifacts
        if (!this.isPNF() && !this.isConfiguration() && !this.selectedComponentIsServiceProxyInstance()) {
            this.tabs.push(tabs.deploymentArtifacts);
        }

        // Properties or Inputs
        if (component.isResource() || this.selectedComponentIsServiceProxyInstance()) {
            this.tabs.push(tabs.properties);
        } else {
            this.tabs.push(tabs.inputs);
        }

        if (!this.isConfiguration() && !this.selectedComponentIsServiceProxyInstance()) {
            this.tabs.push(tabs.infoArtifacts);
        }

        if (!(component.isService()) || this.selectedComponentIsServiceProxyInstance()) {
            this.tabs.push(tabs.reqAndCapabilities);
        }

        if (component.isService() && !this.selectedComponentIsServiceProxyInstance()) {
            this.tabs.push(tabs.apiArtifacts);
        }
        if (component.isService() && this.selectedComponentIsServiceProxyInstance()) {
            this.tabs.push(tabs.consumption);
            this.tabs.push(tabs.dependencies);
        }

    }

    private toggleSidebarDisplay = () => {
        // this.withSidebar = !this.withSidebar;
        this.store.dispatch(new OnSidebarOpenOrCloseAction());
    }

    private isPNF = (): boolean => {
        return this.topologyTemplate.isResource() && (this.topologyTemplate as Resource).resourceType === ResourceType.PNF;
    }

    private isConfiguration = (): boolean => {
        return this.topologyTemplate.isResource() && (this.topologyTemplate as Resource).resourceType === ResourceType.CONFIGURATION;
    }

    private isComponentInstanceSelected = (): boolean => {
        return this.selectedComponent instanceof FullComponentInstance;
    }

    private selectedComponentIsServiceProxyInstance = (): boolean => {
        return this.isComponentInstanceSelected() && this.selectedComponent.isServiceProxy();
    }
}
