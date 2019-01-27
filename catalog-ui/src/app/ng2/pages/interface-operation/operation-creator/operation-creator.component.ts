import * as _ from "lodash";
import {Component, ViewChild} from '@angular/core';

import {Subscription} from "rxjs/Subscription";

import {TranslateService} from "app/ng2/shared/translator/translate.service";
import {WorkflowServiceNg2} from 'app/ng2/services/workflow.service';
import {InterfaceModel, OperationModel, OperationParameter, InputBEModel, RadioButtonModel, WORKFLOW_ASSOCIATION_OPTIONS} from 'app/models';

import {IDropDownOption} from "sdc-ui/lib/angular/form-elements/dropdown/dropdown-models";
import {Tabs, Tab} from "app/ng2/components/ui/tabs/tabs.component";
import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";

export class DropDownOption implements IDropDownOption {
    value: string;
    label: string;

    constructor(value: string, label?: string) {
        this.value = value;
        this.label = label || value;
    }
}

class TypedDropDownOption extends DropDownOption {
    type: number;

    constructor(value: string, label?: string, type?: number) {
        super(value, label);
        this.type = type;
    }
}

export interface OperationCreatorInput {
    inputOperation: OperationModel,
    interfaces: Array<InterfaceModel>,
    inputProperties: Array<InputBEModel>,
    enableWorkflowAssociation: boolean,
    readonly: boolean,
    isService: boolean,
    interfaceTypes: { [interfaceType: string]: Array<string> },
    validityChangedCallback: Function
}

@Component({
    selector: 'operation-creator',
    templateUrl: './operation-creator.component.html',
    styleUrls: ['./operation-creator.component.less'],
    providers: [TranslateService]
})

export class OperationCreatorComponent {

    input: OperationCreatorInput;
    inputOperation: OperationModel;
    interfaces: Array<InterfaceModel>;
    operation: OperationModel;
    interfaceNames: Array<TypedDropDownOption> = [];
    interfaceTypes: { [interfaceType: string]: Array<string> };
    operationNames: Array<TypedDropDownOption> = [];
    validityChangedCallback: Function;

    workflows: Array<DropdownValue> = [];
    workflowVersions: Array<DropdownValue> = [];
    inputProperties: Array<InputBEModel> = [];
    archivedWorkflowId: string = '&';

    inputParameters: Array<OperationParameter> = [];
    noAssignInputParameters: Array<OperationParameter> = [];
    assignInputParameters: { [key: string]: { [key: string]: Array<OperationParameter>; }; } = {};

    outputParameters: Array<OperationParameter> = [];
    noAssignOutputParameters: Array<OperationParameter> = [];
    assignOutputParameters: { [key: string]: { [key: string]: Array<OperationParameter>; }; } = {};

    tableParameters: Array<OperationParameter> = [];
    operationOutputs: Array<OperationModel> = [];

    associationOptions: Array<DropdownValue> = [];
    workflowAssociationType: string;

    enableWorkflowAssociation: boolean;
    isEditMode: boolean = false;
    isLoading: boolean = false;
    readonly: boolean;

    propertyTooltipText: String;

    TYPE_INPUT = 'Inputs';
    TYPE_OUTPUT = 'Outputs';

    INTERFACE_OTHER_HEADER = 'Local Interfaces';
    INTERFACE_OTHER = 'Local';

    @ViewChild('propertyInputTabs') propertyInputTabs: Tabs;
    currentTab: String;

    constructor(private workflowServiceNg2: WorkflowServiceNg2, private translateService: TranslateService) {
        this.translateService.languageChangedObservable.subscribe(lang => {
            this.propertyTooltipText = this.translateService.translate("OPERATION_PROPERTY_TOOLTIP_TEXT");

            this.associationOptions = [
                new DropDownOption(WORKFLOW_ASSOCIATION_OPTIONS.NONE, this.translateService.translate("NO_WORKFLOW_ASSOCIATION")),
                new DropDownOption(WORKFLOW_ASSOCIATION_OPTIONS.EXISTING, this.translateService.translate("EXISTING_WORKFLOW_ASSOCIATION"))
            ];

            this.workflowAssociationType = this.operation.workflowAssociationType || WORKFLOW_ASSOCIATION_OPTIONS.NONE;
        });

        this.currentTab = this.TYPE_INPUT;
    }

    createInterfaceDropdown(type: string) {
        let label = type;
        const lastDot = label.lastIndexOf('.');
        if (lastDot > -1) {
            label = label.substr(lastDot + 1);
        }
        return new TypedDropDownOption(type, label);
    }

    ngOnInit() {
        this.interfaceNames = _.map(
            _.keys(this.interfaceTypes),
            type => this.createInterfaceDropdown(type)
        );
        this.interfaceNames.unshift(new TypedDropDownOption('Existing Interfaces', 'Existing Interfaces', 1));
        this.interfaceNames = this.interfaceNames.concat([
            new TypedDropDownOption(' ', ' ', 3),
            new TypedDropDownOption(this.INTERFACE_OTHER_HEADER, this.INTERFACE_OTHER_HEADER, 1),
            new TypedDropDownOption(this.INTERFACE_OTHER)
        ]);

        const inputOperation = this.inputOperation;
        this.operation = new OperationModel(inputOperation || {});
        this.onSelectInterface(new DropDownOption(this.operation.interfaceType));
        this.validityChanged();

        this.operationOutputs = _.reduce(
            this.interfaces,
            (acc: Array<OperationModel>, interf) => [
                ...acc,
                ..._.filter(
                    interf.operations,
                    op => op.uniqueId !== this.operation.uniqueId
                ),
            ],
        []);

        if (this.enableWorkflowAssociation) {
            this.isLoading = true;
            this.workflowServiceNg2.getWorkflows().subscribe(workflows => {
                this.isLoading = false;
                this.workflows = _.map(
                    _.filter(
                        workflows,
                        (workflow: any) => {
                            if (workflow.archiving === this.workflowServiceNg2.WF_STATE_ACTIVE) {
                                return true;
                            }
                            if (workflow.archiving === this.workflowServiceNg2.WF_STATE_ARCHIVED &&
                                workflow.id === this.operation.workflowId) {
                                this.archivedWorkflowId = workflow.id;
                                return true;
                            }
                            return false;
                        }
                    ),
                    (workflow: any) => new DropdownValue(workflow.id, workflow.name)
                );
                this.reconstructOperation();
            });
        } else {
            this.reconstructOperation();
        }
    }

    reconstructOperation = () => {
        const inputOperation = this.inputOperation;
        if (inputOperation) {
            if (this.enableWorkflowAssociation && inputOperation.workflowVersionId && this.isUsingExistingWF(inputOperation)) {
                const sub = this.onSelectWorkflow(new DropDownOption(inputOperation.workflowId), inputOperation.workflowVersionId);
                if (sub) {
                    sub.add(() => {
                        this.buildParams();
                        this.updateTable();
                    });
                }
            } else {
                this.inputParameters = this.noAssignInputParameters;
                this.outputParameters = this.noAssignOutputParameters;
                this.buildParams();
                this.updateTable();
            }

            if (inputOperation.uniqueId) {
                this.isEditMode = true;
            }
        }
        this.updateTable();
        this.validityChanged();
    }

    buildParams = () => {
        if (this.inputOperation.outputs) {
            this.currentTab = this.TYPE_OUTPUT;
            this.updateTable();
            _.forEach(
                [...this.inputOperation.outputs.listToscaDataDefinition].sort((a, b) => a.name.localeCompare(b.name)),
                (output: OperationParameter) => {
                    this.addParam(output);
                }
            );
        }
        this.currentTab = this.TYPE_INPUT;
        this.updateTable();
        if (this.inputOperation.inputs) {
            _.forEach(
                [...this.inputOperation.inputs.listToscaDataDefinition].sort((a, b) => a.name.localeCompare(b.name)),
                (input: OperationParameter) => {
                    this.addParam(input);
                }
            );
        }
    }

    isInterfaceOther(): boolean {
        return this.operation.interfaceType === this.INTERFACE_OTHER;
    }

    onSelectInterface(interf: IDropDownOption) {
        if (interf && this.operation.interfaceType !== interf.value) {
            this.operation.name = null;
        }
        this.operation.interfaceType = interf && interf.value;
        this.operationNames = !this.operation.interfaceType ? [] : (
            _.map(
                this.interfaceTypes[this.operation.interfaceType],
                name => {
                    const curInterf = _.find(
                        this.interfaces,
                        interf => interf.type === this.operation.interfaceType
                    );
                    const existingOp = _.find(
                        curInterf && curInterf.operations || [],
                        op => op.name === name
                    );
                    const ddType = (existingOp && existingOp.uniqueId !== this.operation.uniqueId) ? 2 : 0;
                    return new TypedDropDownOption(name, name, ddType);
                }
            )
        );
        this.validityChanged();
    }

    onSelectOperationName(name: IDropDownOption) {
        if (name) {
            this.operation.name = name.value;
        }
        this.validityChanged();
    }

    onChangeName() {
        this.validityChanged();
    }

    get descriptionValue() {
        return this.operation.description;
    }

    set descriptionValue(v) {
        this.operation.description = v;
        this.validityChanged();
    }

    onSelectWorkflow(workflowId: DropDownOption, selectedVersionId?: string): Subscription {

        if (_.isUndefined(workflowId) || workflowId.value === this.operation.workflowId) {
            return;
        }
        this.operation.workflowId = workflowId.value;
        if (!this.assignInputParameters[this.operation.workflowId]) {
            this.assignInputParameters[this.operation.workflowId] = {};
            this.assignOutputParameters[this.operation.workflowId] = {};
        }

        this.isLoading = true;
        this.validityChanged();
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

            this.changeWorkflowVersion(new DropDownOption(this.operation.workflowVersionId));
            this.validityChanged();
        });

    }

    changeWorkflowVersion(versionId: DropDownOption) {

        if (_.isUndefined(versionId)) {
            return;
        }

        this.operation.workflowVersionId = versionId.value;
        this.inputParameters = this.assignInputParameters[this.operation.workflowId][this.operation.workflowVersionId];
        this.outputParameters = this.assignOutputParameters[this.operation.workflowId][this.operation.workflowVersionId];
        this.updateTable();
        this.validityChanged();

    }

    toggleAssociateWorkflow(type: DropDownOption) {

        if (_.isUndefined(type)) {
            return;
        }

        this.operation.workflowAssociationType = type.value;
        this.workflowAssociationType = this.operation.workflowAssociationType;

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
        this.validityChanged();

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
        this.validityChanged();
        this.tableParameters.push(new OperationParameter(param));
    }

    canAdd = (): boolean => {

        let valid = true;
        if (this.currentTab === this.TYPE_INPUT) {
            _.forEach(this.inputParameters, param => {
                if (!param.name || !param.inputId) {
                    valid = false;
                }
            });
        } else {
            _.forEach(this.outputParameters, param => {
                if (!param.name || !param.type) {
                    valid = false;
                }
            });
        }

        return valid;

    }

    isParamsValid = (): boolean => {

        let valid = true;
        _.forEach(this.inputParameters, param => {
            if (!param.name || !param.inputId) {
                valid = false;
            }
        });
        _.forEach(this.outputParameters, param => {
            if (!param.name || !param.type) {
                valid = false;
            }
        });

        return valid;

    }

    onRemoveParam = (param: OperationParameter): void => {
        let index = _.indexOf(this.tableParameters, param);
        this.tableParameters.splice(index, 1);
        this.validityChanged();
    }

    createParamLists = () => {
        this.operation.createInputsList(this.inputParameters);
        this.operation.createOutputsList(this.outputParameters);
    }

    isUsingExistingWF = (operation?: OperationModel): boolean => {
        operation = operation || this.operation;
        return operation.workflowAssociationType === WORKFLOW_ASSOCIATION_OPTIONS.EXISTING;
    }

    shouldCreateWF = (operation?: OperationModel): boolean => {
        operation = operation || this.operation;
        return operation.workflowAssociationType === WORKFLOW_ASSOCIATION_OPTIONS.NEW;
    }

    checkFormValidForSubmit = (): boolean => {
        return this.operation.name &&
            (!this.isUsingExistingWF() || this.operation.workflowVersionId) &&
            this.isParamsValid();
    }

    validityChanged = () => {
        let validState = this.checkFormValidForSubmit();
        this.validityChangedCallback(validState);
    }

}
