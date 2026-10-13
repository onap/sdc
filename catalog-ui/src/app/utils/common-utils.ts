/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import { RelationshipModel } from 'app/models/graph/relationship';
import { PolicyInstance } from 'app/models/graph/zones/policy-instance';
import { InputBEModel } from 'app/models/properties-inputs/input-be-model';
import { PropertyBEModel } from 'app/models/properties-inputs/property-be-model';
import * as _ from 'lodash';
import { AttributeModel } from '../models/attributes';
import { AttributeBEModel } from '../models/attributes-outputs/attribute-be-model';
import { OutputBEModel } from '../models/attributes-outputs/output-be-model';
import { ResourceInstance } from '../models/components-instances/resource-instance';
import { GroupInstance } from '../models/graph/zones/group-instance';
import { Module } from '../models/modules/base-module';
import { OperationModel } from '../models/operation';
import { InterfaceModel } from '../models/operation';
import { PropertyModel } from '../models/properties';
import { InputFEModel } from '../models/properties-inputs/input-fe-model';
import { ComponentInstanceFactory } from './component-instance-factory';

export class CommonUtils {

    static initProperties(propertiesObj: PropertyModel[], uniqueId?: string): PropertyModel[] {

        const properties = new Array<PropertyModel>();
        if (propertiesObj) {
            propertiesObj.forEach((property: PropertyModel): void => {
                if (uniqueId) {
                    property.readonly = property.parentUniqueId != uniqueId;
                }
                properties.push(new PropertyModel(property));
            });
        }
        return properties;
    }

    static initAttributes(attributesObj: AttributeModel[], uniqueId?: string): AttributeModel[] {

        const attributes = new Array<AttributeModel>();
        if (attributesObj) {
            attributesObj.forEach((attribute: AttributeModel): void => {
                if (uniqueId) {
                    attribute.readonly = attribute.parentUniqueId != uniqueId;
                }
                attributes.push(new AttributeModel(attribute));
            });
        }
        return attributes;
    }

    static initComponentInstances(componentInstanceObj: ResourceInstance[]): ResourceInstance[] {

        const componentInstances = new Array<ResourceInstance>();
        if (componentInstanceObj) {
            componentInstanceObj.forEach((instance: ResourceInstance): void => {
                componentInstances.push(ComponentInstanceFactory.createComponentInstance(instance));
            });
        }
        return componentInstances;
    }

    static initModules(moduleArrayObj: Module[]): Module[] {

        const modules = new Array<Module>();

        if (moduleArrayObj) {
            moduleArrayObj.forEach((module: Module): void => {
                if (module.type === 'org.openecomp.groups.VfModule') {
                    modules.push(new Module(module));
                }
            });
        }
        return modules;
    }

    static initInputs(inputsObj: InputBEModel[]): InputBEModel[] {

        const inputs = new Array<InputBEModel>();

        if (inputsObj) {
            inputsObj.forEach((input: InputBEModel): void => {
                inputs.push(new InputBEModel(input));
            });
        }

        return inputs;
    }

    static initOutputs(outputsObj: OutputBEModel[]): OutputBEModel[] {

        const outputs = new Array<OutputBEModel>();

        if (outputsObj) {
            outputsObj.forEach((output: OutputBEModel): void => {
                outputs.push(new OutputBEModel(output));
            });
        }

        return outputs;
    }

    static initBeProperties(propertiesObj: PropertyBEModel[]): PropertyBEModel[] {

        const properties = new Array<PropertyBEModel>();

        if (propertiesObj) {
            propertiesObj.forEach((property: PropertyBEModel): void => {
                properties.push(new PropertyBEModel(property));
            });
        }

        return properties;
    }

    static initBeAttributes(attributesObj: AttributeBEModel[]): AttributeBEModel[] {

        const attributes = new Array<AttributeBEModel>();

        if (attributesObj) {
            attributesObj.forEach((attribute: AttributeBEModel): void => {
                attributes.push(new AttributeBEModel(attribute));
            });
        }

        return attributes;
    }

    static initComponentInstanceRelations = (componentInstanceRelationsObj: RelationshipModel[]): RelationshipModel[] => {
        if (componentInstanceRelationsObj) {
             const componentInstancesRelations: RelationshipModel[] = [];
             componentInstanceRelationsObj.forEach((instanceRelation: RelationshipModel): void => {
                componentInstancesRelations.push(new RelationshipModel(instanceRelation));
            });
             return componentInstancesRelations;
        }
    }

    static initPolicies = (policiesObj: PolicyInstance[]): PolicyInstance[] => {
        const policies = new Array<PolicyInstance>();

        if (policiesObj) {
            policiesObj.forEach((policy: PolicyInstance): void => {
                policies.push(new PolicyInstance(policy));
            });
        }

        return policies;
    }
    static initGroups = (groupsObj: GroupInstance[]): GroupInstance[] => {
        const groups = new Array<GroupInstance>();

        if (groupsObj) {
            groupsObj.forEach((group: GroupInstance): void => {
                groups.push(new GroupInstance(group));
            });
        }

        return groups;
    }

    static initInterfaces(interfaces: InterfaceModel[]): InterfaceModel[] {

        return _.map(interfaces, (interf: InterfaceModel) => {

            return new InterfaceModel({
                type: interf.type,
                uniqueId: interf.uniqueId,
                operations: _.map(interf.operations,
                    (operation: OperationModel) => {
                        const newOperation = new OperationModel(operation);
                        newOperation.interfaceType = interf.type;
                        newOperation.interfaceId = interf.uniqueId;

                        const {inputs, outputs, milestones} = operation;
                        if (inputs) {
                            newOperation.createInputsList(inputs.listToscaDataDefinition);
                        }
                        if (outputs) {
                            newOperation.createOutputsList(outputs.listToscaDataDefinition);
                        }
                        if (milestones) {
                            newOperation.milestones = milestones;
                        }

                        return newOperation;
                    }
                )
            });

        });
    }

    static initInterfaceOperations(interfaces: InterfaceModel[]): OperationModel[] {

        return _.reduce(interfaces, (acc, interf: InterfaceModel) => {

            return acc.concat(
                _.map(interf.operations,
                    (operation: OperationModel) => {
                        const newOperation = new OperationModel(operation);
                        newOperation.interfaceType = interf.type;
                        newOperation.interfaceId = interf.uniqueId;

                        const {inputs, outputs} = operation;
                        if (inputs) {
                            newOperation.createInputsList(inputs.listToscaDataDefinition);
                        }
                        if (outputs) {
                            newOperation.createOutputsList(outputs.listToscaDataDefinition);
                        }

                        return newOperation;
                    }
                )
            );

        }, []);
    }

}
