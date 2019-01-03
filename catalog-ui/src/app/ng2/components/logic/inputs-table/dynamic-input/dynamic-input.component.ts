/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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
import {Component, Input, Output, EventEmitter, ViewChild} from "@angular/core";
import {DerivedFEProperty, DerivedPropertyType, InputFEModel} from "app/models";
import { PROPERTY_TYPES } from 'app/utils';
import { DataTypeService } from "../../../../services/data-type.service";
import { trigger, style, transition, animate } from '@angular/core';
import {PropertiesUtils} from "../../../../pages/properties-assignment/services/properties.utils";
import {IUiElementChangeEvent} from "../../../ui/form-components/ui-element-base.component";
import {DynamicElementComponent} from "../../../ui/dynamic-element/dynamic-element.component";

@Component({
    selector: 'dynamic-input',
    templateUrl: './dynamic-input.component.html',
    styleUrls: ['./dynamic-input.component.less'],
    animations: [trigger('fadeIn', [transition(':enter', [style({ opacity: '0' }), animate('.7s ease-out', style({ opacity: '1' }))])])]
})
export class DynamicInputComponent {

    derivedPropertyTypes = DerivedPropertyType;
    propType: DerivedPropertyType;
    propPath: string;
    isInputFEModel: boolean;
    nestedLevel: number;
    inputTestId: string;

    @Input() input: InputFEModel | DerivedFEProperty;
    @Input() expandedChildId: string;
    @Input() readonly: boolean;
    @Input() hasChildren: boolean;
    @Input() rootInput: InputFEModel;

    @Output('inputChanged') emitter: EventEmitter<void> = new EventEmitter<void>();
    @Output() expandChild: EventEmitter<string> = new EventEmitter<string>();
    @Output() deleteItem: EventEmitter<string> = new EventEmitter<string>();
    @Output() mapKeyChanged: EventEmitter<string> = new EventEmitter<string>();
    @Output() addChildPropsToParent: EventEmitter<Array<DerivedFEProperty>> = new EventEmitter<Array<DerivedFEProperty>>();

    @ViewChild('mapKeyInput') public mapKeyInput: DynamicElementComponent;

    constructor(private propertiesUtils: PropertiesUtils, private dataTypeService: DataTypeService) {
    }

    ngOnInit() {
        this.isInputFEModel = this.input instanceof InputFEModel;
        this.propType = this.input.derivedDataType;
        this.propPath = (this.input instanceof InputFEModel) ? this.input.name : this.input.propertiesName;
        this.nestedLevel = (this.input instanceof InputFEModel) ? (this.input.name.match(/#/g) || []).length :  (this.input.propertiesName.match(/#/g) || []).length;
        this.rootInput = (this.rootInput) ? this.rootInput : <InputFEModel>this.input;
        this.inputTestId = this.getPropertyTestsId();
        if (this.input instanceof InputFEModel){
          this.propertiesUtils.initChildrenMapForInputObject(this.input);
        }
    }

    ngDoCheck() {
        // set custom error for mapKeyInput
        if (this.mapKeyInput) {
            const mapKeyInputControl = this.mapKeyInput.cmpRef.instance.control;
            const mapKeyError = (<DerivedFEProperty>this.input).mapKeyError;
            if (mapKeyInputControl.getError('mapKeyError') !== mapKeyError) {
                mapKeyInputControl.setErrors({mapKeyError});
            }
        }
    }

    expandChildById = (id: string) => {
        this.expandedChildId = id;
        this.expandChild.emit(id);
    };

    getPropertyTestsId = () => {
        return [this.rootInput.name].concat(this.rootInput.getParentNamesArray(this.input.name, [], true)).join('.');
    };

    isExpanded = () => {
      return this.expandedChildId ? this.expandedChildId.indexOf(this.propPath) == 0 : false;
    }

  getHasChildren = (property:DerivedFEProperty): boolean => {// enter to this function only from base property (PropertyFEModel) and check for child property if it has children
    return _.filter((<InputFEModel>this.input).flattenedChildren,(prop:DerivedFEProperty)=>{
      return _.startsWith(prop.propertiesName + '#', property.propertiesName);
    }).length > 1;
  }

    onElementChanged = (event: IUiElementChangeEvent) => {
        if (this.input instanceof DerivedFEProperty){
          this.input.updateValueObj(event.value, event.isValid);
        }else{
          this.input.updateDefaultValueObj(event.value, event.isValid);
        }
        this.emitter.emit();
    };

    createNewChildProperty = (): void => {
        let newProps: Array<DerivedFEProperty> = this.propertiesUtils.createListOrMapChildren(this.input, "", null);
        if (this.input instanceof DerivedFEProperty){
          this.propertiesUtils.assignFlattenedChildrenValues(this.input.valueObj, [newProps[0]], this.input.name);
        }else{
          this.propertiesUtils.assignFlattenedChildrenValues(this.input.defaultValueObj, [newProps[0]], this.input.name);
        }
        if (this.input instanceof InputFEModel) {
            this.addChildProps(newProps, this.input.name);
        } else {
            this.addChildPropsToParent.emit(newProps);
        }
    };

    addChildProps = (newProps: Array<DerivedFEProperty>, childPropName: string) => {
        if (this.input instanceof InputFEModel) {
            let insertIndex: number = this.input.getIndexOfChild(childPropName) + this.input.getCountOfChildren(childPropName); //insert after parent prop and existing children
            this.input.flattenedChildren.splice(insertIndex, 0, ...newProps); //using ES6 spread operator
            this.expandChildById(newProps[0].propertiesName);

            this.updateMapKeyValueOnMainParent(newProps);
            this.emitter.emit();
        }
    };

    updateMapKeyValueOnMainParent(childrenProps: Array<DerivedFEProperty>){
        if (this.input instanceof InputFEModel) {
            const input: InputFEModel = <InputFEModel>this.input;
            //Update only if all this input parents has key name
            if (input.getParentNamesArray(childrenProps[0].propertiesName, []).indexOf('') === -1){
                angular.forEach(childrenProps, (prop:DerivedFEProperty):void => { //Update parent PropertyFEModel with value for each child, including nested props
                    input.childPropUpdated(prop);
                    if (prop.isChildOfListOrMap && prop.mapKey !== undefined) {
                        input.childPropMapKeyUpdated(prop, prop.mapKey, true);
                    }
                },this);
                //grab the cumulative value for the new item from parent PropertyFEModel and assign that value to DerivedFEProp[0] (which is the list or map parent with UUID of the set we just added)
                let parentNames = (<InputFEModel>input).getParentNamesArray(childrenProps[0].propertiesName, []);
                childrenProps[0].valueObj = _.get(input.defaultValueObj, parentNames.join('.'), null);
            }
        }
    }

    childValueChanged = (property: DerivedFEProperty) => { //value of child property changed
        if (this.input instanceof InputFEModel) { // will always be the case
            if (this.input.getParentNamesArray(property.propertiesName, []).indexOf('') === -1) {//If one of the parents is empty key -don't save
                this.input.childPropUpdated(property);
                this.dataTypeService.checkForCustomBehavior(this.input);
                this.emitter.emit();
            }
        }
    };

    deleteListOrMapItem = (item: DerivedFEProperty) => {
        if (this.input instanceof InputFEModel) {
            this.removeValueFromParent(item);
            this.input.flattenedChildren.splice(this.input.getIndexOfChild(item.propertiesName), this.input.getCountOfChildren(item.propertiesName));
            this.expandChildById(item.propertiesName);
        }
    };

    removeValueFromParent = (item: DerivedFEProperty) => {
        if (this.input instanceof InputFEModel) {
            let itemParent = (item.parentName == this.input.name)
                ? this.input : this.input.flattenedChildren.find(prop => prop.propertiesName == item.parentName);
            if (!itemParent) {
                return;
            }

            if (item.derivedDataType == DerivedPropertyType.MAP) {
                const oldKey = item.getActualMapKey();
                if (itemParent instanceof DerivedFEProperty){
                  delete itemParent.valueObj[oldKey];
                }else{
                  delete itemParent.defaultValueObj[oldKey];
                }
                this.input.childPropMapKeyUpdated(item, null);  // remove map key
            } else {
                const itemIndex: number = this.input.flattenedChildren.filter(prop => prop.parentName == item.parentName).map(prop => prop.propertiesName).indexOf(item.propertiesName);
                if (itemParent instanceof InputFEModel) {
                  itemParent.defaultValueObj.splice(itemIndex, 1);
                  itemParent.defaultValueObjIsValid= true;
                }
            }
            if (itemParent instanceof InputFEModel) { //direct child
                this.emitter.emit();
            } else { //nested child - need to update parent prop by getting flattened name (recurse through parents and replace map/list keys, etc)
                this.childValueChanged(itemParent);
            }
        }
    };

  updateChildKeyInParent(childProp: DerivedFEProperty, newMapKey: string) {
    if (this.input instanceof InputFEModel) {
      this.input.childPropMapKeyUpdated(childProp, newMapKey);
      this.emitter.emit();
    }
  }

    preventInsertItem = (input: DerivedFEProperty | InputFEModel):boolean => {
      if (input instanceof InputFEModel){
        return input.type == PROPERTY_TYPES.MAP && Object.keys(input.defaultValueObj).indexOf('') > -1;
      }else{
        return input.type == PROPERTY_TYPES.MAP && Object.keys(input.valueObj).indexOf('') > -1;
      }
    };

}
