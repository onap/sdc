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

import { Component, Module, NodesFactory, ComponentInstance } from "app/models";
import { ComponentInstanceFactory } from "app/utils";
import { DeploymentGraphGeneralUtils } from "./deployment-utils/deployment-graph-general-utils";
import { CommonGraphUtils } from "../common/common-graph-utils";
import { ComponentInstanceNodesStyle } from "../common/style/component-instances-nodes-style";
import { ModulesNodesStyle } from "../common/style/module-node-style";
import { GRAPH_EVENTS } from "app/utils";
import { EventListenerService } from "app/services";
import '@bardit/cytoscape-expand-collapse';
import {AngularJSBridge} from "../../../services/angular-js-bridge-service";

interface IDeploymentGraphScope extends ng.IScope {
    component: Component;
}

export class DeploymentGraph implements ng.IDirective {
    private _cy: Cy.Instance;

    constructor(private NodesFactory: NodesFactory,
        private commonGraphUtils: CommonGraphUtils,
        private deploymentGraphGeneralUtils: DeploymentGraphGeneralUtils,
        private ComponentInstanceFactory: ComponentInstanceFactory,
        private eventListenerService: EventListenerService) {
    }

    restrict = 'E';
    template = require('./deployment-graph.html');
    scope = {
        component: '=',
        isViewOnly: '='
    };

    link = (scope: IDeploymentGraphScope, el: JQuery) => {

        if (scope.component.isResource()) {
            if (scope.component.componentInstances && scope.component.componentInstancesRelations && scope.component.groups) {
                this.loadGraph(scope, el);
            } else {
                this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_DEPLOYMENT_GRAPH_DATA_LOADED, () => {
                    this.loadGraph(scope, el);
                });
            }
        }
    };

    public initGraphNodes = (cy: Cy.Instance, component: Component): void => {
        if (component.groups) { // Init module nodes
            _.each(component.groups, (groupModule: Module) => {
                let moduleNode = this.NodesFactory.createModuleNode(groupModule);
                this.commonGraphUtils.addNodeToGraph(cy, moduleNode);

            });
        }
        _.each(component.componentInstances, (instance: ComponentInstance) => { // Init component instance nodes
            let componentInstanceNode = this.NodesFactory.createNode(instance);
            componentInstanceNode.parent = this.deploymentGraphGeneralUtils.findInstanceModule(component.groups, instance.uniqueId);
            if (componentInstanceNode.parent) { // we are not drawing instances that are not a part of a module
                this.commonGraphUtils.addComponentInstanceNodeToGraph(cy, componentInstanceNode);
            }
        });

        // This is a special functionality to pass the cytoscape default behavior - we can't create Parent module node without children's
        // so we must add an empty dummy child node
        _.each(this._cy.nodes('[?isGroup]'), (moduleNode: Cy.CollectionFirstNode) => {
            if (!moduleNode.isParent()) {
                let dummyInstance = this.ComponentInstanceFactory.createEmptyComponentInstance();
                let componentInstanceNode = this.NodesFactory.createNode(dummyInstance);
                componentInstanceNode.parent = moduleNode.id();
                let dummyNode = this.commonGraphUtils.addNodeToGraph(cy, componentInstanceNode, moduleNode.position());
                dummyNode.addClass('dummy-node');
            }
        })
    };

    private registerGraphEvents() {

        this._cy.on('afterExpand', (event) => {
            event.cyTarget.qtip({});
        });

        this._cy.on('afterCollapse', (event) => {
            this.commonGraphUtils.initNodeTooltip(event.cyTarget);
        });
    }

    private loadGraph = (scope: IDeploymentGraphScope, el: JQuery) => {

        let graphEl = el.find('.sdc-deployment-graph-wrapper');
        const imagePath = AngularJSBridge.getAngularConfig().imagesPath;
        this._cy = cytoscape({
            container: graphEl,
            style: ComponentInstanceNodesStyle.getCompositionGraphStyle().concat(ModulesNodesStyle.getModuleGraphStyle()),
            zoomingEnabled: false,
            selectionType: 'single',

        });

        //adding expand collapse extension
        this._cy.expandCollapse({
            layoutBy: {
                name: "grid",
                animate: true,
                randomize: false,
                fit: true
            },
            fisheye: false,
            undoable: false,
            expandCollapseCueSize: 18,
            expandCueImage: imagePath + '/assets/styles/images/resource-icons/' + 'closeModule.png',
            collapseCueImage: imagePath + '/assets/styles/images/resource-icons/' + 'openModule.png',
            expandCollapseCueSensitivity: 2,
            cueOffset: -20
        });

        this.initGraphNodes(this._cy, scope.component); //creating instances nodes
        this.commonGraphUtils.initGraphLinks(this._cy, scope.component.componentInstancesRelations);
        this._cy.collapseAll();
        this.registerGraphEvents();

        scope.$on('$destroy', () => {
            this._cy.destroy();
            _.forEach(GRAPH_EVENTS, (event) => {
                this.eventListenerService.unRegisterObserver(event);
            });
        });

    };

    public static factory = (NodesFactory: NodesFactory, CommonGraphUtils: CommonGraphUtils, DeploymentGraphGeneralUtils: DeploymentGraphGeneralUtils, ComponentInstanceFactory: ComponentInstanceFactory, EventListenerService: EventListenerService) => {
        return new DeploymentGraph(NodesFactory, CommonGraphUtils, DeploymentGraphGeneralUtils, ComponentInstanceFactory, EventListenerService)
    }
}

DeploymentGraph.factory.$inject = ['NodesFactory', 'CommonGraphUtils', 'DeploymentGraphGeneralUtils', 'ComponentInstanceFactory', 'EventListenerService'];
