import * as _ from "lodash";
import {Component, Input, ComponentRef} from '@angular/core';
import {Component as IComponent} from 'app/models/components/component';
import {ModalService} from 'app/ng2/services/modal.service';
import {ModalModel, ButtonModel, InputModel, OperationModel, CreateOperationResponse} from 'app/models';
import {ModalComponent} from 'app/ng2/components/ui/modal/modal.component';
import {ComponentServiceNg2} from 'app/ng2/services/component-services/component.service';
import {ComponentGenericResponse} from 'app/ng2/services/responses/component-generic-response';
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

    @Input() component: IComponent;
    @Input() readonly: boolean;
    @Input() state: ng.ui.IStateService

    constructor(
        private ComponentServiceNg2: ComponentServiceNg2,
        private ModalServiceNg2: ModalService,
    ) {}

    ngOnInit(): void {
        this.initInterfaceOperations();
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

        this.ComponentServiceNg2.getComponentInputs(this.component).subscribe((response: ComponentGenericResponse) => {

            const cancelButton: ButtonModel = new ButtonModel(
                'Cancel',
                'outline white',
                this.ModalServiceNg2.closeCurrentModal,
            );

            const saveButton: ButtonModel = new ButtonModel(
                modalData.saveBtnText,
                'blue',
                () => {
                    this.modalInstance.instance.dynamicContent.instance.createInputParamList();
                    this.ModalServiceNg2.closeCurrentModal();
                    const {operation} = this.modalInstance.instance.dynamicContent.instance;
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
                .subscribe(this.initInterfaceOperations);
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

            const workflowId = response.artifactUUID;
            const operationId = response.uniqueId;
            const resourceId = this.component.uuid;

            const queryParams = {
                id: workflowId,
                operationID: operationId,
                uuid: resourceId,
                displayMode: 'create',
            };

            this.state.go('workspace.plugins', {
                path: 'workflowDesigner',
                queryParams
            });

        });
    }

    private updateOperation = (operation: OperationModel): any => {
        this.ComponentServiceNg2.updateInterfaceOperation(this.component, operation).subscribe(() => {
            this.initInterfaceOperations();
        });
    }

    private initInterfaceOperations = (): void => {
        this.ComponentServiceNg2.getInterfaceOperations(this.component).subscribe((response: ComponentGenericResponse) => {
            let {interfaceOperations} = response;
            this.component.interfaceOperations = interfaceOperations;
            this.operationList = _.toArray(interfaceOperations);
        });
    }

}
