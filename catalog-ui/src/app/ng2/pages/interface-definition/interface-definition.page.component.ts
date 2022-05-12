/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
*  Copyright (C) 2022 Nordix Foundation. All rights reserved.
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
import {Component as IComponent} from 'app/models/components/component';
import {WorkflowServiceNg2} from 'app/ng2/services/workflow.service';

import {ISdcConfig, SdcConfigToken} from "app/ng2/config/sdc-config.config";
import {TranslateService} from "app/ng2/shared/translator/translate.service";
import {IModalButtonComponent, SdcUiServices} from 'onap-ui-angular';
import {ModalComponent} from 'app/ng2/components/ui/modal/modal.component';

import {ModalService} from 'app/ng2/services/modal.service';
import {
    ButtonModel,
    CapabilitiesGroup,
    InputBEModel,
    InterfaceModel,
    ModalModel,
    OperationModel,
    WORKFLOW_ASSOCIATION_OPTIONS
} from 'app/models';

import {ComponentServiceNg2} from 'app/ng2/services/component-services/component.service';
import {TopologyTemplateService} from "../../services/component-services/topology-template.service";
import {InterfaceOperationModel} from "../../../models/interfaceOperation";
import {
    InterfaceOperationHandlerComponent
} from "../composition/interface-operatons/operation-creator/interface-operation-handler.component";
import {
    DropdownValue
} from "../../components/ui/form-components/dropdown/ui-element-dropdown.component";
import {ToscaArtifactModel} from "../../../models/toscaArtifact";
import {ToscaArtifactService} from "../../services/tosca-artifact.service";
import {
    InterfaceOperationComponent
} from "../interface-operation/interface-operation.page.component";
import {Observable} from "rxjs/Observable";
import {PluginsService} from 'app/ng2/services/plugins.service';

export class UIOperationModel extends OperationModel {
    isCollapsed: boolean = true;
    isEllipsis: boolean;
    MAX_LENGTH = 75;

    constructor(operation: OperationModel) {
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

// tslint:disable-next-line:max-classes-per-file
class ModalTranslation {
    CREATE_TITLE: string;
    EDIT_TITLE: string;
    DELETE_TITLE: string;
    CANCEL_BUTTON: string;
    SAVE_BUTTON: string;
    CREATE_BUTTON: string;
    DELETE_BUTTON: string;
    deleteText: Function;

    constructor(private TranslateService: TranslateService) {
        this.TranslateService.languageChangedObservable.subscribe(lang => {
            this.CREATE_TITLE = this.TranslateService.translate("INTERFACE_CREATE_TITLE");
            this.EDIT_TITLE = this.TranslateService.translate('INTERFACE_EDIT_TITLE');
            this.DELETE_TITLE = this.TranslateService.translate("INTERFACE_DELETE_TITLE");
            this.CANCEL_BUTTON = this.TranslateService.translate("INTERFACE_CANCEL_BUTTON");
            this.SAVE_BUTTON = this.TranslateService.translate("INTERFACE_SAVE_BUTTON");
            this.CREATE_BUTTON = this.TranslateService.translate("INTERFACE_CREATE_BUTTON");
            this.DELETE_BUTTON = this.TranslateService.translate("INTERFACE_DELETE_BUTTON");
            this.deleteText = (operationName) => this.TranslateService.translate("INTERFACE_DELETE_TEXT", {operationName});
        });
    }
}

export class UIInterfaceModel extends InterfaceModel {
    isCollapsed: boolean = false;

    constructor(interf?: any) {
        super(interf);
        if (this.operations) {
            this.operations = this.operations.map((operation) => new UIOperationModel(operation));
        }
    }

    toggleCollapse() {
        this.isCollapsed = !this.isCollapsed;
    }
}

// tslint:disable-next-line:max-classes-per-file
@Component({
    selector: 'interface-definition',
    templateUrl: './interface-definition.page.component.html',
    styleUrls: ['interface-definition.page.component.less'],
    providers: [ModalService, TranslateService, InterfaceOperationComponent]
})
export class InterfaceDefinitionComponent {

    modalInstance: ComponentRef<ModalComponent>;
    interfaces: UIInterfaceModel[];
    inputs: InputBEModel[];

    deploymentArtifactsFilePath: Array<DropdownValue> = [];

    toscaArtifactTypes: Array<DropdownValue> = [];
    interfaceTypesTest: Array<DropdownValue> = [];
    interfaceTypesMap: Map<string, string[]>;

    isLoading: boolean;
    interfaceTypes: { [interfaceType: string]: string[] };
    modalTranslation: ModalTranslation;
    workflows: any[];
    capabilities: CapabilitiesGroup;
    isViewOnly: boolean;

    openOperation: OperationModel;
    enableWorkflowAssociation: boolean;
    workflowIsOnline: boolean;

    @Input() component: IComponent;
    @Input() readonly: boolean;
    @Input() enableMenuItems: Function;
    @Input() disableMenuItems: Function;

    constructor(
        @Inject(SdcConfigToken) private sdcConfig: ISdcConfig,
        @Inject("$state") private $state: ng.ui.IStateService,
        @Inject("Notification") private notification: any,
        private translateService: TranslateService,
        private componentServiceNg2: ComponentServiceNg2,
        private modalServiceNg2: ModalService,
        private modalServiceSdcUI: SdcUiServices.ModalService,
        private topologyTemplateService: TopologyTemplateService,
        private toscaArtifactService: ToscaArtifactService,
        private ComponentServiceNg2: ComponentServiceNg2,
        private WorkflowServiceNg2: WorkflowServiceNg2,
        private ModalServiceSdcUI: SdcUiServices.ModalService,
        private PluginsService: PluginsService
    ) {
        this.modalTranslation = new ModalTranslation(translateService);
        this.interfaceTypesMap = new Map<string, string[]>();
    }

    ngOnInit(): void {
        this.isLoading = true;
        this.interfaces = [];
        this.workflowIsOnline = !_.isUndefined(this.PluginsService.getPluginByStateUrl('workflowDesigner'));
        Observable.forkJoin(
            this.ComponentServiceNg2.getInterfaceOperations(this.component),
            this.ComponentServiceNg2.getComponentInputs(this.component),
            this.ComponentServiceNg2.getInterfaceTypes(this.component),
            this.ComponentServiceNg2.getCapabilitiesAndRequirements(this.component.componentType, this.component.uniqueId)
        ).subscribe((response: any[]) => {
            const callback = (workflows) => {
                this.isLoading = false;
                this.initInterfaces(response[0].interfaces);
                this.sortInterfaces();
                this.inputs = response[1].inputs;
                this.interfaceTypes = response[2];
                this.workflows = (workflows.items) ? workflows.items : workflows;
                this.capabilities = response[3].capabilities;
            };
            if (this.enableWorkflowAssociation && this.workflowIsOnline) {
                this.WorkflowServiceNg2.getWorkflows().subscribe(
                    callback,
                    (err) => {
                        this.workflowIsOnline = false;
                        callback([]);
                    }
                );
            } else {
                callback([]);
            }
        });
        this.loadToscaArtifacts();
    }

    initInterfaces(interfaces: InterfaceModel[]): void {
        if (interfaces) {
            this.interfaces = interfaces.map((interf) => new UIInterfaceModel(interf));
        }
    }

    private cancelAndCloseModal = () => {
        return this.modalServiceNg2.closeCurrentModal();
    }

    private disableSaveButton = (): boolean => {
        return this.isViewOnly ||
            (this.isEnableAddArtifactImplementation()
                && (!this.modalInstance.instance.dynamicContent.toscaArtifactTypeSelected ||
                    !this.modalInstance.instance.dynamicContent.artifactName)
            );
    }

    onSelectInterfaceOperation(interfaceModel: UIInterfaceModel, operation: InterfaceOperationModel) {
        const isEdit = operation !== undefined;
        const cancelButton: ButtonModel = new ButtonModel(this.modalTranslation.CANCEL_BUTTON, 'outline white', this.cancelAndCloseModal);
        const saveButton: ButtonModel = new ButtonModel(this.modalTranslation.SAVE_BUTTON, 'blue',
            () => isEdit ? this.updateOperation() : this.createOperationCallback(),
            this.disableSaveButton
        );
        const interfaceDataModal: ModalModel =
            new ModalModel('l', this.modalTranslation.EDIT_TITLE, '', [saveButton, cancelButton], 'custom');
        this.modalInstance = this.modalServiceNg2.createCustomModal(interfaceDataModal);

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
                isEdit: isEdit,
                interfaceTypesMap: this.interfaceTypesMap,
                modelName: this.component.model
            }
        );
        this.modalInstance.instance.open();
    }

    private updateOperation = (): void => {
        let operationToUpdate = this.modalInstance.instance.dynamicContent.instance.operationToUpdate;
        this.componentServiceNg2.updateComponentInterfaceOperation(this.component.uniqueId, operationToUpdate)
        .subscribe((newOperation: InterfaceOperationModel) => {
            let oldOpIndex;
            let oldInterf;
            this.interfaces.forEach(interf => {
                interf.operations.forEach(op => {
                    if (op.uniqueId === newOperation.uniqueId) {
                        oldInterf = interf;
                        oldOpIndex = interf.operations.findIndex((el) => el.uniqueId === op.uniqueId);
                    }
                });
            });
            newOperation = this.handleEnableAddArtifactImplementation(newOperation);
            oldInterf.operations.splice(oldOpIndex, 1);
            oldInterf.operations.push(new InterfaceOperationModel(newOperation));
        });
        this.modalServiceNg2.closeCurrentModal();
    }

    private createOperationCallback(): void {
        const operationToUpdate = this.modalInstance.instance.dynamicContent.instance.operationToUpdate;
        console.log('createOperationCallback', operationToUpdate);
        console.log('this.component', this.component);
        this.componentServiceNg2.createComponentInterfaceOperation(this.component.uniqueId, this.component.getTypeUrl(), operationToUpdate)
        .subscribe((newOperation: InterfaceOperationModel) => {
            const foundInterface = this.interfaces.find(value => value.type === newOperation.interfaceType);
            if (foundInterface) {
                foundInterface.operations.push(new UIOperationModel(new OperationModel(newOperation)));
            } else {
                const uiInterfaceModel = new UIInterfaceModel();
                uiInterfaceModel.type = newOperation.interfaceType;
                uiInterfaceModel.uniqueId = newOperation.interfaceType;
                uiInterfaceModel.operations = [];
                uiInterfaceModel.operations.push(new UIOperationModel(new OperationModel(newOperation)));
                this.interfaces.push(uiInterfaceModel);
            }
        });
        this.modalServiceNg2.closeCurrentModal();
    }

    private handleEnableAddArtifactImplementation = (newOperation: InterfaceOperationModel): InterfaceOperationModel => {
        if (!this.isEnableAddArtifactImplementation()) {
            newOperation.implementation.artifactType = null;
            newOperation.implementation.artifactVersion = null;
        }
        return newOperation;
    }

    private isEnableAddArtifactImplementation = (): boolean => {
        return this.modalInstance.instance.dynamicContent.enableAddArtifactImplementation;
    }

    private initInterfaceDefinition() {
        this.isLoading = true;
        this.interfaces = [];
        this.topologyTemplateService.getComponentInterfaceOperations(this.component.componentType, this.component.uniqueId)
        .subscribe((response) => {
            if (response.interfaces) {
                this.interfaces = response.interfaces.map((interfaceModel) => new UIInterfaceModel(interfaceModel));
            }
            this.isLoading = false;
        });
    }

    private loadToscaArtifacts() {
        this.toscaArtifactService.getToscaArtifacts(this.component.model).subscribe(response => {
            if (response) {
                let toscaArtifactsFound = <ToscaArtifactModel[]>_.values(response);
                toscaArtifactsFound.forEach(value => this.toscaArtifactTypes.push(new DropdownValue(value, value.type)));
            }
        }, error => {
            this.notification.error({
                message: 'Failed to Load Tosca Artifacts:' + error,
                title: 'Failure'
            });
        });
    }

    private loadInterfaceTypes() {
        this.componentServiceNg2.getInterfaceTypes(this.component).subscribe(response => {
            if (response) {
                console.info("loadInterfaceTypes ", response);
                for (const interfaceType in response) {
                    this.interfaceTypesMap.set(interfaceType, response[interfaceType]);
                    this.interfaceTypesTest.push(new DropdownValue(interfaceType, interfaceType));
                }
            }
        }, error => {
            this.notification.error({
                message: 'Failed to Load Interface Types:' + error,
                title: 'Failure'
            });
        });
    }

    collapseAll(value: boolean = true): void {
        this.interfaces.forEach(interfaceData => {
            interfaceData.isCollapsed = value;
        });
    }

    isAllCollapsed(): boolean {
        return this.interfaces.every((interfaceData) => interfaceData.isCollapsed);
    }

    isAllExpanded(): boolean {
        return this.interfaces.every((interfaceData) => !interfaceData.isCollapsed);
    }

    isInterfaceListEmpty(): boolean {
        return this.interfaces.length === 0;
    }

    isOperationListEmpty(): boolean {
        return this.interfaces.filter((interfaceData) => interfaceData.operations && interfaceData.operations.length > 0).length > 0;
    }

    onRemoveOperation = (event: Event, operation: OperationModel): void => {
        event.stopPropagation();

        const deleteButton: IModalButtonComponent = {
            id: 'deleteButton',
            text: this.modalTranslation.DELETE_BUTTON,
            type: 'primary',
            size: 'small',
            closeModal: true,
            callback: () => {
                this.ComponentServiceNg2
                .deleteInterfaceOperation(this.component, operation)
                .subscribe(() => {
                    const curInterf = this.interfaces.find((interf) => interf.type === operation.interfaceType);
                    const index = curInterf.operations.findIndex((el) => el.uniqueId === operation.uniqueId);
                    curInterf.operations.splice(index, 1);
                    if (!curInterf.operations.length) {
                        const interfIndex = this.interfaces.findIndex((interf) => interf.type === operation.interfaceType);
                        this.interfaces.splice(interfIndex, 1);
                    }
                });
            }
        };

        const cancelButton: IModalButtonComponent = {
            id: 'cancelButton',
            text: this.modalTranslation.CANCEL_BUTTON,
            type: 'secondary',
            size: 'small',
            closeModal: true,
            callback: () => {
                this.openOperation = null;
            },
        };

        this.ModalServiceSdcUI.openWarningModal(
            this.modalTranslation.DELETE_TITLE,
            this.modalTranslation.deleteText(operation.name),
            'deleteOperationModal',
            [deleteButton, cancelButton],
        );
    }

    private createOperation = (operation: OperationModel): void => {
        this.ComponentServiceNg2.createInterfaceOperation(this.component, operation).subscribe((response: OperationModel) => {
            this.openOperation = null;

            let curInterf = this.interfaces.find((interf) => interf.type === operation.interfaceType);

            if (!curInterf) {
                curInterf = new UIInterfaceModel({
                    type: response.interfaceType,
                    uniqueId: response.uniqueId,
                    operations: []
                });
                this.interfaces.push(curInterf);
            }

            const newOpModel = new UIOperationModel(response);
            curInterf.operations.push(newOpModel);
            this.sortInterfaces();

            if (operation.workflowAssociationType === WORKFLOW_ASSOCIATION_OPTIONS.EXTERNAL && operation.artifactData) {
                this.ComponentServiceNg2.uploadInterfaceOperationArtifact(this.component, newOpModel, operation).subscribe();
            } else if (response.workflowId && operation.workflowAssociationType === WORKFLOW_ASSOCIATION_OPTIONS.EXISTING) {
                this.WorkflowServiceNg2.associateWorkflowArtifact(this.component, response).subscribe();
            } else if (operation.workflowAssociationType === WORKFLOW_ASSOCIATION_OPTIONS.NEW) {
                this.$state.go('workspace.plugins', {path: 'workflowDesigner'});
            }
        });
    }

    private enableOrDisableSaveButton = (shouldEnable: boolean): void => {
        const saveButton = this.modalInstance.instance.dynamicContent.getButtonById('saveButton');
        saveButton.disabled = !shouldEnable;
    }

    private sortInterfaces(): void {
        this.interfaces = this.interfaces.filter((interf) => interf.operations && interf.operations.length > 0); // remove empty interfaces
        this.interfaces.sort((a, b) => a.type.localeCompare(b.type)); // sort interfaces alphabetically
        this.interfaces.forEach((interf) => {
            interf.operations.sort((a, b) => a.name.localeCompare(b.name)); // sort operations alphabetically
        });
    }

}
