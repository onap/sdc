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
import {CompositionGraphGeneralUtils} from "./composition-graph-general-utils";
import {ServiceServiceNg2} from 'app/ng2/services/component-services/service.service';
import {Service} from "app/models/components/service";
import {ForwardingPath} from "app/models/forwarding-path";
import {ForwardingPathLink} from "app/models/forwarding-path-link";
import {ComponentRef, Injectable} from "@angular/core";
import {CompositionCiServicePathLink} from "app/models/graph/graph-links/composition-graph-links/composition-ci-service-path-link";
import {SdcUiServices} from "onap-ui-angular";
import {QueueServiceUtils} from "app/ng2/utils/queue-service-utils";
import {ServicePathsListComponent} from "app/ng2/pages/composition/graph/service-paths-list/service-paths-list.component";
import {ButtonModel, ModalModel} from "app/models";
import {ServicePathCreatorComponent} from "app/ng2/pages/composition/graph/service-path-creator/service-path-creator.component";
import {ModalService} from "app/ng2/services/modal.service";
import {ModalComponent} from "app/ng2/components/ui/modal/modal.component";
import {Select, Store} from "@ngxs/store";
import {WorkspaceState} from "app/ng2/store/states/workspace.state";
import {WorkspaceService} from "app/ng2/pages/workspace/workspace.service";
import {CompositionService} from "../../composition.service";
import {CommonGraphUtils} from "../common/common-graph-utils";
import {GRAPH_EVENTS} from "app/utils/constants";
import {EventListenerService} from "app/services/event-listener-service";

@Injectable()
export class ServicePathGraphUtils {

    constructor(
        private generalGraphUtils: CompositionGraphGeneralUtils,
        private serviceService: ServiceServiceNg2,
        private commonGraphUtils: CommonGraphUtils,
        private loaderService: SdcUiServices.LoaderService,
        private queueServiceUtils: QueueServiceUtils,
        private modalService: ModalService,
        private workspaceService: WorkspaceService,
        private compositionService: CompositionService,
        private store:Store,
        private eventListenerService: EventListenerService
    ) {
    }

    private isViewOnly = (): boolean => {
        return this.store.selectSnapshot(state => state.workspace.isViewOnly);
    }
    private modalInstance: ComponentRef<ModalComponent>;

    public deletePathsFromGraph(cy: Cy.Instance) {
        cy.remove(`[type="${CompositionCiServicePathLink.LINK_TYPE}"]`);
    }

    public drawPath(cy: Cy.Instance, forwardingPath: ForwardingPath) {
        let pathElements = forwardingPath.pathElements.listToscaDataDefinition;

        _.forEach(pathElements, (link: ForwardingPathLink) => {
            let data: CompositionCiServicePathLink = new CompositionCiServicePathLink(link);
            data.source = _.find(
                this.compositionService.componentInstances,
                instance => instance.name === data.forwardingPathLink.fromNode
            ).uniqueId;
            data.target = _.find(
                this.compositionService.componentInstances,
                instance => instance.name === data.forwardingPathLink.toNode
            ).uniqueId;
            data.pathId = forwardingPath.uniqueId;
            data.pathName = forwardingPath.name;
            this.commonGraphUtils.insertServicePathLinkToGraph(cy, data);
        });
    }

    public createOrUpdateServicePath = (path: any): void => {
        this.loaderService.activate();

        let onSuccess: (response: ForwardingPath) => void = (response: ForwardingPath) => {
            this.loaderService.deactivate();
            this.compositionService.forwardingPaths[response.uniqueId] = response;
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_SERVICE_PATH_CREATED, response.uniqueId)
        };

        this.queueServiceUtils.addBlockingUIAction(
            () => this.serviceService.createOrUpdateServicePath(this.workspaceService.metadata.uniqueId, path).subscribe(onSuccess
            , (error) => {this.loaderService.deactivate()})
        );
    };

    public onCreateServicePath = (): void => {
        // this.showServicePathMenu = false;
        let cancelButton: ButtonModel = new ButtonModel('Cancel', 'outline white', this.modalService.closeCurrentModal);
        let saveButton: ButtonModel = new ButtonModel('Create', 'blue', this.createPath, this.getDisabled);
        let modalModel: ModalModel = new ModalModel('l', 'Create Service Flow', '', [saveButton, cancelButton], 'standard', true);
        this.modalInstance = this.modalService.createCustomModal(modalModel);
        this.modalService.addDynamicContentToModal(this.modalInstance, ServicePathCreatorComponent, {serviceId: this.workspaceService.metadata.uniqueId});
        this.modalInstance.instance.open();
    };

    public onListServicePath = (): void => {
        // this.showServicePathMenu = false;
        let cancelButton: ButtonModel = new ButtonModel('Close', 'outline white', this.modalService.closeCurrentModal);
        let modalModel: ModalModel = new ModalModel('md', 'Service Flows List', '', [cancelButton], 'standard', true);
        this.modalInstance = this.modalService.createCustomModal(modalModel);
        this.modalService.addDynamicContentToModal(this.modalInstance, ServicePathsListComponent, {
            serviceId: this.workspaceService.metadata.uniqueId,
            onCreateServicePath: this.onCreateServicePath,
            onEditServicePath: this.onEditServicePath,
            isViewOnly: this.isViewOnly()
        });
        this.modalInstance.instance.open();
    };

    public onEditServicePath = (id: string): void => {
        let cancelButton: ButtonModel = new ButtonModel('Cancel', 'outline white', this.modalService.closeCurrentModal);
        let saveButton: ButtonModel = new ButtonModel('Save', 'blue', this.createPath, this.getDisabled);
        let modalModel: ModalModel = new ModalModel('l', 'Edit Path', '', [saveButton, cancelButton], 'standard', true);
        this.modalInstance = this.modalService.createCustomModal(modalModel);
        this.modalService.addDynamicContentToModal(this.modalInstance, ServicePathCreatorComponent, {
            serviceId: this.workspaceService.metadata.uniqueId,
            pathId: id
        });
        this.modalInstance.instance.open();
    };

    public getDisabled = (): boolean => {
        return this.isViewOnly() || !this.modalInstance.instance.dynamicContent.instance.checkFormValidForSubmit();
    };

    public createPath = (): void => {
        this.createOrUpdateServicePath(this.modalInstance.instance.dynamicContent.instance.createServicePathData());
        this.modalService.closeCurrentModal();
    };
}
