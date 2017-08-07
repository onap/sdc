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

import {Component, Input, Output, EventEmitter} from "@angular/core";
import { PropertyBEModel, PropertyFEModel, DerivedFEProperty, DerivedPropertyType, SchemaPropertyGroupModel, DataTypeModel } from "app/models";
import { PROPERTY_DATA, PROPERTY_TYPES } from 'app/utils';
import { PropertiesUtils } from "app/ng2/pages/properties-assignment/properties.utils";
import { DataTypeService } from "../../../services/data-type.service";
import { trigger, state, style, transition, animate } from '@angular/core';


@Component({
    selector: 'dynamic-property',
    templateUrl: './dynamic-property.component.html',
    styleUrls: ['./dynamic-property.component.less'],
    animations: [trigger('fadeIn', [transition(':enter', [style({ opacity: '0' }), animate('.7s ease-out', style({ opacity: '1' }))])])]
})
export class DynamicPropertyComponent {

    derivedPropertyTypes = DerivedPropertyType; //http://stackoverflow.com/questions/35835984/how-to-use-a-typescript-enum-value-in-an-angular2-ngswitch-statement
    propType: DerivedPropertyType;
    propPath: string;
    isPropertyFEModel: boolean;
    nestedLevel: number;

    @Input() canBeDeclared: boolean;
    @Input() property: PropertyFEModel | DerivedFEProperty;
    @Input() expandedChildId: string;
    @Input() selectedPropertyId: string;
    @Input() propertyNameSearchText: string;
    @Input() readonly: boolean;

    @Output() valueChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() expandChild: EventEmitter<string> = new EventEmitter<string>();
    @Output() checkProperty: EventEmitter<string> = new EventEmitter<string>();
    @Output() deleteItem: EventEmitter<string> = new EventEmitter<string>();
    @Output() clickOnPropertyRow: EventEmitter<PropertyFEModel | DerivedFEProperty> = new EventEmitter<PropertyFEModel | DerivedFEProperty>();
    @Output() mapKeyChanged: EventEmitter<string> = new EventEmitter<string>();
    @Output() addChildPropsToParent: EventEmitter<Array<DerivedFEProperty>> = new EventEmitter<Array<DerivedFEProperty>>();


    constructor(private propertiesUtils: PropertiesUtils, private dataTypeService: DataTypeService) {
    }

    ngOnInit() {
        this.isPropertyFEModel = this.property instanceof PropertyFEModel;
        this.propType = this.property.derivedDataType;
        this.propPath = (this.property instanceof PropertyFEModel) ? this.property.name : this.property.propertiesName;
        this.nestedLevel = (this.property.propertiesName.match(/#/g) || []).length;
    }


    onClickPropertyRow = (property, event) => {
        // Because DynamicPropertyComponent is recrusive second time the event is fire event.stopPropagation = undefined
        event && event.stopPropagation && event.stopPropagation();
        this.clickOnPropertyRow.emit(property);
    }


    expandChildById = (id: string) => {
        this.expandedChildId = id;
        this.expandChild.emit(id);
    }

    checkedChange = (propName: string) => {
        this.checkProperty.emit(propName);
    }

    hasChildren = (): number => {
        return (this.property.valueObj && typeof this.property.valueObj == 'object') ? Object.keys(this.property.valueObj).length : 0;
    }

    createNewChildProperty = (): void => {

        let newProps: Array<DerivedFEProperty> = this.propertiesUtils.createListOrMapChildren(this.property, "", undefined);
        if (this.property instanceof PropertyFEModel) {
            this.addChildProps(newProps, this.property.name);
        } else {
            this.addChildPropsToParent.emit(newProps);
        }
    }

    addChildProps = (newProps: Array<DerivedFEProperty>, childPropName: string) => {

        if (this.property instanceof PropertyFEModel) {
            let insertIndex: number = this.property.getIndexOfChild(childPropName) + this.property.getCountOfChildren(childPropName); //insert after parent prop and existing children
            this.property.flattenedChildren.splice(insertIndex, 0, ...newProps); //using ES6 spread operator
            this.expandChildById(newProps[0].propertiesName);


            if(!newProps[0].schema.property.isSimpleType){
                angular.forEach(newProps, (prop:DerivedFEProperty):void => { //Update parent PropertyFEModel with value for each child, including nested props
                    (<PropertyFEModel>this.property).childPropUpdated(prop);
                },this);
                //grab the cumulative value for the new item from parent PropertyFEModel and assign that value to DerivedFEProp[0] (which is the list or map parent with UUID of the set we just added)
                let parentNames = (<PropertyFEModel>this.property).getParentNamesArray(newProps[0].propertiesName, []);
                newProps[0].valueObj = _.get(this.property.valueObj, parentNames.join('.'));
                if ( newProps[0].mapKey ) {//prevent saving if it is map item, whem it is list item- the map key is the es type (it will be saved only after user enter key)
                    this.valueChanged.emit(this.property.name);
                }
            }
        }
    }

    childValueChanged = (property: DerivedFEProperty) => { //value of child property changed

        if (this.property instanceof PropertyFEModel) { // will always be the case
            this.property.childPropUpdated(property);
            this.dataTypeService.checkForCustomBehavior(this.property);
            if (this.property.getParentNamesArray(property.propertiesName, []).indexOf('') === -1) {//If one of the parents is empty key -don't save
                this.valueChanged.emit(this.property.name);
            }
        }
    }

    deleteListOrMapItem = (item: DerivedFEProperty) => {
        if (this.property instanceof PropertyFEModel) {
            this.removeValueFromParent(item);
            this.property.flattenedChildren.splice(this.property.getIndexOfChild(item.propertiesName), this.property.getCountOfChildren(item.propertiesName));
            this.expandChildById(item.propertiesName);
        }
    }

    removeValueFromParent = (item: DerivedFEProperty, target?: any) => {
        if (this.property instanceof PropertyFEModel) {
            let itemParent = (item.parentName == this.property.name) ? this.property : this.property.flattenedChildren.find(prop => prop.propertiesName == item.parentName);

            if (item.derivedDataType == DerivedPropertyType.MAP) {
                let oldKey = item.mapKey;
                if (target && typeof target.value == 'string') { //allow saving empty string
                    let replaceKey:string = target.value;
                    if (!replaceKey) {//prevent delete map key
                        return;
                    }
                    if(Object.keys(itemParent.valueObj).indexOf(replaceKey) > -1){//the key is exists
                        target.setCustomValidity('This key is already exists.');
                        return;
                    }else {
                        target.setCustomValidity('');
                        _.set(itemParent.valueObj, replaceKey, itemParent.valueObj[oldKey]);
                        item.mapKey = replaceKey;
                    }
                }
                delete itemParent.valueObj[oldKey];
            } else {
                let itemIndex: number = this.property.flattenedChildren.filter(prop => prop.parentName == item.parentName).map(prop => prop.propertiesName).indexOf(item.propertiesName);
                itemParent.valueObj.splice(itemIndex, 1);
            }
            if (item.mapKey) {//prevent going to BE if user tries to delete map item without key (it was not saved in BE)
                if (itemParent instanceof PropertyFEModel) { //direct child
                    this.valueChanged.emit(this.property.name);
                } else { //nested child - need to update parent prop by getting flattened name (recurse through parents and replace map/list keys, etc)
                    this.childValueChanged(itemParent);
                }
            }
        }
    }

    preventInsertItem = (property:DerivedFEProperty):boolean => {
        if(property.type == PROPERTY_TYPES.MAP && Object.keys(property.valueObj).indexOf('') > -1 ){
            return true;
        }
        return false;
    }

}
