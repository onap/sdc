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

import { Injectable } from '@angular/core';
import { PropertyFEModel, PropertyBEModel, PropertyDeclareAPIModel, DerivedFEProperty} from "app/models";

@Injectable()
export class PropertiesService {

    constructor() {
    }

    public getParentPropertyFEModelFromPath = (properties: Array<PropertyFEModel>, path: string) => {
        let parent: PropertyFEModel = _.find(properties, (property: PropertyFEModel): boolean => {
            return property.name === path.substring(0, path.indexOf('#'));
        });
        return parent;
    }

    //undo disabling of parent and child props=
    public undoDisableRelatedProperties = (property: PropertyFEModel, childPath?: string): void => {
        property.isDisabled = false;
        if (!childPath) {
            property.isSelected = false;
            property.flattenedChildren && property.flattenedChildren.map(child => child.isDisabled = false);
        } else { //QND - unselect everything and then re-do the disabling of declared props. TODO: put a flag on propertyFEModel instead to indicate who's causing them to be disabled instead
            property.flattenedChildren.filter(child => child.isDisabled && !child.isDeclared).map(child => child.isDisabled = false);
            property.flattenedChildren.filter(child => child.isDeclared || child.isSelected).forEach((childProp) => { //handle brothers who are selected - redo their disabled relatives as well
                this.disableRelatedProperties(property, childProp.propertiesName);
            });
        }
    }

    //disable parents and children of prop
    public disableRelatedProperties = (property: PropertyFEModel, childPath?: string): void => {
        if (!childPath) { //selecting the parent property
            property.isSelected = true;
            property.flattenedChildren && property.flattenedChildren.map(child => { child.isSelected = false; child.isDisabled = true; });
        } else {
            property.isSelected = false;
            property.isDisabled = true;
            property.flattenedChildren.filter((childProp: DerivedFEProperty) => {
                return (childProp.propertiesName.indexOf(childPath + "#") === 0 //is child of prop to disable
                    || childPath.indexOf(childProp.propertiesName + "#") === 0); //is parent of prop to disable
            }).map((child: DerivedFEProperty) => { child.isSelected = false; child.isDisabled = true; });
        }
    }

    public getCheckedProperties = (properties: Array<PropertyFEModel>): Array<PropertyBEModel> => {
        let selectedProps: Array<PropertyDeclareAPIModel> = [];
        properties.forEach(prop => {
            if (prop.isSelected && !prop.isDeclared && !prop.isDisabled) {
                selectedProps.push(new PropertyDeclareAPIModel(prop));
            } else if (prop.flattenedChildren) {
                prop.flattenedChildren.forEach((child) => {
                    if (child.isSelected && !child.isDeclared && !child.isDisabled) {
                        let childProp = new PropertyDeclareAPIModel(prop, child); //create it from the parent
                        selectedProps.push(childProp);
                    }
                })
            }
        });
        return selectedProps;
    }


}
