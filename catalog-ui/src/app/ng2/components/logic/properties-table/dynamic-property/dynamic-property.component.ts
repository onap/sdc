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

import * as _ from "lodash";
import {Component, Input, Output, EventEmitter, ViewChild, ComponentRef} from "@angular/core";
import { PropertyFEModel, DerivedFEProperty, DerivedPropertyType } from "app/models";
import { PROPERTY_TYPES } from 'app/utils';
import { DataTypeService } from "../../../../services/data-type.service";
import { trigger, state, style, transition, animate } from '@angular/animations';
import {PropertiesUtils} from "../../../../pages/properties-assignment/services/properties.utils";
import {IUiElementChangeEvent} from "../../../ui/form-components/ui-element-base.component";
import {DynamicElementComponent} from "../../../ui/dynamic-element/dynamic-element.component";

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
    propertyTestsId: string;
    constraints:string[];

    @Input() canBeDeclared: boolean;
    @Input() property: PropertyFEModel | DerivedFEProperty;
    @Input() expandedChildId: string;
    @Input() selectedPropertyId: string;
    @Input() propertyNameSearchText: string;
    @Input() readonly: boolean;
    @Input() hasChildren: boolean;
    @Input() hasDeclareOption:boolean;
    @Input() rootProperty: PropertyFEModel;

    @Output('propertyChanged') emitter: EventEmitter<void> = new EventEmitter<void>();
    @Output() expandChild: EventEmitter<string> = new EventEmitter<string>();
    @Output() checkProperty: EventEmitter<string> = new EventEmitter<string>();
    @Output() deleteItem: EventEmitter<string> = new EventEmitter<string>();
    @Output() clickOnPropertyRow: EventEmitter<PropertyFEModel | DerivedFEProperty> = new EventEmitter<PropertyFEModel | DerivedFEProperty>();
    @Output() mapKeyChanged: EventEmitter<string> = new EventEmitter<string>();
    @Output() addChildPropsToParent: EventEmitter<Array<DerivedFEProperty>> = new EventEmitter<Array<DerivedFEProperty>>();

    @ViewChild('mapKeyInput') public mapKeyInput: DynamicElementComponent;

    constructor(private propertiesUtils: PropertiesUtils, private dataTypeService: DataTypeService) {
    }

    ngOnInit() {
        this.isPropertyFEModel = this.property instanceof PropertyFEModel;
        this.propType = this.property.derivedDataType;
        this.propPath = (this.property instanceof PropertyFEModel) ? this.property.name : this.property.propertiesName;
        this.nestedLevel = (this.property.propertiesName.match(/#/g) || []).length;
        this.rootProperty = (this.rootProperty) ? this.rootProperty : <PropertyFEModel>this.property;
        this.propertyTestsId = this.getPropertyTestsId(); 
        
        this.initConsraintsValues();
        
        
    }

    initConsraintsValues(){
        let primitiveProperties = ['string', 'integer', 'float', 'boolean'];

        //Property has constraints
        if(this.property.constraints && this.property.constraints[0]){
            this.constraints = this.property.constraints[0].validValues
        }

        //Complex Type
        else if (primitiveProperties.indexOf(this.rootProperty.type) == -1 && primitiveProperties.indexOf(this.property.type) >= 0 ){
            this.constraints = this.dataTypeService.getConstraintsByParentTypeAndUniqueID(this.rootProperty.type, this.property.name);           
        }
 
        else{
            this.constraints = null;
        }
        
    }

    ngDoCheck() {
        // set custom error for mapKeyInput
        if (this.mapKeyInput) {
            const mapKeyInputControl = this.mapKeyInput.cmpRef.instance.control;
            const mapKeyError = (<DerivedFEProperty>this.property).mapKeyError;
            if (mapKeyInputControl.getError('mapKeyError') !== mapKeyError) {
                mapKeyInputControl.setErrors({mapKeyError});
            }
        }
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

    getPropertyTestsId = () => {
        return [this.rootProperty.name].concat(this.rootProperty.getParentNamesArray(this.property.propertiesName, [], true)).join('.');
    };

    onElementChanged = (event: IUiElementChangeEvent) => {
        this.property.updateValueObj(event.value, event.isValid);
        this.emitter.emit();
    };

    createNewChildProperty = (): void => {

        let newProps: Array<DerivedFEProperty> = this.propertiesUtils.createListOrMapChildren(this.property, "", null);
        this.propertiesUtils.assignFlattenedChildrenValues(this.property.valueObj, [newProps[0]], this.property.propertiesName);
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

            this.updateMapKeyValueOnMainParent(newProps);
            this.emitter.emit();
        }
    }

    updateMapKeyValueOnMainParent(childrenProps: Array<DerivedFEProperty>){
        if (this.property instanceof PropertyFEModel) {
            const property: PropertyFEModel = <PropertyFEModel>this.property;
            //Update only if all this property parents has key name
            if (property.getParentNamesArray(childrenProps[0].propertiesName, []).indexOf('') === -1){
                angular.forEach(childrenProps, (prop:DerivedFEProperty):void => { //Update parent PropertyFEModel with value for each child, including nested props
                    property.childPropUpdated(prop);
                    if (prop.isChildOfListOrMap && prop.mapKey !== undefined) {
                        property.childPropMapKeyUpdated(prop, prop.mapKey, true);
                    }
                },this);
                //grab the cumulative value for the new item from parent PropertyFEModel and assign that value to DerivedFEProp[0] (which is the list or map parent with UUID of the set we just added)
                let parentNames = (<PropertyFEModel>property).getParentNamesArray(childrenProps[0].propertiesName, []);
                childrenProps[0].valueObj = _.get(property.valueObj, parentNames.join('.'), null);
            }
        }
    }

    childValueChanged = (property: DerivedFEProperty) => { //value of child property changed

        if (this.property instanceof PropertyFEModel) { // will always be the case
            if (this.property.getParentNamesArray(property.propertiesName, []).indexOf('') === -1) {//If one of the parents is empty key -don't save
                this.property.childPropUpdated(property);
                this.dataTypeService.checkForCustomBehavior(this.property);
                this.emitter.emit();
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

    removeValueFromParent = (item: DerivedFEProperty) => {
        if (this.property instanceof PropertyFEModel) {
            let itemParent = (item.parentName == this.property.name)
                ? this.property : this.property.flattenedChildren.find(prop => prop.propertiesName == item.parentName);
            if (!itemParent) {
                return;
            }

            if (item.derivedDataType == DerivedPropertyType.MAP && !item.mapInlist) {
                const oldKey = item.getActualMapKey();
                delete itemParent.valueObj[oldKey];
                if (itemParent instanceof PropertyFEModel) {
                    delete itemParent.valueObjValidation[oldKey];
                    itemParent.valueObjIsValid = itemParent.calculateValueObjIsValid();
                }
                this.property.childPropMapKeyUpdated(item, null);  // remove map key
            } else {
                const itemIndex: number = this.property.flattenedChildren.filter(prop => prop.parentName == item.parentName).map(prop => prop.propertiesName).indexOf(item.propertiesName);
                itemParent.valueObj.splice(itemIndex, 1);
                if (itemParent instanceof PropertyFEModel) {
                    itemParent.valueObjValidation.splice(itemIndex, 1);
                    itemParent.valueObjIsValid = itemParent.calculateValueObjIsValid();
                }
            }
            if (itemParent instanceof PropertyFEModel) { //direct child
                this.emitter.emit();
            } else { //nested child - need to update parent prop by getting flattened name (recurse through parents and replace map/list keys, etc)
                this.childValueChanged(itemParent);
            }
        }
    }

    updateChildKeyInParent(childProp: DerivedFEProperty, newMapKey: string) {
        if (this.property instanceof PropertyFEModel) {
            this.property.childPropMapKeyUpdated(childProp, newMapKey);
            this.emitter.emit();
        }
    }

    preventInsertItem = (property:DerivedFEProperty):boolean => {
        if(property.type == PROPERTY_TYPES.MAP && Object.keys(property.valueObj).indexOf('') > -1 ){
            return true;
        }
        return false;
    }

}
