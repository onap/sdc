import * as _ from "lodash";
import {Component} from '@angular/core';

import {Subscription} from "rxjs/Subscription";

import {TranslateService} from "app/ng2/shared/translator/translate.service";
import {WorkflowServiceNg2} from 'app/ng2/services/workflow.service';
import {OperationModel, OperationParameter, InputBEModel} from 'app/models';

import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";

export interface OperationCreatorInput {
    operation: OperationModel,
    inputProperties: Array<InputBEModel>,
    enableWorkflowAssociation: boolean,
    readonly: boolean,
    isService: boolean
}

@Component({
    selector: 'operation-creator',
    templateUrl: './operation-creator.component.html',
    styleUrls: ['./operation-creator.component.less'],
    providers: [TranslateService]
})

export class OperationCreatorComponent {

    input: OperationCreatorInput;
    operation: OperationModel;

    workflows: Array<DropdownValue> = [];
    workflowVersions: Array<DropdownValue> = [];
    inputProperties: Array<DropdownValue> = [];
    inputPropertyTypes: { [key: string]: string };

    inputParameters: Array<OperationParameter> = [];
    noAssignInputParameters: Array<OperationParameter> = [];
    assignInputParameters: { [key: string]: { [key: string]: Array<OperationParameter>; }; } = {};

    enableWorkflowAssociation: boolean;
    isAssociateWorkflow: boolean;
    isEditMode: boolean = false;
    isLoading: boolean = false;
    readonly: boolean;
    isService: boolean;

    propertyTooltipText: String;

    constructor(private workflowServiceNg2: WorkflowServiceNg2, private translateService: TranslateService) {
        this.translateService.languageChangedObservable.subscribe(lang => {
            this.propertyTooltipText = this.translateService.translate("OPERATION_PROPERTY_TOOLTIP_TEXT");
        });
    }

    ngOnInit() {

        this.readonly = this.input.readonly;
        this.isService = this.input.isService;
        this.enableWorkflowAssociation = this.input.enableWorkflowAssociation && !this.isService;

        this.inputProperties = _.map(this.input.inputProperties,
            (input: InputBEModel) => new DropdownValue(input.uniqueId, input.name)
        );

        this.inputPropertyTypes = {};
        _.forEach(this.input.inputProperties, (input: InputBEModel) => {
            this.inputPropertyTypes[input.uniqueId] = input.type;
        });

        const inputOperation = this.input.operation;
        this.operation = new OperationModel(inputOperation || {});

        if (this.enableWorkflowAssociation) {
            this.isLoading = true;
            this.workflowServiceNg2.getWorkflows().subscribe(workflows => {
                this.isLoading = false;
                this.workflows = _.map(workflows, (workflow: any) => {
                    return new DropdownValue(workflow.id, workflow.name);
                });
                this.reconstructOperation();
            });
        } else {
            this.reconstructOperation();
        }

    }

    reconstructOperation = () => {
        const inputOperation = this.input.operation;
        if (inputOperation) {
            if (!this.enableWorkflowAssociation || !inputOperation.workflowVersionId || this.isService) {
                this.inputParameters = this.noAssignInputParameters;
                this.isAssociateWorkflow = false;
                this.buildInputParams();
            } else {
                this.isAssociateWorkflow = true;
                this.onSelectWorkflow(inputOperation.workflowVersionId).add(this.buildInputParams);
            }

            if (inputOperation.uniqueId) {
                this.isEditMode = true;
            }
        }
    }

    buildInputParams = () => {
        if (this.input.operation.inputParams) {
            _.forEach(
                [...this.input.operation.inputParams.listToscaDataDefinition].sort((a, b) => a.name.localeCompare(b.name)),
                (input: OperationParameter) => {
                    this.addParam(input);
                }
            );
        }
    }

    onSelectWorkflow(selectedVersionId?: string): Subscription {

        this.operation.workflowVersionId = selectedVersionId || null;
        if (!this.assignInputParameters[this.operation.workflowId]) {
            this.assignInputParameters[this.operation.workflowId] = {};
        }

        this.isLoading = true;
        return this.workflowServiceNg2.getWorkflowVersions(this.operation.workflowId).subscribe((versions: Array<any>) => {
            this.isLoading = false;

            this.workflowVersions = _.map(
                _.filter(
                    versions, version => version.state === this.workflowServiceNg2.VERSION_STATE_CERTIFIED
                ).sort((a, b) => a.name.localeCompare(b.name)),
                (version: any) => {
                    if (!this.assignInputParameters[this.operation.workflowId][version.id]) {
                        this.assignInputParameters[this.operation.workflowId][version.id] = _.map(version.inputs, (input: any) => {
                            return new OperationParameter({
                                name: input.name,
                                type: input.type && input.type.toLowerCase(),
                                property: null,
                                mandatory: input.mandatory,
                            });
                        })
                        .sort((a, b) => a.name.localeCompare(b.name));
                    }
                    return new DropdownValue(version.id, `V ${version.name}`);
                }
            );

            if (!selectedVersionId && this.workflowVersions.length) {
                this.operation.workflowVersionId = _.last(this.workflowVersions).value;
            }
            this.changeWorkflowVersion();
        });

    }

    changeWorkflowVersion() {
        this.inputParameters = this.assignInputParameters[this.operation.workflowId][this.operation.workflowVersionId];
    }

    toggleAssociateWorkflow() {

        if (!this.isAssociateWorkflow) {
            this.inputParameters = this.noAssignInputParameters;
        } else {
            if (!this.operation.workflowId || !this.operation.workflowVersionId) {
                this.inputParameters = [];
            } else {
                this.inputParameters = this.assignInputParameters[this.operation.workflowId][this.operation.workflowVersionId];
            }
        }

    }

    addParam(param?: OperationParameter): void {
        this.inputParameters.push(new OperationParameter(param));
    }

    isParamsValid(): boolean {

        for (let ctr=0; ctr<this.inputParameters.length; ctr++) {
            if (!this.inputParameters[ctr].name || !this.inputParameters[ctr].property) {
                return false;
            }
        }
        return true;

    }

    onRemoveParam = (param: OperationParameter): void => {
        let index = _.indexOf(this.inputParameters, param);
        this.inputParameters.splice(index, 1);
    }

    createInputParamList(): void {
        this.operation.createInputParamsList(this.inputParameters);
    }

    checkFormValidForSubmit(): boolean {
        return this.operation.operationType &&
            (!this.isAssociateWorkflow || this.operation.workflowVersionId) &&
            this.isParamsValid();
    }

}
