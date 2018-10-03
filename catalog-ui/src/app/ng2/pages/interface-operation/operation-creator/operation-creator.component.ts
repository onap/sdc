import * as _ from "lodash";
import {Component, ViewChild} from '@angular/core';

import {Subscription} from "rxjs/Subscription";

import {TranslateService} from "app/ng2/shared/translator/translate.service";
import {WorkflowServiceNg2} from 'app/ng2/services/workflow.service';
import {OperationModel, OperationParameter, InputBEModel, RadioButtonModel} from 'app/models';

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
    workflowAssociationType: String;

    enableWorkflowAssociation: boolean;
    isEditMode: boolean = false;
    isLoading: boolean = false;
    readonly: boolean;
    isService: boolean;

    propertyTooltipText: String;

    WORKFLOW_ASSOCIATION_OPTIONS = {
        NONE: 'No Workflow',
        NEW: 'New Workflow',
        EXISTING: 'Existing Workflow'
    }

    TYPE_INPUT = 'Inputs';
    TYPE_OUTPUT = 'Outputs';

    @ViewChild('propertyInputTabs') propertyInputTabs: Tabs;
    currentTab: String;

    constructor(private workflowServiceNg2: WorkflowServiceNg2, private translateService: TranslateService) {
        this.translateService.languageChangedObservable.subscribe(lang => {
            this.propertyTooltipText = this.translateService.translate("OPERATION_PROPERTY_TOOLTIP_TEXT");
        });
        this.currentTab = this.TYPE_INPUT;

        this.associationOptions = [
            new DropdownValue(this.WORKFLOW_ASSOCIATION_OPTIONS.NONE, this.WORKFLOW_ASSOCIATION_OPTIONS.NONE),
            new DropdownValue(this.WORKFLOW_ASSOCIATION_OPTIONS.NEW, this.WORKFLOW_ASSOCIATION_OPTIONS.NEW),
            new DropdownValue(this.WORKFLOW_ASSOCIATION_OPTIONS.EXISTING, this.WORKFLOW_ASSOCIATION_OPTIONS.EXISTING)
        ];
        this.workflowAssociationType = this.WORKFLOW_ASSOCIATION_OPTIONS.NONE;
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
                this.outputParameters = this.noAssignOutputParameters;
                this.buildParams();
                this.updateTable();
            } else {
                this.workflowAssociationType = this.WORKFLOW_ASSOCIATION_OPTIONS.EXISTING;
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

                        this.assignOutputParameters[this.operation.workflowId][version.id] = _.map(version.outputs, (output: any) => {
                            return new OperationParameter({
                                name: output.name,
                                type: output.type && output.type.toLowerCase(),
                                property: null,
                                mandatory: output.mandatory,
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
        this.outputParameters = this.assignOutputParameters[this.operation.workflowId][this.operation.workflowVersionId];
        this.updateTable();
    }

    toggleAssociateWorkflow() {

        if (this.workflowAssociationType !== this.WORKFLOW_ASSOCIATION_OPTIONS.EXISTING) {
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

        for (let ctr=0; ctr<this.tableParameters.length; ctr++) {
            if (!this.tableParameters[ctr].name ||
                (this.currentTab == this.TYPE_INPUT && !this.tableParameters[ctr].property)
            ) {
                return false;
            }
        }
        return true;

    }

    onRemoveParam = (param: OperationParameter): void => {
        let index = _.indexOf(this.tableParameters, param);
        this.tableParameters.splice(index, 1);
    }

    createParamLists(): void {
        this.operation.createInputParamsList(_.map(this.inputParameters, input => {
            return {
                name: input.name,
                type: input.type,
                property: input.property,
                mandatory: Boolean(input.mandatory)
            }
        }));
        this.operation.createOutputParamsList(_.map(this.outputParameters, output => {
            return {
                name: output.name,
                type: output.type,
                property: output.property,
                mandatory: Boolean(output.mandatory)
            }
        }));
    }

    checkFormValidForSubmit(): boolean {
        return this.operation.operationType &&
            (this.workflowAssociationType !== this.WORKFLOW_ASSOCIATION_OPTIONS.EXISTING || this.operation.workflowVersionId) &&
            this.isParamsValid();
    }

}
