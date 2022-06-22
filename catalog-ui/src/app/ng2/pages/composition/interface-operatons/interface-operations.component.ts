/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
*  Copyright (C) 2021 Nordix Foundation. All rights reserved.
*  ================================================================================
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*  SPDX-License-Identifier: Apache-2.0
*  ============LICENSE_END=========================================================
*/

import {Component, ComponentRef, Inject, Input} from '@angular/core';
import {
    TopologyTemplateService
} from '../../../services/component-services/topology-template.service';
import {TranslateService} from "../../../shared/translator/translate.service";
import {ModalService} from 'app/ng2/services/modal.service';
import {ModalComponent} from 'app/ng2/components/ui/modal/modal.component';
import {Component as TopologyTemplate} from "../../../../models/components/component";
import {PluginsService} from "app/ng2/services/plugins.service";
import {SelectedComponentType} from "../common/store/graph.actions";

import {WorkspaceService} from "../../workspace/workspace.service";
import {
    ComponentInterfaceDefinitionModel,
    InterfaceOperationModel
} from "../../../../models/interfaceOperation";
import {
    InterfaceOperationHandlerComponent
} from "./operation-creator/interface-operation-handler.component";

import {
    ArtifactModel,
    ButtonModel,
    ComponentInstance,
    ComponentMetadata,
    InputBEModel,
    InterfaceModel,
    ModalModel
} from 'app/models';
import {ArtifactGroupType} from "../../../../utils/constants";
import {
    DropdownValue
} from "../../../components/ui/form-components/dropdown/ui-element-dropdown.component";
import {ToscaArtifactService} from "../../../services/tosca-artifact.service";
import {ToscaArtifactModel} from "../../../../models/toscaArtifact";

export class UIInterfaceOperationModel extends InterfaceOperationModel {
    isCollapsed: boolean = true;
    isEllipsis: boolean;
    MAX_LENGTH = 75;

    constructor(operation: InterfaceOperationModel) {
        super(operation);

        if (!operation.description) {
            this.description = '';
        }

        if (this.description.length > this.MAX_LENGTH) {
            this.isEllipsis = true;
        } else {
            this.isEllipsis = false;
        }
    }

    getDescriptionEllipsis(): string {
        if (this.isCollapsed && this.description.length > this.MAX_LENGTH) {
            return this.description.substr(0, this.MAX_LENGTH - 3) + '...';
        }
        return this.description;
    }

    toggleCollapsed(e) {
        e.stopPropagation();
        this.isCollapsed = !this.isCollapsed;
    }
}

class ModalTranslation {
    EDIT_TITLE: string;
    CANCEL_BUTTON: string;
    CLOSE_BUTTON: string;
    SAVE_BUTTON: string;

    constructor(private translateService: TranslateService) {
        this.translateService.languageChangedObservable.subscribe(lang => {
            this.EDIT_TITLE = this.translateService.translate('INTERFACE_EDIT_TITLE');
            this.CANCEL_BUTTON = this.translateService.translate("INTERFACE_CANCEL_BUTTON");
            this.CLOSE_BUTTON = this.translateService.translate("INTERFACE_CLOSE_BUTTON");
            this.SAVE_BUTTON = this.translateService.translate("INTERFACE_SAVE_BUTTON");
        });
    }
}

export class UIInterfaceModel extends ComponentInterfaceDefinitionModel {
    isCollapsed: boolean = false;

    constructor(interf?: any) {
        super(interf);
        this.operations = _.map(
            this.operations,
            (operation) => new UIInterfaceOperationModel(operation)
        );
    }

    toggleCollapse() {
        this.isCollapsed = !this.isCollapsed;
    }
}

@Component({
    selector: 'app-interface-operations',
    templateUrl: './interface-operations.component.html',
    styleUrls: ['./interface-operations.component.less'],
    providers: [ModalService, TranslateService]
})
export class InterfaceOperationsComponent {
    interfaces: UIInterfaceModel[];
    inputs: Array<InputBEModel>;
    isLoading: boolean;
    interfaceTypes: { [interfaceType: string]: string[] };
    topologyTemplate: TopologyTemplate;
    componentMetaData: ComponentMetadata;
    componentInstanceSelected: ComponentInstance;
    modalInstance: ComponentRef<ModalComponent>;
    modalTranslation: ModalTranslation;
    componentInstancesInterfaces: Map<string, InterfaceModel[]>;

    deploymentArtifactsFilePath: Array<DropdownValue> = [];
    toscaArtifactTypes: Array<DropdownValue> = [];

    @Input() component: ComponentInstance;
    @Input() isViewOnly: boolean;
    @Input() enableMenuItems: Function;
    @Input() disableMenuItems: Function;
    @Input() componentType: SelectedComponentType;


    constructor(
        private translateService: TranslateService,
        private pluginsService: PluginsService,
        private topologyTemplateService: TopologyTemplateService,
        private toscaArtifactService: ToscaArtifactService,
        private modalServiceNg2: ModalService,
        private workspaceService: WorkspaceService,
        @Inject("Notification") private Notification: any,
    ) {
        this.modalTranslation = new ModalTranslation(translateService);
    }

    ngOnInit(): void {
        this.componentMetaData = this.workspaceService.metadata;
        this.loadComponentInstances();
        this.loadDeployedArtifacts();
        this.loadToscaArtifacts()
    }

    private loadComponentInstances() {
        this.isLoading = true;
        this.topologyTemplateService.getComponentInstances(this.componentMetaData.componentType, this.componentMetaData.uniqueId)
        .subscribe((response) => {
            this.componentInstanceSelected = response.componentInstances.find(ci => ci.uniqueId === this.component.uniqueId);
            this.initComponentInstanceInterfaceOperations();
            this.isLoading = false;
        });
    }

    private initComponentInstanceInterfaceOperations() {
        this.initInterfaces(this.componentInstanceSelected.interfaces);
        this.sortInterfaces();
    }

    private initInterfaces(interfaces: ComponentInterfaceDefinitionModel[]): void {
        this.interfaces = _.map(interfaces, (interfaceModel) => new UIInterfaceModel(interfaceModel));
    }

    private sortInterfaces(): void {
        this.interfaces = _.filter(this.interfaces, (interf) => interf.operations && interf.operations.length > 0); // remove empty interfaces
        this.interfaces.sort((a, b) => a.type.localeCompare(b.type)); // sort interfaces alphabetically
        _.forEach(this.interfaces, (interf) => {
            interf.operations.sort((a, b) => a.name.localeCompare(b.name)); // sort operations alphabetically
        });
    }

    collapseAll(value: boolean = true): void {
        _.forEach(this.interfaces, (interf) => {
            interf.isCollapsed = value;
        });
    }

    isAllCollapsed(): boolean {
        return _.every(this.interfaces, (interf) => interf.isCollapsed);
    }

    isAllExpanded(): boolean {
        return _.every(this.interfaces, (interf) => !interf.isCollapsed);
    }

    isListEmpty(): boolean {
        return _.filter(
            this.interfaces,
            (interf) => interf.operations && interf.operations.length > 0
        ).length === 0;
    }

    private disableSaveButton = (): boolean => {
        let disable:boolean = true;
        if(this.isViewOnly) {
            return disable;
        }

        let enableAddArtifactImplementation = this.modalInstance.instance.dynamicContent.instance.enableAddArtifactImplementation;
        if(enableAddArtifactImplementation) {
            let toscaArtifactTypeSelected = this.modalInstance.instance.dynamicContent.instance.toscaArtifactTypeSelected;
            let isToscaArtifactType:boolean = !(typeof toscaArtifactTypeSelected == 'undefined' || _.isEmpty(toscaArtifactTypeSelected));
            disable = !isToscaArtifactType;
            return disable;
        }
        disable = false;
        return disable;
    }

    onSelectInterfaceOperation(interfaceModel: UIInterfaceModel, operation: InterfaceOperationModel) {

        const buttonList = [];
        if (this.isViewOnly) {
            const closeButton: ButtonModel = new ButtonModel(this.modalTranslation.CLOSE_BUTTON, 'outline white', this.cancelAndCloseModal);
            buttonList.push(closeButton);
        } else {
            const saveButton: ButtonModel = new ButtonModel(this.modalTranslation.SAVE_BUTTON, 'blue', () =>
                this.updateInterfaceOperation(), this.disableSaveButton);
            const cancelButton: ButtonModel = new ButtonModel(this.modalTranslation.CANCEL_BUTTON, 'outline white', this.cancelAndCloseModal);
            buttonList.push(saveButton);
            buttonList.push(cancelButton);
        }
        const modalModel: ModalModel = new ModalModel('l', this.modalTranslation.EDIT_TITLE, '', buttonList, 'custom');
        this.modalInstance = this.modalServiceNg2.createCustomModal(modalModel);

        this.modalServiceNg2.addDynamicContentToModal(
            this.modalInstance,
            InterfaceOperationHandlerComponent,
            {
                deploymentArtifactsFilePath: this.deploymentArtifactsFilePath,
                toscaArtifactTypes: this.toscaArtifactTypes,
                selectedInterface: interfaceModel ? interfaceModel : new UIInterfaceModel(),
                selectedInterfaceOperation: operation ? operation : new InterfaceOperationModel(),
                validityChangedCallback: this.disableSaveButton,
                isViewOnly: this.isViewOnly,
                isEdit: true,
                modelName: this.componentMetaData.model
            }
        );
        this.modalInstance.instance.open();
    }

    private cancelAndCloseModal = () => {
        this.loadComponentInstances();
        return this.modalServiceNg2.closeCurrentModal();
    }

    private updateInterfaceOperation() {
        this.modalServiceNg2.currentModal.instance.dynamicContent.instance.isLoading = true;
        const interfaceOperationHandlerComponentInstance: InterfaceOperationHandlerComponent = this.modalInstance.instance.dynamicContent.instance;
        const operationUpdated: InterfaceOperationModel = interfaceOperationHandlerComponentInstance.operationToUpdate;
        const isArtifactChecked = interfaceOperationHandlerComponentInstance.enableAddArtifactImplementation;
        if (!isArtifactChecked) {
            let artifactName = interfaceOperationHandlerComponentInstance.artifactName;
            artifactName = artifactName === undefined ? '' : artifactName;
            operationUpdated.implementation = new ArtifactModel({'artifactName': artifactName, 'artifactVersion': ''} as ArtifactModel);
        }
        this.topologyTemplateService.updateComponentInstanceInterfaceOperation(
            this.componentMetaData.uniqueId,
            this.componentMetaData.componentType,
            this.componentInstanceSelected.uniqueId,
            operationUpdated)
        .subscribe((updatedComponentInstance: ComponentInstance) => {
            this.componentInstanceSelected = new ComponentInstance(updatedComponentInstance);
            this.initComponentInstanceInterfaceOperations();
            this.modalServiceNg2.currentModal.instance.dynamicContent.instance.isLoading = false;
            this.modalServiceNg2.closeCurrentModal();
        }, () => {
            this.modalServiceNg2.currentModal.instance.dynamicContent.instance.isLoading = false;
            this.modalServiceNg2.closeCurrentModal();
        });
    }

    loadDeployedArtifacts() {
        this.topologyTemplateService.getArtifactsByType(this.componentMetaData.componentType, this.componentMetaData.uniqueId, ArtifactGroupType.DEPLOYMENT)
        .subscribe(response => {
            let artifactsDeployment = response.deploymentArtifacts;
            if (artifactsDeployment) {
                let deploymentArtifactsFound = <ArtifactModel[]>_.values(artifactsDeployment)
                deploymentArtifactsFound.forEach(value => {
                    this.deploymentArtifactsFilePath.push(new DropdownValue(value, value.artifactType.concat('->').concat(value.artifactName)));
                });
            }
        }, error => {
            this.Notification.error({
                message: 'Failed to Load the Deployed Artifacts:' + error,
                title: 'Failure'
            });
        });
    }

    loadToscaArtifacts() {
        this.toscaArtifactService.getToscaArtifacts(this.componentMetaData.model).subscribe(response => {
            if (response) {
                let toscaArtifactsFound = <ToscaArtifactModel[]>_.values(response);
                toscaArtifactsFound.forEach(value => this.toscaArtifactTypes.push(new DropdownValue(value, value.type)));
            }
        }, error => {
            this.Notification.error({
                message: 'Failed to Load Tosca Artifacts:' + error,
                title: 'Failure'
            });
        });
    }

}
