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
import { SimpleFlatProperty, PropertyFEModel, DerivedFEProperty } from 'app/models';


@Injectable()
export class HierarchyNavService {
    /**
     * Build hirarchy structure for the tree when user selects on table row.
     * First create Array<SimpleFlatProperty> and insert also the parent (PropertyFEModel) to this array.
     * The Array is flat and contains SimpleFlatProperty that has parentName and uniqueId.
     * Now we build hirarchy from this Array (that includes childrens) and return it for the tree
     *
     * @argument property: PropertyFEModel - property contains flattenedChildren array of DerivedFEProperty
     * @returns  Array<SimpleFlatProperty> - containing childrens Array<SimpleFlatProperty>, augmantin childrens to SimpleFlatProperty.
     */
    public getSimplePropertiesTree(property: PropertyFEModel, instanceName: string): Array<SimpleFlatProperty> {
        // Build Array of SimpleFlatProperty before unflatten function
        let flattenProperties: Array<SimpleFlatProperty> = [];
        flattenProperties.push(this.createSimpleFlatProperty(property, instanceName)); // Push the root property
        _.each(property.flattenedChildren, (child: DerivedFEProperty): void => {
            if (child.isChildOfListOrMap && child.schema.property.isSimpleType) return; //do not display non-complex children of list or map
            flattenProperties.push(this.createSimpleFlatProperty(child, instanceName));
        });

        let tree = this.unflatten(flattenProperties, '', []);
        return tree[0].childrens; // Return the childrens without the root.
    }

    public createSimpleFlatProperty = (property: PropertyFEModel | DerivedFEProperty, instanceName:string): SimpleFlatProperty => {
        if (property instanceof PropertyFEModel) {
            return new SimpleFlatProperty(property.uniqueId, property.name, property.name, '', instanceName);
        } else {
            let propName: string = (property.isChildOfListOrMap) ? property.mapKey : property.name;
            return new SimpleFlatProperty(property.uniqueId, property.propertiesName, propName, property.parentName, instanceName);
        }
        
    }

    /**
     * Unflatten Array<SimpleFlatProperty> and build hirarchy.
     * The result will be Array<SimpleFlatProperty> that augmantin with childrens for each SimpleFlatProperty.
     */
    private unflatten(array: Array<SimpleFlatProperty>, parent: any, tree?: any): any {
        tree = typeof tree !== 'undefined' ? tree : [];
        parent = typeof parent !== 'undefined' && parent !== '' ? parent : { path: '' };

        var childrens = _.filter(array, (child: SimpleFlatProperty): boolean => {
            return child.parentName == parent.path;
        });

        if (!_.isEmpty(childrens)) {
            if (parent.path == '') {
                tree = childrens;
            } else {
                parent['childrens'] = childrens;
            }
            _.each(childrens, (child): void => {
                this.unflatten(array, child);
            });
        }
        return tree;
    }
}
