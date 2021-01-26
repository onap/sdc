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
import {AttributeFEModel} from "../../../../models/attributes-outputs/attribute-fe-model";
import {SimpleFlatAttribute} from "app/models/attributes-outputs/simple-flat-attribute";
import {DerivedFEAttribute} from "../../../../models/attributes-outputs/derived-fe-attribute";

@Injectable()
export class HierarchyNavService {
  /**
   * Build hierarchy structure for the tree when user selects on table row.
   * First create Array<SimpleFlatAttribute> and insert also the parent (AttributeFEModel) to this array.
   * The Array is flat and contains SimpleFlatAttribute that has parentName and uniqueId.
   * Now we build hierarchy from this Array (that includes children) and return it for the tree
   *
   * @argument attribute: AttributeFEModel - attribute contains flattenedChildren array of DerivedFEAttribute
   * @returns  Array<SimpleFlatAttribute> - containing children Array<SimpleFlatAttribute>, augmantin children to SimpleFlatAttribute.
   */
  public getSimpleAttributesTree(attribute: AttributeFEModel, instanceName: string): Array<SimpleFlatAttribute> {
    // Build Array of SimpleFlatAttribute before unflatten function
    let flatAttributes: Array<SimpleFlatAttribute> = [];
    flatAttributes.push(this.createSimpleFlatAttribute(attribute, instanceName)); // Push the root attribute
    attribute.flattenedChildren.forEach((child: DerivedFEAttribute): void => {
      if (child.isChildOfListOrMap && child.schema.property.isSimpleType) return; //do not display non-complex children of list or map
      flatAttributes.push(this.createSimpleFlatAttribute(child, instanceName));
    });

    let tree = this.unflatten(flatAttributes, '', []);
    return tree[0].childrens; // Return the childrens without the root.
  }

  public createSimpleFlatAttribute = (attribute: AttributeFEModel | DerivedFEAttribute, instanceName: string): SimpleFlatAttribute => {
    if (attribute instanceof AttributeFEModel) {
      return new SimpleFlatAttribute(attribute.uniqueId, attribute.name, attribute.name, '', instanceName);
    } else {
      let attribName: string = (attribute.isChildOfListOrMap) ? attribute.mapKey : attribute.name;
      return new SimpleFlatAttribute(attribute.uniqueId, attribute.attributesName, attribName, attribute.parentName, instanceName);
    }

  }

  /**
   * Unflatten Array<SimpleFlatAttribute> and build hierarchy.
   * The result will be Array<SimpleFlatAttribute> that augmantin with children for each SimpleFlatAttribute.
   */
  private unflatten(array: Array<SimpleFlatAttribute>, parent: any, tree?: any): any {
    tree = typeof tree !== 'undefined' ? tree : [];
    parent = typeof parent !== 'undefined' && parent !== '' ? parent : {path: ''};

    var children = array.filter((child: SimpleFlatAttribute): boolean => {
      return child.parentName == parent.path;
    });

    if (children && children.length) {
      if (parent.path == '') {
        tree = children;
      } else {
        parent['children'] = children;
      }
      children.forEach((child): void => {
        this.unflatten(array, child);
      });
    }
    return tree;
  }
}
