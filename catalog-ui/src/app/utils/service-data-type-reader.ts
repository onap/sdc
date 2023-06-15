/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import {DataTypeModel, PropertyBEModel} from "../models";
import {Constraint, ConstraintTypes} from "../ng2/pages/properties-assignment/constraints/constraints.component";
import {load} from 'js-yaml';

export class ServiceDataTypeReader {

    private serviceDataType = new DataTypeModel();

    public read(dataTypeFile: File): Promise<DataTypeModel> {

        return new Promise<DataTypeModel>((resolve, reject) => {
            const reader = new FileReader();
            reader.onloadend = () => {
                try {
                    const result = <String>reader.result;
                    const loadedContent = load(result);
                    console.log("Readed content: " + loadedContent);
                    const dataType = this.getDataType(loadedContent);
                    this.readName(dataType);
                    this.readDerivedFrom(dataType);
                    this.readDescription(dataType);
                    this.readProperties(dataType);
                    resolve(this.serviceDataType);
                } catch (error) {
                    reject(error);
                }
            }
            reader.readAsText(dataTypeFile);
        });
    }

    private getDataType(fileContent:any) {
        const index = Object.keys(fileContent).indexOf("data_types",0)
        if (index == -1){
            return fileContent;
        }
        return fileContent["data_types"];
    }

    private readName(fileContent: any) {
        this.serviceDataType.name = Object.keys(fileContent).values().next().value;
    }

    private readDerivedFrom(fileContent: any) {
        let dataType = Object.keys(fileContent).values().next().value;
        this.serviceDataType.derivedFromName = fileContent[dataType]["derived_from"];
    }

    private readDescription(fileContent: any) {
        let dataType = Object.keys(fileContent).values().next().value;
        this.serviceDataType.description = fileContent[dataType]["description"];
    }

    private readProperties(fileContent: any) {
        this.serviceDataType.properties = new Array<PropertyBEModel>();
        let dataType = Object.keys(fileContent).values().next().value;
        const properties = fileContent[dataType]["properties"];
        Object.keys(properties).forEach((key )=>
            {
                let property = new PropertyBEModel();
                property.name = key;
                property.description = properties[key]["description"];
                property.type = properties[key]["type"];
                property.schemaType = properties[key]["schema"];
                property.required = properties[key]["required"];
                const constraints = properties[key]["constraints"];

                if (constraints) {
                    property.constraints = new Array();
                    let constraintArray = new Array();
                    Object.keys(constraints).forEach((constrainKey) => {
                        Object.keys(constraints[constrainKey]).forEach((kc) => {
                            let newConstraint = this.mapValuesToConstraint(<ConstraintTypes>kc, constraints[constrainKey][kc]);
                            let jsonObject = this.getConstraintFormat(newConstraint);
                            constraintArray.push(jsonObject);

                        });
                    });
                    property.constraints.push(constraintArray);
                }
                this.serviceDataType.properties.push(property);
            }
        );
    }

    private getConstraintFormat(constraint: Constraint): any {
        switch (constraint.type) {
            case ConstraintTypes.equal:
                return {
                    [ConstraintTypes.equal]: constraint.value
                }
            case ConstraintTypes.less_or_equal:
                return {
                    [ConstraintTypes.less_or_equal]: constraint.value
                }
            case ConstraintTypes.less_than:
                return {
                    [ConstraintTypes.less_than]: constraint.value
                }
            case ConstraintTypes.greater_or_equal:
                return {
                    [ConstraintTypes.greater_or_equal]: constraint.value
                }
            case ConstraintTypes.greater_than:
                return {
                    [ConstraintTypes.greater_than]: constraint.value
                }
            case ConstraintTypes.in_range:
                return {
                    [ConstraintTypes.in_range]: constraint.value
                }
            case ConstraintTypes.length:
                return {
                    [ConstraintTypes.length]: constraint.value
                }
            case ConstraintTypes.max_length:
                return {
                    [ConstraintTypes.max_length]: constraint.value
                }
            case ConstraintTypes.min_length:
                return {
                    [ConstraintTypes.min_length]: constraint.value
                }
            case ConstraintTypes.pattern:
                return {
                    [ConstraintTypes.pattern]: constraint.value
                }
            case ConstraintTypes.valid_values:
                return {
                    [ConstraintTypes.valid_values]: constraint.value
                }
            default:
                return;
        }
    }

    private mapValuesToConstraint(type: string, value: any):Constraint {
        let constraintType: ConstraintTypes;
        let constraintValue: any;
        if (!type) {
            constraintType = ConstraintTypes.null;
            constraintValue = "";
        } else if(type === "valid_values"){
            constraintType = ConstraintTypes.valid_values;
            constraintValue = value;
        } else if(type === "equal") {
            constraintType = ConstraintTypes.equal;
            constraintValue = value;
        } else if(type === "greater_than") {
            constraintType = ConstraintTypes.greater_than;
            constraintValue = value;
        } else if(type === "greater_or_equal") {
            constraintType = ConstraintTypes.greater_or_equal;
            constraintValue = value;
        } else if(type === "less_than") {
            constraintType = ConstraintTypes.less_than;
            constraintValue = value;
        } else if(type === "less_or_equal") {
            constraintType = ConstraintTypes.less_or_equal;
            constraintValue = value;
        } else if(type === "in_range") {
            constraintType = ConstraintTypes.in_range;
            constraintValue = value;
        } else if(type === "range_max_value" || type === "range_min_value") {
            constraintType = ConstraintTypes.in_range;
            constraintValue = value;
        } else if(type === "length") {
            constraintType = ConstraintTypes.length;
            constraintValue = value;
        } else if(type === "min_length") {
            constraintType = ConstraintTypes.min_length;
            constraintValue = value;
        } else if(type === "max_length") {
            constraintType = ConstraintTypes.max_length;
            constraintValue = value;
        } else if(type === "pattern") {
            constraintType = ConstraintTypes.pattern;
            constraintValue = value;
        }
        return {
            type:constraintType,
            value:constraintValue
        }
    }
}
