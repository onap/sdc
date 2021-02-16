/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 Nordix Foundation. All rights reserved.
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

import {Injectable} from '@angular/core';
import {AttributeFEModel} from "../../models/attributes-outputs/attribute-fe-model";
import {AttributeBEModel} from "app/models/attributes-outputs/attribute-be-model";
import {DerivedFEAttribute} from "../../models/attributes-outputs/derived-fe-attribute";
import {AttributeDeclareAPIModel} from "app/models/attributes-outputs/attribute-declare-api-model";

@Injectable()
export class AttributesService {

  constructor() {
  }

  public getParentAttributeFEModelFromPath = (attributes: Array<AttributeFEModel>, path: string) => {
    let parent: AttributeFEModel = attributes.find((property: AttributeFEModel): boolean => {
      return property.name === path.substring(0, path.indexOf('#'));
    });
    return parent;
  }

  //undo disabling of parent and child props=
  public undoDisableRelatedAttributes = (property: AttributeFEModel, childPath?: string): void => {
    property.isDisabled = false;
    if (!childPath) {
      property.isSelected = false;
      property.flattenedChildren && property.flattenedChildren.map(child => child.isDisabled = false);
    } else { //QND - unselect everything and then re-do the disabling of declared props. TODO: put a flag on propertyFEModel instead to indicate who's causing them to be disabled instead
      property.flattenedChildren.filter(child => child.isDisabled && !child.isDeclared).forEach(child => child.isDisabled = false);
      property.flattenedChildren.filter(child => child.isDeclared || child.isSelected).forEach((childProp) => { //handle brothers who are selected - redo their disabled relatives as well
        this.disableRelatedAttributes(property, childProp.attributesName);
      });
    }
  }

  //disable parents and children of prop
  public disableRelatedAttributes = (property: AttributeFEModel, childPath?: string): void => {
    if (!childPath) { //selecting the parent property
      property.isSelected = true;
      property.flattenedChildren && property.flattenedChildren.map(child => {
        child.isSelected = false;
        child.isDisabled = true;
      });
    } else {
      property.isSelected = false;
      property.isDisabled = true;
      property.flattenedChildren.filter((childProp: DerivedFEAttribute) => {
        return (childProp.attributesName.indexOf(childPath + "#") === 0 //is child of prop to disable
            || childPath.indexOf(childProp.attributesName + "#") === 0); //is parent of prop to disable
      }).forEach((child: DerivedFEAttribute) => {
        child.isSelected = false;
        child.isDisabled = true;
      });
    }
  }

  public getCheckedAttributes = (attributes: Array<AttributeFEModel>): Array<AttributeBEModel> => {
    let selectedProps: Array<AttributeDeclareAPIModel> = [];
    attributes.forEach(attrib => {
      if (attrib.isSelected && !attrib.isDeclared && !attrib.isDisabled) {
        selectedProps.push(new AttributeDeclareAPIModel(attrib));
      } else if (attrib.flattenedChildren) {
        attrib.flattenedChildren.forEach((child) => {
          if (child.isSelected && !child.isDeclared && !child.isDisabled) {
            let childProp = new AttributeDeclareAPIModel(attrib, child); //create it from the parent
            selectedProps.push(childProp);
          }
        })
      }
    });
    return selectedProps;
  }

}
