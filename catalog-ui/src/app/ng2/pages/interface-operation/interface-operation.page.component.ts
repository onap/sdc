import * as _ from "lodash";
import {Component, Input, ComponentRef, Inject} from '@angular/core';
import {Component as IComponent} from 'app/models/components/component';

import {ModalComponent} from 'app/ng2/components/ui/modal/modal.component';
import {ModalService} from 'app/ng2/services/modal.service';
import {ModalModel, ButtonModel, InputModel, OperationModel, CreateOperationResponse} from 'app/models';

import {ComponentServiceNg2} from 'app/ng2/services/component-services/component.service';
import {ComponentGenericResponse} from 'app/ng2/services/responses/component-generic-response';
import {WorkflowServiceNg2} from 'app/ng2/services/workflow.service';

import {OperationCreatorComponent} from './operation-creator/operation-creator.component';

@Component({
    selector: 'interface-operation',
    templateUrl: './interface-operation.page.component.html',
    styleUrls: ['interface-operation.page.component.less'],
    providers: [ModalService]
})

export class InterfaceOperationComponent {

    modalInstance: ComponentRef<ModalComponent>;
    operationList: Array<OperationModel> = [];
    openOperation: OperationModel;

    @Input() component: IComponent;
    @Input() readonly: boolean;

    constructor(
        @Inject('$state') private $state:ng.ui.IStateService,
        private ComponentServiceNg2: ComponentServiceNg2,
        private WorkflowServiceNg2: WorkflowServiceNg2,
        private ModalServiceNg2: ModalService,
    ) {}

    ngOnInit(): void {
        this.ComponentServiceNg2.getInterfaceOperations(this.component).subscribe((response: ComponentGenericResponse) => {
            let {interfaceOperations} = response;
            this.component.interfaceOperations = interfaceOperations;
            this.operationList = _.toArray(interfaceOperations);
        });
    }

    getDisabled = (): boolean => {
        return !this.modalInstance.instance.dynamicContent.instance.checkFormValidForSubmit();
    }

    onEditOperation = (operation: OperationModel): void => {
        this.ComponentServiceNg2
            .getInterfaceOperation(this.component, operation)
            .subscribe(op => this.onAddOperation(op));
    }

    onAddOperation = (operation?: OperationModel): void => {
        const modalMap = {
            create: {
                modalTitle: 'Create a New Operation',
                saveBtnText: 'Create',
                submitCallback: this.createOperation,
            },
            edit: {
                modalTitle: 'Edit Operation',
                saveBtnText: 'Save',
                submitCallback: this.updateOperation,
            }
        };

        const modalData = operation ? modalMap.edit : modalMap.create;

        if (this.openOperation) {
            if (operation ? operation.uniqueId === this.openOperation.uniqueId : !this.openOperation.uniqueId) {
                operation = this.openOperation;
            }
        }

        this.ComponentServiceNg2.getComponentInputs(this.component).subscribe((response: ComponentGenericResponse) => {

            const cancelButton: ButtonModel = new ButtonModel(
                'Cancel',
                'outline white',
                () => {
                    this.openOperation = null;
                    this.ModalServiceNg2.closeCurrentModal();
                },
            );

            const saveButton: ButtonModel = new ButtonModel(
                modalData.saveBtnText,
                'blue',
                () => {
                    this.modalInstance.instance.dynamicContent.instance.createInputParamList();
                    this.ModalServiceNg2.closeCurrentModal();

                    const {operation, isAssociateWorkflow} = this.modalInstance.instance.dynamicContent.instance;
                    this.openOperation = {...operation};

                    if (!isAssociateWorkflow) {
                        operation.workflowId = null;
                        operation.workflowVersionId = null;
                    }

                    modalData.submitCallback(operation);
                },
                this.getDisabled,
            );

            const modalModel: ModalModel = new ModalModel(
                'l',
                modalData.modalTitle,
                '',
                [saveButton, cancelButton],
                'standard',
            );

            this.modalInstance = this.ModalServiceNg2.createCustomModal(modalModel);

            this.ModalServiceNg2.addDynamicContentToModal(
                this.modalInstance,
                OperationCreatorComponent,
                {
                    operation,
                    inputProperties: response.inputs,
                },
            );

            this.modalInstance.instance.open();
        });
    }

    onRemoveOperation = (event: Event, operation: OperationModel): void => {
        event.stopPropagation();

        const confirmCallback = () => {
            this.ModalServiceNg2.closeCurrentModal();
            this.ComponentServiceNg2
                .deleteInterfaceOperation(this.component, operation)
                .subscribe(() => {
                    const index = _.findIndex(this.operationList, el => el.uniqueId === operation.uniqueId);
                    this.operationList.splice(index, 1);
                    this.component.interfaceOperations = this.operationList;
                });
        }

        this.modalInstance = this.ModalServiceNg2.createActionModal(
            operation.operationType,
            'Are you sure you want to delete this operation?',
            'Delete',
            confirmCallback,
            'Cancel',
        );

        this.modalInstance.instance.open();
    }

    private createOperation = (operation: OperationModel): any => {
        this.ComponentServiceNg2.createInterfaceOperation(this.component, operation).subscribe((response: CreateOperationResponse) => {
            this.openOperation = null;
            this.operationList.push(new OperationModel(response));
            if (response.workflowId) {
                const resourceId = this.component.uuid;
                const operationId = response.uniqueId;
                const workflowId = response.workflowId;
                const versionId = response.workflowVersionId;
                const artifactId = response.artifactUUID;
                this.WorkflowServiceNg2.associateWorkflowArtifact(resourceId, operationId, workflowId, versionId, artifactId).subscribe(res => {
                    console.log('Workflow artifact successfully deilvered.');
                });
            }
        });
    }

    private updateOperation = (operation: OperationModel): any => {
        this.ComponentServiceNg2.updateInterfaceOperation(this.component, operation).subscribe(newOperation => {
            this.openOperation = null;
            const index = _.findIndex(this.operationList, el => el.uniqueId === operation.uniqueId);
            this.operationList.splice(index, 1, newOperation);
            this.component.interfaceOperations = this.operationList;
        });
    }

}
