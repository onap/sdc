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
import {Component, Input} from '@angular/core';
import {DataTypeService} from "app/ng2/services/data-type.service";
import {
    Capability,
    DataTypeModel,
    InputBEModel,
    OperationModel,
    OperationParameter
} from 'app/models';
import {
    DropdownValue
} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";
import {WorkspaceService} from "../../../workspace/workspace.service";

class DropdownValueType extends DropdownValue {
    type: string;

    constructor(value: string, label: string, type?: string) {
        super(value, label);
        if (type) {
            this.type = type;
        }
    }
}

@Component({
    selector: 'param-row',
    templateUrl: './param-row.component.html',
    styleUrls: ['./param-row.component.less']
})

export class ParamRowComponent {
    @Input() param: OperationParameter;
    @Input() inputProps: Array<InputBEModel>;
    @Input() operationOutputs: Array<OperationModel>;
    @Input() capabilitiesProps: Array<Capability>;
    @Input() onRemoveParam: Function;
    @Input() isAssociateWorkflow: boolean;
    @Input() readonly: boolean;
    @Input() isInputParam: boolean;
    @Input() validityChanged: Function;

    propTypeEnum: Array<string> = [];
    operationOutputCats: Array<{ operationName: string, outputs: Array<DropdownValueType> }> = [];
    filteredInputProps: Array<DropdownValue> = [];
    filteredCapabilitiesProps: Array<{ capabilityName: string, properties: Array<DropdownValueType> }> = [];

    constructor(private dataTypeService: DataTypeService, protected workspaceService: WorkspaceService) {
    }

    ngOnInit() {
        if (this.isInputParam) {
            this.propTypeEnum = _.uniq(
                _.map(
                    _.concat(
                        this.getPrimitiveSubtypes(),
                        _.reduce(
                            this.operationOutputs,
                            (acc, op) => [...acc, ...op.outputs.listToscaDataDefinition],
                            []),
                        _.reduce(
                            this.capabilitiesProps,
                            (acc, capab) => [...acc, ...capab.properties],
                            [])
                    ),
                    prop => prop.type
                )
            );
        } else {
            const dataTypes: Array<DataTypeModel> = _.toArray(this.dataTypeService.getDataTypeByModel(this.workspaceService.metadata.model));
            this.propTypeEnum = _.concat(
                _.map(
                    _.filter(
                        dataTypes,
                        type => this.isTypePrimitive(type.name)
                    ),
                    type => type.name
                ).sort(),
                _.map(
                    _.filter(
                        dataTypes,
                        type => !this.isTypePrimitive(type.name)
                    ),
                    type => type.name
                ).sort()
            );
        }

        this.onChangeType();
        this.validityChanged();
    }

    onChangeName() {
        this.validityChanged();
    }

    onChangeType() {
        if (!this.isInputParam) {
            this.validityChanged();
            return;
        }

        this.filteredInputProps = _.map(
            _.filter(
                this.getPrimitiveSubtypes(),
                prop => !this.param.type || prop.type === this.param.type
            ),
            prop => new DropdownValue(prop.uniqueId, prop.name)
        );
        this.filteredInputProps.unshift(new DropdownValue("", ""));

        this.operationOutputCats = _.filter(
            _.map(
                this.operationOutputs,
                op => {
                    return {
                        operationName: `${op.displayType()}.${op.name}`,
                        outputs: _.map(
                            _.filter(op.outputs.listToscaDataDefinition, output => !this.param.type || output.type === this.param.type),
                            output => new DropdownValueType(
                                `${op.interfaceType}.${op.name}.${output.name}`,
                                output.name,
                                output.type
                            )
                        )
                    };
                }
            ),
            category => category.outputs.length > 0
        );

        this.filteredCapabilitiesProps = _.filter(
            _.map(
                this.capabilitiesProps,
                cap => {
                    return {
                        capabilityName: cap.name,
                        properties: _.map(
                            _.filter(cap.properties, prop => !this.param.type || prop.type === this.param.type),
                            prop => new DropdownValueType(
                                prop.uniqueId,
                                prop.name,
                                prop.type
                            )
                        )
                    };
                }
            ),
            capability => capability.properties.length > 0
        );

        if (this.param.inputId) {
            const selProp = this.getSelectedProp();
            if (selProp && selProp.type === this.param.type) {
                this.param.inputId = '-1';
                setTimeout(() => this.param.inputId = selProp.uniqueId || selProp.value);
            } else {
                this.param.inputId = null;
            }
        }

        this.validityChanged();
    }

    onChangeProperty() {
        const newProp = this.getSelectedProp();

        if (!this.param.type) {
            this.param.type = newProp.type;
            this.onChangeType();
        }

        if (!this.param.name) {
            this.param.name = newProp.name || newProp.label;
        }

        this.validityChanged();
    }

    getPrimitiveSubtypes(): Array<InputBEModel> {
        const flattenedProps: Array<any> = [];
        const dataTypes = this.dataTypeService.getDataTypeByModel(this.workspaceService.metadata.model);

        _.forEach(this.inputProps, prop => {
            const type: DataTypeModel = _.find(
                _.toArray(dataTypes),
                (type: DataTypeModel) => type.name === prop.type
            );
            flattenedProps.push(prop);
            if (!type) {
                console.error('Could not find prop in dataTypes: ', prop);
            } else {
                if (type.properties) {
                    _.forEach(type.properties, subType => {
                        if (this.isTypePrimitive(subType.type)) {
                            flattenedProps.push({
                                type: subType.type,
                                name: `${prop.name}.${subType.name}`,
                                uniqueId: `${prop.uniqueId}.${subType.name}`
                            });
                        }
                    });
                }
            }
        });

        return flattenedProps;
    }

    getSelectedProp() {
        return _.find(
            this.getPrimitiveSubtypes(),
            prop => this.param.inputId === prop.uniqueId
        ) || _.find(
            _.reduce(
                this.operationOutputCats,
                (acc, cat) => [...acc, ...cat.outputs],
                []),
            (out: DropdownValueType) => this.param.inputId === out.value
        ) || _.find(
            _.reduce(
                this.filteredCapabilitiesProps,
                (acc, cap) => [...acc, ...cap.properties],
                []),
            (prop: DropdownValueType) => this.param.inputId === prop.value
        );
    }

    isTypePrimitive(type: string): boolean {
        return (
            type === 'string' ||
            type === 'integer' ||
            type === 'float' ||
            type === 'boolean'
        );
    }
}
