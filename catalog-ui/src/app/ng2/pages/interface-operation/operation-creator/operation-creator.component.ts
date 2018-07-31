import * as _ from "lodash";
import {Component} from '@angular/core';

import {Subscription} from "rxjs/Subscription";

import {TranslateService} from "app/ng2/shared/translator/translate.service";
import {WorkflowServiceNg2} from 'app/ng2/services/workflow.service';
import {InputModel, OperationModel, OperationParameter} from 'app/models';

import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";

@Component({
    selector: 'operation-creator',
    templateUrl: './operation-creator.component.html',
    styleUrls:['./operation-creator.component.less'],
    providers: [TranslateService]
})

export class OperationCreatorComponent {

    input: any;
    operation: OperationModel;

    workflows: Array<DropdownValue> = [];
    workflowVersions: Array<DropdownValue> = [];
    inputProperties: Array<DropdownValue> = [];
    inputPropertyTypes: {};

    inputParameters: Array<OperationParameter> = [];
    noAssignInputParameters: Array<OperationParameter> = [];
    assignInputParameters: { [key: string]: { [key: string]: Array<OperationParameter>; }; } = {};

    isAssociateWorkflow: boolean = false;
    isEditMode: boolean = false;
    isLoading: boolean = false;

    propertyTooltipText: String;

    constructor(private workflowServiceNg2: WorkflowServiceNg2, private translateService: TranslateService) {
        this.translateService.languageChangedObservable.subscribe(lang => {
            this.propertyTooltipText = this.translateService.translate("OPERATION_PROPERTY_TOOLTIP_TEXT");
        });
    }

    ngOnInit() {

        this.inputProperties = _.map(this.input.inputProperties,
            (input: InputModel) => new DropdownValue(input.uniqueId, input.name)
        );

        this.inputPropertyTypes = {};
        _.forEach(this.input.inputProperties, (input: InputModel) => {
            this.inputPropertyTypes[input.uniqueId] = input.type;
        });

        const inputOperation = <OperationModel>this.input.operation;
        this.operation = new OperationModel(inputOperation || {});

        const buildInputParams = () => {
            if (inputOperation.inputParams) {
                this.inputParameters = [];
                _.forEach(inputOperation.inputParams.listToscaDataDefinition, (input: OperationParameter) => {
                    this.addParam(input);
                });
            }
        }

        this.isLoading = true;
        this.workflowServiceNg2.getWorkflows().subscribe(workflows => {
            this.isLoading = false;

            this.workflows = _.map(workflows, (workflow: any) => {
                return new DropdownValue(workflow.id, workflow.name);
            });

            if (inputOperation) {
                if (inputOperation.workflowVersionId) {
                    this.isAssociateWorkflow = true;
                    this.onSelectWorkflow(inputOperation.workflowVersionId).add(buildInputParams);
                } else {
                    this.inputParameters = this.noAssignInputParameters;
                    this.isAssociateWorkflow = false;
                    buildInputParams();
                }

                if (inputOperation.uniqueId) {
                    this.isEditMode = true;
                }
            }
        });


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
                _.filter(versions, version => version.state === this.workflowServiceNg2.VERSION_STATE_CERTIFIED),
                (version: any) => {
                    if (!this.assignInputParameters[this.operation.workflowId][version.id]) {
                        this.assignInputParameters[this.operation.workflowId][version.id] = _.map(version.inputs, (input: any) => {
                            return new OperationParameter({
                                name: input.name,
                                type: input.type && input.type.toLowerCase(),
                                property: null,
                                mandatory: input.mandatory,
                            });
                        });
                    }
                    return new DropdownValue(version.id, `v. ${version.name}`);
                }
            );

            if (!selectedVersionId && versions.length) {
                this.operation.workflowVersionId = _.last(versions.sort()).id;
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
