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

import { Injectable } from '@angular/core';
import { Component as TopologyTemplate } from 'app/models';
import {
    ComponentInstance,
    CompositionCiNodeVl, Service
} from 'app/models';
import { CompositionCiServicePathLink } from 'app/models/graph/graph-links/composition-graph-links/composition-ci-service-path-link';
import { WorkspaceService } from 'app/ng2/pages/workspace/workspace.service';
import { ServiceServiceNg2 } from 'app/ng2/services/component-services/service.service';
import { TopologyTemplateService } from 'app/ng2/services/component-services/topology-template.service';
import { ServiceGenericResponse } from 'app/ng2/services/responses/service-generic-response';
import { QueueServiceUtils } from 'app/ng2/utils/queue-service-utils';
import { EventListenerService } from 'app/services';
import { GRAPH_EVENTS } from 'app/utils';
import * as _ from 'lodash';
import { SdcUiServices } from 'onap-ui-angular';
import { CompositionService } from '../../composition.service';
import { CommonGraphUtils } from '../common/common-graph-utils';
import { CompositionGraphGeneralUtils } from './composition-graph-general-utils';

/**
 * Created by obarda on 11/9/2016.
 */
@Injectable()
export class CompositionGraphNodesUtils {
    constructor(private generalGraphUtils: CompositionGraphGeneralUtils,
                private commonGraphUtils: CommonGraphUtils,
                private eventListenerService: EventListenerService,
                private queueServiceUtils: QueueServiceUtils,
                private serviceService: ServiceServiceNg2,
                private loaderService: SdcUiServices.LoaderService,
                private compositionService: CompositionService,
                private topologyTemplateService: TopologyTemplateService,
                private workspaceService: WorkspaceService) {
    }

    /**
     * Returns component instances for all nodes passed in
     * @param nodes - Cy nodes
     * @returns {any[]}
     */
    public getAllNodesData(nodes: Cy.CollectionNodes) {
        return _.map(nodes, (node: Cy.CollectionFirstNode) => {
            return node.data();
        });
    }

    public highlightMatchingNodesByName = (cy: Cy.Instance, nameToMatch: string) => {

        cy.batch(() => {
            cy.nodes("[name !@^= '" + nameToMatch + "']").style({'background-image-opacity': 0.4});
            cy.nodes("[name @^= '" + nameToMatch + "']").style({'background-image-opacity': 1});
        });

    }

    // Returns all nodes whose name starts with searchTerm
    public getMatchingNodesByName = (cy: Cy.Instance, nameToMatch: string): Cy.CollectionNodes => {
        return cy.nodes("[name @^= '" + nameToMatch + "']");
    }

    /**
     * Deletes component instances on server and then removes it from the graph as well
     * @param cy
     * @param component
     * @param nodeToDelete
     */
    public deleteNode(cy: Cy.Instance, component: TopologyTemplate, nodeToDelete: Cy.CollectionNodes): void {

        this.loaderService.activate();
        const onSuccess: (response: ComponentInstance) => void = (response: ComponentInstance) => {
            // check whether the node is connected to any VLs that only have one other connection. If so, delete that VL as well
            this.loaderService.deactivate();
            this.compositionService.deleteComponentInstance(response.uniqueId);

            const nodeToDeleteIsNotVl = nodeToDelete.data().componentInstance && !(nodeToDelete.data().componentInstance.isVl());
            if (nodeToDeleteIsNotVl) {
                const connectedVls: Cy.CollectionFirstNode[] = this.getConnectedVlToNode(nodeToDelete);
                this.handleConnectedVlsToDelete(connectedVls);
            }

            // check whether there is a service path going through this node, and if so clean it from the graph.
            const nodeId = nodeToDelete.data().id;
            const connectedPathLinks = cy.collection(`[type="${CompositionCiServicePathLink.LINK_TYPE}"][source="${nodeId}"], [type="${CompositionCiServicePathLink.LINK_TYPE}"][target="${nodeId}"]`);
            _.forEach(connectedPathLinks, (link, key) => {
                cy.remove(`[pathId="${link.data().pathId}"]`);
            });

            // update service path list
            this.serviceService.getComponentCompositionData(component).subscribe((serviceResponse: ServiceGenericResponse) => {
                (component as Service).forwardingPaths = serviceResponse.forwardingPaths;
            });

            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE_SUCCESS, nodeId);

            // update UI
            cy.remove(nodeToDelete);
        };

        const onFailed: (response: any) => void = (response: any) => {
            this.loaderService.deactivate();
        };

        this.queueServiceUtils.addBlockingUIAction(
            () => {
                const uniqueId = this.workspaceService.metadata.uniqueId;
                const componentType = this.workspaceService.metadata.componentType;
                this.topologyTemplateService.deleteComponentInstance(componentType, uniqueId, nodeToDelete.data().componentInstance.uniqueId).subscribe(onSuccess, onFailed);
            }
        );
    }

    /**
     * Finds all VLs connected to a single node
     * @param node
     * @returns {Array<Cy.CollectionFirstNode>}
     */
    public getConnectedVlToNode = (node: Cy.CollectionNodes): Cy.CollectionFirstNode[] => {
        const connectedVls: Cy.CollectionFirstNode[] = new Array<Cy.CollectionFirstNode>();
        _.forEach(node.connectedEdges().connectedNodes(), (connectedNode: Cy.CollectionFirstNode) => {
            const connectedNodeIsVl = connectedNode.data().componentInstance.isVl();
            if (connectedNodeIsVl) {
                connectedVls.push(connectedNode);
            }
        });
        return connectedVls;
    }

    /**
     * Delete all VLs that have only two connected nodes (this function is called when deleting a node)
     * @param connectedVls
     */
    public handleConnectedVlsToDelete = (connectedVls: Cy.CollectionFirstNode[]) => {
        _.forEach(connectedVls, (vlToDelete: Cy.CollectionNodes) => {

            if (vlToDelete.connectedEdges().length === 2) { // if vl connected only to 2 nodes need to delete the vl
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE, vlToDelete.data().componentInstance.uniqueId);
            }
        });
    }

    /**
     * This function will update nodes position.
     * @param cy
     * @param component
     * @param nodesMoved - the node/multiple nodes now moved by the user
     */
    public onNodesPositionChanged = (cy: Cy.Instance, component: TopologyTemplate, nodesMoved: Cy.CollectionNodes): void => {

        if (nodesMoved.length === 0) {
            return;
        }

        const isValidMove: boolean = this.generalGraphUtils.isGroupValidDrop(cy, nodesMoved);
        if (isValidMove) {

            const instancesToUpdate: ComponentInstance[] = new Array<ComponentInstance>();

            _.each(nodesMoved, (node: Cy.CollectionFirstNode) => {  // update all nodes new position

                // update position
                const newPosition: Cy.Position = this.commonGraphUtils.getNodePosition(node);
                node.data().componentInstance.updatePosition(newPosition.x, newPosition.y);
                instancesToUpdate.push(node.data().componentInstance);

            });

            if (instancesToUpdate.length > 0) {
                this.generalGraphUtils.pushMultipleUpdateComponentInstancesRequestToQueue(instancesToUpdate);
            }
        } else {
            // reset nodes position
            nodesMoved.positions((i, node) => {
                return {
                    x: +node.data().componentInstance.posX,
                    y: +node.data().componentInstance.posY
                };
            });
        }
    }

}
