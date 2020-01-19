import * as _ from "lodash";
import { Component, Input, Output, ComponentRef, Inject } from '@angular/core';
import {Component as IComponent } from 'app/models/components/component';

import { SdcConfigToken, ISdcConfig } from "app/ng2/config/sdc-config.config";
import {TranslateService } from "app/ng2/shared/translator/translate.service";

import {Observable } from "rxjs/Observable";

import {ModalComponent } from 'app/ng2/components/ui/modal/modal.component';
import {ModalService } from 'app/ng2/services/modal.service';
import {
    InputBEModel,
    OperationModel,
    InterfaceModel,
    WORKFLOW_ASSOCIATION_OPTIONS,
    CapabilitiesGroup,
    Capability
} from 'app/models';

// import {SdcUiComponents } from 'sdc-ui/lib/angular';
// import {ModalButtonComponent } from 'sdc-ui/lib/angular/components';
// import { IModalButtonComponent, IModalConfig } from 'sdc-ui/lib/angular/modals/models/modal-config';

import {ComponentServiceNg2 } from 'app/ng2/services/component-services/component.service';
import {PluginsService } from 'app/ng2/services/plugins.service';
import {WorkflowServiceNg2 } from 'app/ng2/services/workflow.service';

import { OperationCreatorComponent, OperationCreatorInput } from 'app/ng2/pages/interface-operation/operation-creator/operation-creator.component';
import { IModalButtonComponent } from 'onap-ui-angular';
import { ModalButtonComponent } from 'onap-ui-angular';
import { IModalConfig } from 'onap-ui-angular';
import { SdcUiServices } from 'onap-ui-angular';

export class UIOperationModel extends OperationModel {
    isCollapsed: boolean = true;
    isEllipsis: boolean;
    MAX_LENGTH = 75;
    _description: string;

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

// tslint:disable-next-line:max-classes-per-file
export class UIInterfaceModel extends InterfaceModel {
    isCollapsed: boolean = false;

    constructor(interf?: any) {
        super(interf);
        this.operations = _.map(
            this.operations,
            (operation) => new UIOperationModel(operation)
        );
    }

    toggleCollapse() {
        this.isCollapsed = !this.isCollapsed;
    }
}

// tslint:disable-next-line:max-classes-per-file
@Component({
    selector: 'interface-operation',
    templateUrl: './interface-operation.page.component.html',
    styleUrls: ['interface-operation.page.component.less'],
    providers: [ModalService, TranslateService]
})

export class InterfaceOperationComponent {

    interfaces: UIInterfaceModel[];
    modalInstance: ComponentRef<ModalComponent>;
    openOperation: OperationModel;
    enableWorkflowAssociation: boolean;
    inputs: InputBEModel[];
    isLoading: boolean;
    interfaceTypes: { [interfaceType: string]: string[] };
    modalTranslation: ModalTranslation;
    workflowIsOnline: boolean;
    workflows: any[];
    capabilities: CapabilitiesGroup;

    @Input() component: IComponent;
    @Input() readonly: boolean;
    @Input() enableMenuItems: Function;
    @Input() disableMenuItems: Function;

    constructor(
        @Inject(SdcConfigToken) private sdcConfig: ISdcConfig,
        @Inject("$state") private $state: ng.ui.IStateService,
        private TranslateService: TranslateService,
        private PluginsService: PluginsService,
        private ComponentServiceNg2: ComponentServiceNg2,
        private WorkflowServiceNg2: WorkflowServiceNg2,
        private ModalServiceNg2: ModalService,
        private ModalServiceSdcUI: SdcUiServices.ModalService
        
    ) {
        this.enableWorkflowAssociation = sdcConfig.enableWorkflowAssociation;
        this.modalTranslation = new ModalTranslation(TranslateService);
    }

    ngOnInit(): void {
        this.isLoading = true;
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
                this.workflows = workflows;
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
    }

    initInterfaces(interfaces: InterfaceModel[]): void {
        this.interfaces = _.map(interfaces, (interf) => new UIInterfaceModel(interf));
    }

    sortInterfaces(): void {
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

    getDisabled = (): boolean => {
        return !this.modalInstance.instance.dynamicContent.instance.checkFormValidForSubmit();
    }

    onEditOperation = (operation?: OperationModel): void => {

        const modalMap = {
            create: {
                modalTitle: this.modalTranslation.CREATE_TITLE,
                saveBtnText: this.modalTranslation.CREATE_BUTTON,
                submitCallback: this.createOperation,
            },
            edit: {
                modalTitle: this.modalTranslation.EDIT_TITLE,
                saveBtnText: this.modalTranslation.SAVE_BUTTON,
                submitCallback: this.updateOperation,
            }
        };

        const modalData = operation ? modalMap.edit : modalMap.create;

        if (this.openOperation) {
            if (operation ? operation.uniqueId === this.openOperation.uniqueId : !this.openOperation.uniqueId) {
                operation = this.openOperation;
            }
        }

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

        const saveButton: IModalButtonComponent = {
            id: 'saveButton',
            text: modalData.saveBtnText,
            type: 'primary',
            size: 'small',
            closeModal: true,
            callback: () => {
                const modalInstance = this.ModalServiceSdcUI.getCurrentInstance().innerModalContent.instance;

                const {operation, isUsingExistingWF, createParamLists} = modalInstance;
                createParamLists();
                this.openOperation = {...operation};

                if (this.enableWorkflowAssociation && !isUsingExistingWF()) {
                    operation.workflowId = null;
                    operation.workflowVersionId = null;
                }

                modalData.submitCallback(operation);
            }
        };

        const input: OperationCreatorInput = {
            allWorkflows: this.workflows,
            inputOperation: operation,
            interfaces: this.interfaces,
            inputProperties: this.inputs,
            enableWorkflowAssociation: this.enableWorkflowAssociation,
            readonly: this.readonly,
            interfaceTypes: this.interfaceTypes,
            validityChangedCallback: this.enableOrDisableSaveButton,
            workflowIsOnline: this.workflowIsOnline,
            capabilities: _.filter(CapabilitiesGroup.getFlattenedCapabilities(this.capabilities), (capability: Capability) => capability.ownerId === this.component.uniqueId)
        };

        const modalConfig: IModalConfig = {
            title: modalData.modalTitle,
            size: 'l',
            type: 'custom',
            buttons: [saveButton, cancelButton] as IModalButtonComponent[]
        };

        this.ModalServiceSdcUI.openCustomModal(modalConfig, OperationCreatorComponent, input);

    }

    onRemoveOperation = (event: Event, operation: OperationModel): void => {
        event.stopPropagation();

        const confirmCallback = () => {
            this.ComponentServiceNg2
                .deleteInterfaceOperation(this.component, operation)
                .subscribe(() => {
                    const curInterf = _.find(this.interfaces, (interf) => interf.type === operation.interfaceType);
                    const index = _.findIndex(curInterf.operations, (el) => el.uniqueId === operation.uniqueId);
                    curInterf.operations.splice(index, 1);
                    if (!curInterf.operations.length) {
                        const interfIndex = _.findIndex(this.interfaces, (interf) => interf.type === operation.interfaceType);
                        this.interfaces.splice(interfIndex, 1);
                    }
                });
        }

        this.ModalServiceSdcUI.openAlertModal(
            this.modalTranslation.DELETE_TITLE,
            this.modalTranslation.deleteText(operation.name),
            this.modalTranslation.DELETE_BUTTON,
            confirmCallback,
            'deleteOperationModal'
        );
    }

    private enableOrDisableSaveButton = (shouldEnable: boolean): void => {
        const saveButton: ModalButtonComponent = this.ModalServiceSdcUI.getCurrentInstance().getButtonById('saveButton');
        saveButton.disabled = !shouldEnable;
    }

    private createOperation = (operation: OperationModel): void => {
        this.ComponentServiceNg2.createInterfaceOperation(this.component, operation).subscribe((response: OperationModel) => {
            this.openOperation = null;

            let curInterf = _.find(
                this.interfaces,
                (interf) => interf.type === operation.interfaceType
            );

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
                this.$state.go('workspace.plugins', { path: 'workflowDesigner' });
            }
        });
    }

    private updateOperation = (operation: OperationModel): void => {
        this.ComponentServiceNg2.updateInterfaceOperation(this.component, operation).subscribe((newOperation: OperationModel) => {
            this.openOperation = null;

            let oldOpIndex;
            let oldInterf;
            _.forEach(this.interfaces, (interf) => {
                _.forEach(interf.operations, (op) => {
                    if (op.uniqueId === newOperation.uniqueId) {
                        oldInterf = interf;
                        oldOpIndex = _.findIndex(interf.operations, (el) => el.uniqueId === op.uniqueId);
                    }
                })
            });
            oldInterf.operations.splice(oldOpIndex, 1);

            const newInterf = _.find(this.interfaces, (interf) => interf.type === operation.interfaceType);
            const newOpModel = new UIOperationModel(newOperation);
            newInterf.operations.push(newOpModel);
            this.sortInterfaces();

            if (operation.workflowAssociationType === WORKFLOW_ASSOCIATION_OPTIONS.EXTERNAL && operation.artifactData) {
                this.ComponentServiceNg2.uploadInterfaceOperationArtifact(this.component, newOpModel, operation).subscribe();
            } else if (newOperation.workflowId && operation.workflowAssociationType === WORKFLOW_ASSOCIATION_OPTIONS.EXISTING) {
                this.WorkflowServiceNg2.associateWorkflowArtifact(this.component, newOperation).subscribe();
            }
        });
    }

}
