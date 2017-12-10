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
import { PropertyFEModel, DerivedFEProperty, DerivedPropertyType } from "app/models";
import { PROPERTY_TYPES } from 'app/utils';
import { DataTypeService } from "../../../../services/data-type.service";
import { trigger, state, style, transition, animate } from '@angular/core';
import {PropertiesUtils} from "../../../../pages/properties-assignment/services/properties.utils";


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
    @Input() hasChildren: boolean;
    @Input() hasDeclareOption:boolean;

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

    getHasChildren = (property:DerivedFEProperty): boolean => {// enter to this function only from base property (PropertyFEModel) and check for child property if it has children
        return _.filter((<PropertyFEModel>this.property).flattenedChildren,(prop:DerivedFEProperty)=>{
            return _.startsWith(prop.propertiesName + '#', property.propertiesName);
        }).length > 1;
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
                if ( newProps[0].mapKey ) {//prevent update the new item value on parent property valueObj and saving on BE if it is map item, it will be updated and saved only after user enter key (when it is list item- the map key is the es type)
                    this.updateMapKeyValueOnMainParent(newProps);
                    if (this.property.getParentNamesArray(newProps[0].propertiesName, []).indexOf('') === -1) {
                        this.valueChanged.emit(this.property.name);
                    }
                }
            }
        }
    }

    updateMapKeyValueOnMainParent(childrenProps: Array<DerivedFEProperty>){
        if (this.property instanceof PropertyFEModel) {
            //Update only if all this property parents has key name
            if (this.property.getParentNamesArray(childrenProps[0].propertiesName, []).indexOf('') === -1){
                angular.forEach(childrenProps, (prop:DerivedFEProperty):void => { //Update parent PropertyFEModel with value for each child, including nested props
                    (<PropertyFEModel>this.property).childPropUpdated(prop);
                },this);
                //grab the cumulative value for the new item from parent PropertyFEModel and assign that value to DerivedFEProp[0] (which is the list or map parent with UUID of the set we just added)
                let parentNames = (<PropertyFEModel>this.property).getParentNamesArray(childrenProps[0].propertiesName, []);
                childrenProps[0].valueObj = _.get(this.property.valueObj, parentNames.join('.'));
            }
        }
    }

    childValueChanged = (property: DerivedFEProperty) => { //value of child property changed

        if (this.property instanceof PropertyFEModel) { // will always be the case
            if (this.property.getParentNamesArray(property.propertiesName, []).indexOf('') === -1) {//If one of the parents is empty key -don't save
                this.property.childPropUpdated(property);
                this.dataTypeService.checkForCustomBehavior(this.property);
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
                        //If the map key was empty its valueObj was not updated on its prent property valueObj, and now we should update it.
                        if(!oldKey && !item.schema.property.isSimpleType){
                            //Search this map item children and update these value on parent property valueOBj
                            let mapKeyFlattenChildren:Array<DerivedFEProperty> = _.filter(this.property.flattenedChildren, (prop:DerivedFEProperty) => {
                                return _.startsWith(prop.propertiesName, item.propertiesName);
                            });
                            this.updateMapKeyValueOnMainParent(mapKeyFlattenChildren);
                        }
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
