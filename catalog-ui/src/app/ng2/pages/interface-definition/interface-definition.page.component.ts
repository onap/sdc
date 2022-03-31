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

import {ISdcConfig, SdcConfigToken} from "app/ng2/config/sdc-config.config";
import {TranslateService} from "app/ng2/shared/translator/translate.service";

import {ModalComponent} from 'app/ng2/components/ui/modal/modal.component';
import {ModalService} from 'app/ng2/services/modal.service';
import {ButtonModel, CapabilitiesGroup, ModalModel, OperationModel} from 'app/models';

import {ComponentServiceNg2} from 'app/ng2/services/component-services/component.service';

import {SdcUiServices} from 'onap-ui-angular';
import {TopologyTemplateService} from "../../services/component-services/topology-template.service";
import {
    ComponentInterfaceDefinitionModel,
    InputOperationParameter,
    InterfaceOperationModel
} from "../../../models/interfaceOperation";
import {
    PropertyParamRowComponent
} from "../composition/interface-operatons/operation-creator/property-param-row/property-param-row.component";
import {
    InterfaceOperationHandlerComponent
} from "../composition/interface-operatons/operation-creator/interface-operation-handler.component";
import {
    DropdownValue
} from "../../components/ui/form-components/dropdown/ui-element-dropdown.component";
import {ToscaArtifactModel} from "../../../models/toscaArtifact";
import {ToscaArtifactService} from "../../services/tosca-artifact.service";
import {
    UIInterfaceOperationModel
} from "../composition/interface-operatons/interface-operations.component";

export class UIOperationModel extends OperationModel {
    isCollapsed: boolean = true;
    isEllipsis: boolean;
    MAX_LENGTH = 75;

    constructor(operation: UIOperationModel) {
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

// tslint:disable-next-line:max-classes-per-file
@Component({
    selector: 'interface-definition',
    templateUrl: './interface-definition.page.component.html',
    styleUrls: ['interface-definition.page.component.less'],
    providers: [ModalService, TranslateService]
})

export class InterfaceDefinitionComponent {

    modalInstance: ComponentRef<ModalComponent>;
    interfaces: UIInterfaceModel[];
    inputs: Array<InputOperationParameter> = [];

    properties: Array<PropertyParamRowComponent> = [];
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
        private toscaArtifactService: ToscaArtifactService
    ) {
        this.modalTranslation = new ModalTranslation(translateService);
        this.interfaceTypesMap = new Map<string, string[]>();
    }

    ngOnInit(): void {
        console.info("this.component.lifecycleState ", this.component.lifecycleState);
        if (this.component) {
            this.isViewOnly = this.component.componentMetadata.isComponentDataEditable();
            this.initInterfaceDefinition();
            this.loadInterfaceTypes();
            this.loadToscaArtifacts();
        }
    }

    private cancelAndCloseModal = () => {
        return this.modalServiceNg2.closeCurrentModal();
    }

    private disableSaveButton = (): boolean => {
        return this.isViewOnly ||
            (this.isEnableAddArtifactImplementation()
                && (!this.modalInstance.instance.dynamicContent.instance.toscaArtifactTypeSelected ||
                    !this.modalInstance.instance.dynamicContent.instance.artifactName)
            );
    }

    onSelectInterfaceOperation(interfaceModel: UIInterfaceModel, operation: InterfaceOperationModel) {
        const cancelButton: ButtonModel = new ButtonModel(this.modalTranslation.CANCEL_BUTTON, 'outline white', this.cancelAndCloseModal);
        const saveButton: ButtonModel = new ButtonModel(this.modalTranslation.SAVE_BUTTON, 'blue', () =>
            this.updateOperation(), this.disableSaveButton);
        const interfaceDataModal: ModalModel =
            new ModalModel('l', this.modalTranslation.EDIT_TITLE, '', [saveButton, cancelButton], 'custom');
        this.modalInstance = this.modalServiceNg2.createCustomModal(interfaceDataModal);

        this.modalServiceNg2.addDynamicContentToModal(
            this.modalInstance,
            InterfaceOperationHandlerComponent,
            {
                deploymentArtifactsFilePath: this.deploymentArtifactsFilePath,
                toscaArtifactTypes: this.toscaArtifactTypes,
                selectedInterface: interfaceModel,
                selectedInterfaceOperation: operation,
                validityChangedCallback: this.disableSaveButton,
                isViewOnly: this.isViewOnly,
                interfaceTypesMap: this.interfaceTypesMap,
            });
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

    private handleEnableAddArtifactImplementation = (newOperation: InterfaceOperationModel): InterfaceOperationModel => {
        if (!this.isEnableAddArtifactImplementation()) {
            newOperation.implementation.artifactType = null;
            newOperation.implementation.artifactVersion = null;
        }
        return newOperation;
    }

    private isEnableAddArtifactImplementation = (): boolean => {
        return this.modalInstance.instance.dynamicContent.instance.enableAddArtifactImplementation;
    }

    private initInterfaceDefinition() {
        this.isLoading = true;
        this.interfaces = [];
        this.topologyTemplateService.getComponentInterfaceOperations(this.component.componentType, this.component.uniqueId)
        .subscribe((response) => {
            if (response.interfaces) {
                this.interfaces = _.map(response.interfaces, (interfaceModel) => new UIInterfaceModel(interfaceModel));
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
        return _.every(this.interfaces, (interfaceData) => interfaceData.isCollapsed);
    }

    isAllExpanded(): boolean {
        return _.every(this.interfaces, (interfaceData) => !interfaceData.isCollapsed);
    }

    isInterfaceListEmpty(): boolean {
        return this.interfaces.length === 0;
    }

    isOperationListEmpty(): boolean {
        return _.filter(this.interfaces, (interfaceData) =>
            interfaceData.operations && interfaceData.operations.length > 0).length > 0;
    }

}
