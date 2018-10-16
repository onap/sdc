import * as _ from "lodash";
import {Component, ViewChild} from '@angular/core';

import {Subscription} from "rxjs/Subscription";

import {TranslateService} from "app/ng2/shared/translator/translate.service";
import {WorkflowServiceNg2} from 'app/ng2/services/workflow.service';
import {OperationModel, OperationParameter, InputBEModel, RadioButtonModel, WORKFLOW_ASSOCIATION_OPTIONS} from 'app/models';

import {Tabs, Tab} from "app/ng2/components/ui/tabs/tabs.component";
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

    outputParameters: Array<OperationParameter> = [];
    noAssignOutputParameters: Array<OperationParameter> = [];
    assignOutputParameters: { [key: string]: { [key: string]: Array<OperationParameter>; }; } = {};

    tableParameters: Array<OperationParameter> = [];

    associationOptions: Array<DropdownValue>;

    enableWorkflowAssociation: boolean;
    isEditMode: boolean = false;
    isLoading: boolean = false;
    readonly: boolean;
    isService: boolean;

    propertyTooltipText: String;

    TYPE_INPUT = 'Inputs';
    TYPE_OUTPUT = 'Outputs';

    @ViewChild('propertyInputTabs') propertyInputTabs: Tabs;
    currentTab: String;

    constructor(private workflowServiceNg2: WorkflowServiceNg2, private translateService: TranslateService) {
        this.translateService.languageChangedObservable.subscribe(lang => {
            this.propertyTooltipText = this.translateService.translate("OPERATION_PROPERTY_TOOLTIP_TEXT");

            this.associationOptions = [
                new DropdownValue(WORKFLOW_ASSOCIATION_OPTIONS.NONE, this.translateService.translate("NO_WORKFLOW_ASSOCIATION")),
                new DropdownValue(WORKFLOW_ASSOCIATION_OPTIONS.EXISTING, this.translateService.translate("EXISTING_WORKFLOW_ASSOCIATION"))
            ];
        });

        this.currentTab = this.TYPE_INPUT;
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
        if (!inputOperation) {
            this.operation.workflowAssociationType = WORKFLOW_ASSOCIATION_OPTIONS.NONE;
        }

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
                this.outputParameters = this.noAssignOutputParameters;
                this.buildParams();
                this.updateTable();
            } else {
                this.onSelectWorkflow(inputOperation.workflowVersionId).add(() => {
                    this.buildParams();
                    this.updateTable();
                });
            }

            if (inputOperation.uniqueId) {
                this.isEditMode = true;
            }
        }
        this.updateTable();
    }

    buildParams = () => {
        if (this.input.operation.outputParams) {
            this.currentTab = this.TYPE_OUTPUT;
            this.updateTable();
            _.forEach(
                [...this.input.operation.outputParams.listToscaDataDefinition].sort((a, b) => a.name.localeCompare(b.name)),
                (output: OperationParameter) => {
                    this.addParam(output);
                }
            );
        }
        this.currentTab = this.TYPE_INPUT;
        this.updateTable();
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
            this.assignOutputParameters[this.operation.workflowId] = {};
        }

        this.isLoading = true;
        return this.workflowServiceNg2.getWorkflowVersions(this.operation.workflowId).subscribe((versions: Array<any>) => {
            this.isLoading = false;

            this.workflowVersions = _.map(
                _.filter(
                    versions, version => version.state === this.workflowServiceNg2.VERSION_STATE_CERTIFIED
                ).sort((a, b) => a.name.localeCompare(b.name)),
                (version: any) => {
                    if (!this.assignInputParameters[this.operation.workflowId][version.id] && version.id !== selectedVersionId) {
                        this.assignInputParameters[this.operation.workflowId][version.id] = _.map(version.inputs, (input: OperationParameter) => {
                            return new OperationParameter({...input, type: input.type.toLowerCase()});
                        })
                        .sort((a, b) => a.name.localeCompare(b.name));

                        this.assignOutputParameters[this.operation.workflowId][version.id] = _.map(version.outputs, (output: OperationParameter) => {
                            return new OperationParameter({...output, type: output.type.toLowerCase()});
                        })
                        .sort((a, b) => a.name.localeCompare(b.name));
                    }
                    return new DropdownValue(version.id, `V ${version.name}`);
                }
            );

            if (selectedVersionId) {
                this.assignInputParameters[this.operation.workflowId][selectedVersionId] = [];
                this.assignOutputParameters[this.operation.workflowId][selectedVersionId] = [];
            }
            if (!selectedVersionId && this.workflowVersions.length) {
                this.operation.workflowVersionId = _.last(this.workflowVersions).value;
            }

            this.changeWorkflowVersion();
        });

    }

    changeWorkflowVersion() {
        this.inputParameters = this.assignInputParameters[this.operation.workflowId][this.operation.workflowVersionId];
        this.outputParameters = this.assignOutputParameters[this.operation.workflowId][this.operation.workflowVersionId];
        this.updateTable();
    }

    toggleAssociateWorkflow() {

        if (!this.isUsingExistingWF()) {
            this.inputParameters = this.noAssignInputParameters;
            this.outputParameters = this.noAssignOutputParameters;
        } else {
            if (!this.operation.workflowId || !this.operation.workflowVersionId) {
                this.inputParameters = [];
                this.outputParameters = [];
            } else {
                this.inputParameters = this.assignInputParameters[this.operation.workflowId][this.operation.workflowVersionId];
                this.outputParameters = this.assignOutputParameters[this.operation.workflowId][this.operation.workflowVersionId];
            }
        }

        this.updateTable();

    }

    tabChanged = (event) => {
        this.currentTab = event.title;
        this.updateTable();
    }

    updateTable() {
        switch (this.currentTab) {
            case this.TYPE_INPUT:
                this.tableParameters = this.inputParameters;
                break;
            case this.TYPE_OUTPUT:
                this.tableParameters = this.outputParameters;
                break;
        }
    }

    addParam(param?: OperationParameter): void {
        this.tableParameters.push(new OperationParameter(param));
    }

    isParamsValid(): boolean {
        let valid = true;
        _.forEach(this.tableParameters, param => {
            if (!param.name || (this.currentTab == this.TYPE_INPUT && !param.property)) {
                valid = false;
            }
        });
        return valid;
    }

    onRemoveParam = (param: OperationParameter): void => {
        let index = _.indexOf(this.tableParameters, param);
        this.tableParameters.splice(index, 1);
    }

    createParamLists(): void {
        this.operation.createInputParamsList(this.inputParameters);
        this.operation.createOutputParamsList(this.outputParameters);
    }

    isUsingExistingWF = (): boolean => {
        return this.operation.workflowAssociationType === WORKFLOW_ASSOCIATION_OPTIONS.EXISTING;
    }

    shouldCreateWF(): boolean {
        return this.operation.workflowAssociationType === WORKFLOW_ASSOCIATION_OPTIONS.NEW;
    }

    checkFormValidForSubmit(): boolean {
        return this.operation.operationType &&
            (!this.isUsingExistingWF() || this.operation.workflowVersionId) &&
            this.isParamsValid();
    }

}
