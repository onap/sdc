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

import {Module, AttributeModel, ResourceInstance, PropertyModel, InputFEModel} from "../models";
import {ComponentInstanceFactory} from "./component-instance-factory";
import {InputBEModel, PropertyBEModel, RelationshipModel} from "app/models";

export class CommonUtils {

    static initProperties(propertiesObj:Array<PropertyModel>, uniqueId?:string):Array<PropertyModel> {

        let properties = new Array<PropertyModel>();
        if (propertiesObj) {
            _.forEach(propertiesObj, (property:PropertyModel):void => {
                if (uniqueId) {
                    property.readonly = property.parentUniqueId != uniqueId;
                }
                properties.push(new PropertyModel(property));
            });
        }
        return properties;
    };

    static initAttributes(attributesObj:Array<AttributeModel>, uniqueId?:string):Array<AttributeModel> {

        let attributes = new Array<AttributeModel>();
        if (attributesObj) {
            _.forEach(attributesObj, (attribute:AttributeModel):void => {
                if (uniqueId) {
                    attribute.readonly = attribute.parentUniqueId != uniqueId;
                }
                attributes.push(new AttributeModel(attribute));
            });
        }
        return attributes;
    };

    static initComponentInstances(componentInstanceObj:Array<ResourceInstance>):Array<ResourceInstance> {

        let componentInstances = new Array<ResourceInstance>();
        if (componentInstanceObj) {
            _.forEach(componentInstanceObj, (instance:ResourceInstance):void => {
                componentInstances.push(ComponentInstanceFactory.createComponentInstance(instance));
            });
        }
        return componentInstances;
    };

    static initModules(moduleArrayObj:Array<Module>):Array<Module> {

        let modules = new Array<Module>();

        if (moduleArrayObj) {
            _.forEach(moduleArrayObj, (module:Module):void => {
                if (module.type === "org.openecomp.groups.VfModule") {
                    modules.push(new Module(module));
                }
            });
        }
        return modules;
    };

    static initInputs(inputsObj: Array<InputBEModel>): Array<InputBEModel> {

        let inputs = new Array<InputBEModel>();

        if(inputsObj) {
            _.forEach(inputsObj, (input: InputBEModel):void => {
                inputs.push(new InputBEModel(input));
            })
        }

        return inputs;
    }

    static initBeProperties(propertiesObj: Array<PropertyBEModel>): Array<PropertyBEModel> {

        let properties = new Array<PropertyBEModel>();

        if (propertiesObj) {
            _.forEach(propertiesObj, (property: PropertyBEModel): void => {
                properties.push(new PropertyBEModel(property));
            })
        }

        return properties;
    }

    static initComponentInstanceRelations = (componentInstanceRelationsObj:Array<RelationshipModel>):Array<RelationshipModel> => {
        if (componentInstanceRelationsObj) {
             let componentInstancesRelations: Array<RelationshipModel> = [];
            _.forEach(componentInstanceRelationsObj, (instanceRelation:RelationshipModel):void => {
                componentInstancesRelations.push(new RelationshipModel(instanceRelation));
            });
            return componentInstancesRelations;
        }
    };
}

