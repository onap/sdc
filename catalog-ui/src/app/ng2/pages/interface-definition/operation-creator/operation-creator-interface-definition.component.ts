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
import * as _ from "lodash";
import {Component, ViewChild} from '@angular/core';

import {TranslateService} from "app/ng2/shared/translator/translate.service";
import {WorkflowServiceNg2} from 'app/ng2/services/workflow.service';
import {
    Capability,
    InputBEModel,
    InterfaceModel,
    OperationModel,
    OperationParameter,
    WORKFLOW_ASSOCIATION_OPTIONS
} from 'app/models';

import {Tabs} from "app/ng2/components/ui/tabs/tabs.component";
import {
    DropdownValue
} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";
import {IDropDownOption} from 'onap-ui-angular';
import {DropDownComponent} from "onap-ui-angular/dist/components";
import {DROPDOWN_OPTION_TYPE} from "app/utils";
import {Subscription} from "rxjs";

export class DropDownOption implements IDropDownOption {
    value: string;
    label: string;

    constructor(value: string, label?: string) {
        this.value = value;
        this.label = label || value;
    }
}

class TypedDropDownOption extends DropDownOption {
    type: string;

    constructor(value: string, label?: string, type?: string) {
        super(value, label);
        this.type = type;
    }
}

export interface OperationCreatorInput {
    allWorkflows: Array<any>,
    inputOperation: OperationModel,
    interfaces: Array<InterfaceModel>,
    inputProperties: Array<InputBEModel>,
    enableWorkflowAssociation: boolean,
    readonly: boolean,
    interfaceTypes: { [interfaceType: string]: Array<string> },
    validityChangedCallback: Function,
    workflowIsOnline: boolean,
    capabilities: Array<Capability>
}

@Component({
    selector: 'operation-creator-interface-definition',
    templateUrl: './operation-creator-interface-definition.component.html',
    styleUrls: ['./operation-creator-interface-definition.component.less'],
    providers: [TranslateService]
})

export class OperationCreatorInterfaceDefinitionComponent implements OperationCreatorInput {

    input: OperationCreatorInput;
    inputOperation: OperationModel;
    interfaces: Array<InterfaceModel>;
    operation: OperationModel;
    interfaceNames: Array<TypedDropDownOption> = [];
    interfaceTypes: { [interfaceType: string]: Array<string> };
    operationNames: Array<TypedDropDownOption> = [];
    validityChangedCallback: Function;
    capabilities: Array<Capability>;

    allWorkflows: Array<any>;
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
    componentCapabilities: Array<Capability> = [];

    tableParameters: Array<OperationParameter> = [];
    operationOutputs: Array<OperationModel> = [];

    associationOptions: Array<DropdownValue> = [];
    workflowAssociationType: string;

    enableWorkflowAssociation: boolean;
    workflowIsOnline: boolean;
    isEditMode: boolean = false;
    isLoading: boolean = false;
    readonly: boolean;

    propertyTooltipText: String;

    TYPE_INPUT = 'Inputs';
    TYPE_OUTPUT = 'Outputs';

    INTERFACE_OTHER_HEADER = 'Local Interfaces';
    INTERFACE_OTHER = 'Local';

    @ViewChild('propertyInputTabs') propertyInputTabs: Tabs;
    @ViewChild('operationNamesDropdown') operationNamesDropdown: DropDownComponent;
    @ViewChild('workflowAssignmentDropdown') workflowAssignmentDropdown: DropDownComponent;
    currentTab: String;

    constructor(private workflowServiceNg2: WorkflowServiceNg2, private translateService: TranslateService) {
        this.translateService.languageChangedObservable.subscribe(lang => {
            this.propertyTooltipText = this.translateService.translate("OPERATION_PROPERTY_TOOLTIP_TEXT");

            this.associationOptions = [
                new DropDownOption(WORKFLOW_ASSOCIATION_OPTIONS.EXTERNAL, this.translateService.translate("EXTERNAL_WORKFLOW_ASSOCIATION")),
                new DropDownOption(WORKFLOW_ASSOCIATION_OPTIONS.EXISTING, this.translateService.translate("EXISTING_WORKFLOW_ASSOCIATION")),
            ];

            this.workflowAssociationType = this.operation.workflowAssociationType;
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
        this.interfaceNames.unshift(new TypedDropDownOption('Existing Interfaces', 'Existing Interfaces', DROPDOWN_OPTION_TYPE.HEADER));
        this.interfaceNames = this.interfaceNames.concat([
            new TypedDropDownOption(' ', ' ', DROPDOWN_OPTION_TYPE.HORIZONTAL_LINE),
            new TypedDropDownOption(this.INTERFACE_OTHER_HEADER, this.INTERFACE_OTHER_HEADER, DROPDOWN_OPTION_TYPE.HEADER),
            new TypedDropDownOption(this.INTERFACE_OTHER)
        ]);
        const inputOperation = this.inputOperation;
        this.operation = new OperationModel(inputOperation || {});

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
            if (this.workflowIsOnline) {
                this.workflows = _.map(
                    _.filter(
                        this.allWorkflows,
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
            } else {
                this.workflows = [];
            }
        }
        this.reconstructOperation();
        this.filterCapabilities();
        this.validityChanged();
        this.updateTable();
    }

    ngAfterViewInit() {
        if (this.workflowAssignmentDropdown) {
            this.workflowAssignmentDropdown.allOptions = this.associationOptions && this.associationOptions.length ?
                this.associationOptions :
                [
                    new DropDownOption(WORKFLOW_ASSOCIATION_OPTIONS.EXTERNAL, this.translateService.translate("EXTERNAL_WORKFLOW_ASSOCIATION")),
                    new DropDownOption(WORKFLOW_ASSOCIATION_OPTIONS.EXISTING, this.translateService.translate("EXISTING_WORKFLOW_ASSOCIATION")),
                ];
        }
    }

    reconstructOperation = () => {

        const buildAndUpdate = () => {
            this.buildParams();
            this.updateTable();
        };

        const inputOperation = this.inputOperation;
        if (inputOperation) {
            this.onSelectInterface(new DropDownOption(this.operation.interfaceType));

            if (this.enableWorkflowAssociation && inputOperation.workflowVersionId && this.isUsingExistingWF(inputOperation)) {
                this.assignInputParameters[this.operation.workflowId] = {[inputOperation.workflowVersionId]: []};
                this.assignOutputParameters[this.operation.workflowId] = {[inputOperation.workflowVersionId]: []};
                this.inputParameters = this.assignInputParameters[this.operation.workflowId][this.operation.workflowVersionId];
                this.outputParameters = this.assignOutputParameters[this.operation.workflowId][this.operation.workflowVersionId];

                const sub = this.onSelectWorkflow(new DropDownOption(inputOperation.workflowId), inputOperation.workflowVersionId);
                if (sub) {
                    sub.add(() => {
                        buildAndUpdate();
                        this.operation.workflowVersionId = '-1';
                        setTimeout(() => this.operation.workflowVersionId = this.inputOperation.workflowVersionId);
                    });
                } else {
                    buildAndUpdate();
                }
            } else {
                this.inputParameters = this.noAssignInputParameters;
                this.outputParameters = this.noAssignOutputParameters;
                buildAndUpdate();
            }

            if (inputOperation.uniqueId) {
                this.isEditMode = true;
            }
        }

    }

    filterCapabilities() {
        this.componentCapabilities = _.filter(this.capabilities, (cap: Capability) => cap.properties);
    }

    buildParams = () => {

        if (this.inputOperation.outputs) {
            this.currentTab = this.TYPE_OUTPUT;
            this.updateTable();
            _.forEach(
                [...this.inputOperation.outputs.listToscaDataDefinition].sort((a, b) => a.name.localeCompare(b.name)),
                (output: OperationParameter) => {
                    this.addParam({...output, required: Boolean(output.required)});
                }
            );
        }

        this.currentTab = this.TYPE_INPUT;
        this.updateTable();
        if (this.inputOperation.inputs) {
            _.forEach(
                [...this.inputOperation.inputs.listToscaDataDefinition].sort((a, b) => a.name.localeCompare(b.name)),
                (input: OperationParameter) => {
                    this.addParam({...input, required: Boolean(input.required)});
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
                    const ddType = (existingOp && existingOp.uniqueId !== this.operation.uniqueId) ? DROPDOWN_OPTION_TYPE.HORIZONTAL_LINE : DROPDOWN_OPTION_TYPE.SIMPLE;
                    return new TypedDropDownOption(name, name, ddType);
                }
            )
        );
        if (this.operationNamesDropdown) {
            this.operationNamesDropdown.allOptions = <IDropDownOption[]>this.operationNames;
        }
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
        this.operation.description = v || null;
        this.validityChanged();
    }

    onSelectWorkflow(workflowId: DropDownOption, selectedVersionId?: string): Subscription {

        if (_.isUndefined(workflowId) || !this.workflowIsOnline) {
            return;
        }

        if (this.operation.workflowId === workflowId.value && !selectedVersionId) {
            return;
        }

        this.operation.workflowId = workflowId.value;
        if (!this.assignInputParameters[this.operation.workflowId]) {
            this.assignInputParameters[this.operation.workflowId] = {};
            this.assignOutputParameters[this.operation.workflowId] = {};
        }
        this.operation.workflowName = workflowId.label;
        if (!this.assignInputParameters[this.operation.workflowName]) {
            this.assignInputParameters[this.operation.workflowName] = {};
            this.assignOutputParameters[this.operation.workflowName] = {};
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
                        this.assignInputParameters[this.operation.workflowId][version.id] = _.map(version.inputs, (input: any) => {
                            return new OperationParameter({
                                ...input,
                                type: input.type.toLowerCase(),
                                required: Boolean(input.mandatory)
                            });
                        })
                        .sort((a, b) => a.name.localeCompare(b.name));

                        this.assignOutputParameters[this.operation.workflowId][version.id] = _.map(version.outputs, (output: any) => {
                            return new OperationParameter({
                                ...output,
                                type: output.type.toLowerCase(),
                                required: Boolean(output.mandatory)
                            });
                        })
                        .sort((a, b) => a.name.localeCompare(b.name));
                    }
                    return new DropdownValue(version.id, `V ${version.name}`);
                }
            );
            if (!selectedVersionId && this.workflowVersions.length) {
                this.operation.workflowVersionId = _.last(this.workflowVersions).value;
                this.operation.workflowVersion = _.last(this.workflowVersions).label;
            }

            this.changeWorkflowVersion(new DropDownOption(this.operation.workflowVersionId));
            this.validityChanged();
        });

    }

    changeWorkflowVersion(versionId: DropDownOption) {

        if (_.isUndefined(versionId) || !this.workflowIsOnline) {
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

    onChangeArtifactFile(e: any) {
        const file = e.target.files && e.target.files[0];
        this.operation.artifactFileName = file && file.name;

        if (!this.operation.artifactFileName) {
            this.operation.artifactData = null;
            this.validityChanged();
            return;
        }

        const reader = new FileReader();
        reader.onloadend = () => {
            this.isLoading = false;
            const result = <String>reader.result;
            this.operation.artifactData = result.substring(result.indexOf(',') + 1);
            this.validityChanged();
        }

        this.isLoading = true;
        reader.readAsDataURL(file);
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
        this.tableParameters.push(new OperationParameter(param || {required: false}));
        this.validityChanged();
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

    isUsingExternalWF = (operation?: OperationModel): boolean => {
        operation = operation || this.operation;
        return operation.workflowAssociationType === WORKFLOW_ASSOCIATION_OPTIONS.EXTERNAL;
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

    getSelectedDropdown(options: DropdownValue[], selectedValue: string): DropdownValue {
        const selectedDropdown = _.find(options, (option) => option.value === selectedValue);
        return selectedDropdown || this.toDropDownOption(null);
    }

    toDropDownOption(val: string) {
        return {value: val, label: val};
    }
}
