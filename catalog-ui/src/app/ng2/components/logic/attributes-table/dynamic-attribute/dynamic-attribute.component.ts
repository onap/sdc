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

import * as _ from "lodash";
import {Component, EventEmitter, Input, Output, ViewChild} from "@angular/core";
import {PROPERTY_TYPES} from 'app/utils';
import {DataTypeService} from "../../../../services/data-type.service";
import {animate, style, transition, trigger} from '@angular/animations';
import {IUiElementChangeEvent} from "../../../ui/form-components/ui-element-base.component";
import {DynamicElementComponent} from "../../../ui/dynamic-element/dynamic-element.component";
import {DerivedAttributeType} from "../../../../../models/attributes-outputs/attribute-be-model";
import {AttributeFEModel} from "app/models/attributes-outputs/attribute-fe-model";
import {DerivedFEAttribute} from "../../../../../models/attributes-outputs/derived-fe-attribute";
import {AttributesUtils} from "../../../../pages/attributes-outputs/services/attributes.utils";

@Component({
  selector: 'dynamic-property',
  templateUrl: './dynamic-attribute.component.html',
  styleUrls: ['./dynamic-attribute.component.less'],
  animations: [trigger('fadeIn', [transition(':enter', [style({opacity: '0'}), animate('.7s ease-out', style({opacity: '1'}))])])]
})
export class DynamicAttributeComponent {

  derivedAttributeType = DerivedAttributeType;
  attribType: DerivedAttributeType;
  attribPath: string;
  isAttributeFEModel: boolean;
  nestedLevel: number;
  attributeTestsId: string;
  constraints: string[];

  @Input() canBeDeclared: boolean;
  @Input() attribute: AttributeFEModel | DerivedFEAttribute;
  @Input() expandedChildId: string;
  @Input() selectedAttributeId: string;
  @Input() attributeNameSearchText: string;
  @Input() readonly: boolean;
  @Input() hasChildren: boolean;
  @Input() hasDeclareOption: boolean;
  @Input() rootAttribute: AttributeFEModel;

  @Output('attributeChanged') emitter: EventEmitter<void> = new EventEmitter<void>();
  @Output() expandChild: EventEmitter<string> = new EventEmitter<string>();
  @Output() checkAttribute: EventEmitter<string> = new EventEmitter<string>();
  @Output() deleteItem: EventEmitter<string> = new EventEmitter<string>();
  @Output() clickOnAttributeRow: EventEmitter<AttributeFEModel | DerivedFEAttribute> = new EventEmitter<AttributeFEModel | DerivedFEAttribute>();
  @Output() mapKeyChanged: EventEmitter<string> = new EventEmitter<string>();
  @Output() addChildAttribsToParent: EventEmitter<Array<DerivedFEAttribute>> = new EventEmitter<Array<DerivedFEAttribute>>();

  @ViewChild('mapKeyInput') public mapKeyInput: DynamicElementComponent;

  constructor(private attributesUtils: AttributesUtils, private dataTypeService: DataTypeService) {
  }

  ngOnInit() {
    this.isAttributeFEModel = this.attribute instanceof AttributeFEModel;
    this.attribType = this.attribute.derivedDataType;
    this.attribPath = (this.attribute instanceof AttributeFEModel) ? this.attribute.name : this.attribute.attributesName;
    this.nestedLevel = (this.attribute.attributesName.match(/#/g) || []).length;
    this.rootAttribute = (this.rootAttribute) ? this.rootAttribute : <AttributeFEModel>this.attribute;
    this.attributeTestsId = this.getAttributeTestsId();

    this.initConstraintsValues();

  }

  initConstraintsValues() {
    let primitiveProperties = ['string', 'integer', 'float', 'boolean'];

    if (this.attribute.constraints) {
      this.constraints = this.attribute.constraints[0].validValues
    }

    //Complex Type
    else if (primitiveProperties.indexOf(this.rootAttribute.type) == -1 && primitiveProperties.indexOf(this.attribute.type) >= 0) {
      this.constraints = this.dataTypeService.getConstraintsByParentTypeAndUniqueID(this.rootAttribute.type, this.attribute.name);
    } else {
      this.constraints = null;
    }

  }

  onClickPropertyRow = (property, event) => {
    // Because DynamicAttributeComponent is recursive second time the event is fire event.stopPropagation = undefined
    event && event.stopPropagation && event.stopPropagation();
    this.clickOnAttributeRow.emit(property);
  }

  expandChildById = (id: string) => {
    this.expandedChildId = id;
    this.expandChild.emit(id);
  }

  checkedChange = (propName: string) => {
    this.checkAttribute.emit(propName);
  }

  getHasChildren = (property: DerivedFEAttribute): boolean => {// enter to this function only from base property (AttributeFEModel) and check for child property if it has children
    return _.filter((<AttributeFEModel>this.attribute).flattenedChildren, (prop: DerivedFEAttribute) => {
      return _.startsWith(prop.attributesName + '#', property.attributesName);
    }).length > 1;
  }

  getAttributeTestsId = () => {
    return [this.rootAttribute.name].concat(this.rootAttribute.getParentNamesArray(this.attribute.attributesName, [], true)).join('.');
  };

  onElementChanged = (event: IUiElementChangeEvent) => {
    this.attribute.updateValueObj(event.value, event.isValid);
    this.emitter.emit();
  };

  createNewChildProperty = (): void => {

    let newProps: Array<DerivedFEAttribute> = this.attributesUtils.createListOrMapChildren(this.attribute, "", null);
    this.attributesUtils.assignFlattenedChildrenValues(this.attribute.valueObj, [newProps[0]], this.attribute.attributesName);
    if (this.attribute instanceof AttributeFEModel) {
      this.addChildProps(newProps, this.attribute.name);
    } else {
      this.addChildAttribsToParent.emit(newProps);
    }
  }

  addChildProps = (newProps: Array<DerivedFEAttribute>, childPropName: string) => {

    if (this.attribute instanceof AttributeFEModel) {
      let insertIndex: number = this.attribute.getIndexOfChild(childPropName) + this.attribute.getCountOfChildren(childPropName); //insert after parent prop and existing children
      this.attribute.flattenedChildren.splice(insertIndex, 0, ...newProps); //using ES6 spread operator
      this.expandChildById(newProps[0].attributesName);

      this.updateMapKeyValueOnMainParent(newProps);
      this.emitter.emit();
    }
  }

  updateMapKeyValueOnMainParent(childrenProps: Array<DerivedFEAttribute>) {
    if (this.attribute instanceof AttributeFEModel) {
      const attributeFEModel: AttributeFEModel = <AttributeFEModel>this.attribute;
      //Update only if all this attributeFEModel parents has key name
      if (attributeFEModel.getParentNamesArray(childrenProps[0].attributesName, []).indexOf('') === -1) {
        angular.forEach(childrenProps, (prop: DerivedFEAttribute): void => { //Update parent AttributeFEModel with value for each child, including nested props
          attributeFEModel.childPropUpdated(prop);
          if (prop.isChildOfListOrMap && prop.mapKey !== undefined) {
            attributeFEModel.childPropMapKeyUpdated(prop, prop.mapKey, true);
          }
        }, this);
        //grab the cumulative value for the new item from parent AttributeFEModel and assign that value to DerivedFEProp[0] (which is the list or map parent with UUID of the set we just added)
        let parentNames = (<AttributeFEModel>attributeFEModel).getParentNamesArray(childrenProps[0].attributesName, []);
        childrenProps[0].valueObj = _.get(attributeFEModel.valueObj, parentNames.join('.'), null);
      }
    }
  }

  childValueChanged = (property: DerivedFEAttribute) => { //value of child property changed

    if (this.attribute instanceof AttributeFEModel) { // will always be the case
      if (this.attribute.getParentNamesArray(property.attributesName, []).indexOf('') === -1) {//If one of the parents is empty key -don't save
        this.attribute.childPropUpdated(property);
        this.emitter.emit();
      }
    }
  }

  deleteListOrMapItem = (item: DerivedFEAttribute) => {
    if (this.attribute instanceof AttributeFEModel) {
      this.removeValueFromParent(item);
      this.attribute.flattenedChildren.splice(this.attribute.getIndexOfChild(item.attributesName), this.attribute.getCountOfChildren(item.attributesName));
      this.expandChildById(item.attributesName);
    }
  }

  removeValueFromParent = (item: DerivedFEAttribute) => {
    if (this.attribute instanceof AttributeFEModel) {
      let itemParent = (item.parentName == this.attribute.name)
          ? this.attribute : this.attribute.flattenedChildren.find(prop => prop.attributesName == item.parentName);
      if (!itemParent) {
        return;
      }

      if (item.derivedDataType == DerivedAttributeType.MAP) {
        const oldKey = item.getActualMapKey();
        delete itemParent.valueObj[oldKey];
        if (itemParent instanceof AttributeFEModel) {
          delete itemParent.valueObjValidation[oldKey];
          itemParent.valueObjIsValid = itemParent.calculateValueObjIsValid();
        }
        this.attribute.childPropMapKeyUpdated(item, null);  // remove map key
      } else {
        const itemIndex: number = this.attribute.flattenedChildren.filter(prop => prop.parentName == item.parentName).map(prop => prop.attributesName).indexOf(item.attributesName);
        itemParent.valueObj.splice(itemIndex, 1);
        if (itemParent instanceof AttributeFEModel) {
          itemParent.valueObjValidation.splice(itemIndex, 1);
          itemParent.valueObjIsValid = itemParent.calculateValueObjIsValid();
        }
      }
      if (itemParent instanceof AttributeFEModel) { //direct child
        this.emitter.emit();
      } else { //nested child - need to update parent prop by getting flattened name (recurse through parents and replace map/list keys, etc)
        this.childValueChanged(itemParent);
      }
    }
  }

  updateChildKeyInParent(childProp: DerivedFEAttribute, newMapKey: string) {
    if (this.attribute instanceof AttributeFEModel) {
      this.attribute.childPropMapKeyUpdated(childProp, newMapKey);
      this.emitter.emit();
    }
  }

  preventInsertItem = (property: DerivedFEAttribute): boolean => {
    return property.type == PROPERTY_TYPES.MAP && Object.keys(property.valueObj).indexOf('') > -1;
  }

}
