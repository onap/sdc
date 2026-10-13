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

import { animate, style, transition, trigger } from '@angular/animations';
import { Component, ComponentRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { DerivedFEProperty } from 'app/models/properties-inputs/derived-fe-property';
import { DerivedPropertyType } from 'app/models/properties-inputs/property-be-model';
import { PropertyFEModel } from 'app/models/properties-inputs/property-fe-model';
import { SubPropertyToscaFunction } from 'app/models/sub-property-tosca-function';
import { PROPERTY_TYPES } from 'app/utils/constants';
import * as _ from 'lodash';
import { PropertiesUtils } from '../../../../pages/properties-assignment/services/properties.utils';
import { DataTypeService } from '../../../../services/data-type.service';
import { DynamicElementComponent } from '../../../ui/dynamic-element/dynamic-element.component';
import { IUiElementChangeEvent } from '../../../ui/form-components/ui-element-base.component';

@Component({
    selector: 'dynamic-property',
    templateUrl: './dynamic-property.component.html',
    styleUrls: ['./dynamic-property.component.less'],
    animations: [trigger('fadeIn', [transition(':enter', [style({ opacity: '0' }), animate('.7s ease-out', style({ opacity: '1' }))])])]
})
export class DynamicPropertyComponent {

    derivedPropertyTypes = DerivedPropertyType; // http://stackoverflow.com/questions/35835984/how-to-use-a-typescript-enum-value-in-an-angular2-ngswitch-statement
    propType: DerivedPropertyType;
    propPath: string;
    isPropertyFEModel: boolean;
    nestedLevel: number;
    propertyTestsId: string;
    constraints: string[];
    checkboxDisabled: boolean = false;

    @Input() canBeDeclared: boolean;
    @Input() property: PropertyFEModel | DerivedFEProperty;
    @Input() expandedChildId: string;
    @Input() selectedPropertyId: string;
    @Input() propertyNameSearchText: string;
    @Input() readonly: boolean;
    @Input() hasChildren: boolean;
    @Input() hasDeclareOption: boolean;
    @Input() disablePropertyValue: boolean;
    @Input() hideCheckBox: boolean;
    @Input() rootProperty: PropertyFEModel;

    @Output('propertyChanged') emitter: EventEmitter<void> = new EventEmitter<void>();
    @Output() expandChild: EventEmitter<string> = new EventEmitter<string>();
    @Output() checkProperty: EventEmitter<string> = new EventEmitter<string>();
    @Output() toggleTosca: EventEmitter<DerivedFEProperty> = new EventEmitter<DerivedFEProperty>();
    @Output() deleteItem: EventEmitter<string> = new EventEmitter<string>();
    @Output() clickOnPropertyRow: EventEmitter<PropertyFEModel | DerivedFEProperty> = new EventEmitter<PropertyFEModel | DerivedFEProperty>();
    @Output() mapKeyChanged: EventEmitter<string> = new EventEmitter<string>();
    @Output() addChildPropsToParent: EventEmitter<DerivedFEProperty[]> = new EventEmitter<DerivedFEProperty[]>();

    @ViewChild('mapKeyInput') public mapKeyInput: DynamicElementComponent;

    constructor(private propertiesUtils: PropertiesUtils, private dataTypeService: DataTypeService) {
    }

    /**
     * `hidden` and `isChildOfListOrMap` are declared (and only ever assigned) on
     * DerivedFEProperty; the PropertyFEModel arm of the union has neither, so the bindings
     * that probe them for both arms need a widened view of `property`. Angular 5 templates
     * have no `$any()` cast - that arrived in 6.1 - so the cast has to live here.
     */
    public get propertyAsDerived(): DerivedFEProperty {
        return this.property as DerivedFEProperty;
    }

    ngOnInit() {
        this.isPropertyFEModel = this.property instanceof PropertyFEModel;
        this.propType = this.property.derivedDataType;
        this.propPath = (this.property instanceof PropertyFEModel) ? this.property.name : this.property.propertiesName;
        this.nestedLevel = (this.property.propertiesName.match(/#/g) || []).length;
        this.rootProperty = (this.rootProperty) ? this.rootProperty : this.property as PropertyFEModel;
        this.propertyTestsId = this.getPropertyTestsId();

        this.initConsraintsValues();
    }

    initConsraintsValues() {
        const primitiveProperties = ['string', 'integer', 'float', 'boolean', PROPERTY_TYPES.TIMESTAMP];

        // Property has constraints
        if (this.property.constraints && this.property.constraints[0]) {
            this.constraints = this.property.constraints[0].validValues;
        } else if (primitiveProperties.indexOf(this.rootProperty.type) == -1 && primitiveProperties.indexOf(this.property.type) >= 0 ) {
            this.constraints = this.dataTypeService.getConstraintsByParentTypeAndUniqueID(this.rootProperty.type, this.property.name);
        } else {
            this.constraints = null;
        }

    }

    ngDoCheck() {
        // set custom error for mapKeyInput
        if (this.mapKeyInput) {
            const mapKeyInputControl = this.mapKeyInput.cmpRef.instance.control;
            const mapKeyError = (this.property as DerivedFEProperty).mapKeyError;
            if (mapKeyInputControl.getError('mapKeyError') !== mapKeyError) {
                mapKeyInputControl.setErrors({mapKeyError});
            }
        }
    }

    ngOnChanges() {
        this.propType = this.property.derivedDataType;
        this.propPath = (this.property instanceof PropertyFEModel) ? this.property.name : this.property.propertiesName;
        this.rootProperty = (this.rootProperty) ? this.rootProperty : this.property as PropertyFEModel;
        this.propertyTestsId = this.getPropertyTestsId();
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

    toggleToscaFunction = (prop: DerivedFEProperty) => {
        this.toggleTosca.emit(prop);
    }

    getHasChildren = (property: DerivedFEProperty): boolean => {// enter to this function only from base property (PropertyFEModel) and check for child property if it has children
        return _.filter((this.property as PropertyFEModel).flattenedChildren, (prop: DerivedFEProperty) => {
            return _.startsWith(prop.propertiesName + '#', property.propertiesName);
        }).length > 1;
    }

    getPropertyTestsId = () => {
        return [this.rootProperty.name].concat(this.rootProperty.getParentNamesArray(this.property.propertiesName, [], true)).join('.');
    }

    onElementChanged = (event: IUiElementChangeEvent) => {
        this.property.updateValueObj(event.value, event.isValid);
        if (this.property.hasValueObjChanged()) {
            this.checkboxDisabled = true;
        }
        if (event.value === '' || event.value === null || event.value === undefined) {
            this.checkboxDisabled = false;
        }
        this.emitter.emit();
    }

    createNewChildProperty = (): void => {

        let parentToscaFunction = null;
        if (this.property.type == PROPERTY_TYPES.MAP && this.property instanceof DerivedFEProperty && this.property.mapInlist) {
            parentToscaFunction = this.property.toscaFunction;
            this.property.toscaFunction = null;
        }
        const newProps: DerivedFEProperty[] = this.propertiesUtils.createListOrMapChildren(this.property, '', null);

        this.propertiesUtils.assignFlattenedChildrenValues(this.property.valueObj, [newProps[0]], this.property.propertiesName);
        if (this.property instanceof PropertyFEModel) {
            this.addChildProps(newProps, this.property.name);
        } else {
            this.addChildPropsToParent.emit(newProps);
        }
        this.property.toscaFunction = parentToscaFunction;
    }

    addChildProps = (newProps: DerivedFEProperty[], childPropName: string) => {

        if (this.property instanceof PropertyFEModel) {
            const insertIndex: number = this.property.getIndexOfChild(childPropName) + this.property.getCountOfChildren(childPropName); // insert after parent prop and existing children
            this.property.flattenedChildren.splice(insertIndex, 0, ...newProps); // using ES6 spread operator
            this.expandChildById(newProps[0].propertiesName);

            this.updateMapKeyValueOnMainParent(newProps);
        }
    }

    updateMapKeyValueOnMainParent(childrenProps: DerivedFEProperty[]) {
        if (this.property instanceof PropertyFEModel) {
            const property: PropertyFEModel = this.property as PropertyFEModel;
            // Update only if all this property parents has key name
            if (property.getParentNamesArray(childrenProps[0].propertiesName, []).indexOf('') === -1) {
                _.forEach(childrenProps, (prop: DerivedFEProperty): void => { // Update parent PropertyFEModel with value for each child, including nested props
                    property.childPropUpdated(prop);
                    if (prop.isChildOfListOrMap && prop.mapKey !== undefined) {
                        property.childPropMapKeyUpdated(prop, prop.mapKey, true);
                    }
                });
                // grab the cumulative value for the new item from parent PropertyFEModel and assign that value to DerivedFEProp[0] (which is the list or map parent with UUID of the set we just added)
                const parentNames = (property as PropertyFEModel).getParentNamesArray(childrenProps[0].propertiesName, []);
                childrenProps[0].valueObj = _.get(property.valueObj, parentNames.join('.'), null);
            }
        }
    }

    childValueChanged = (property: DerivedFEProperty) => { // value of child property changed

        if (this.property instanceof PropertyFEModel) { // will always be the case
            if (this.property.getParentNamesArray(property.propertiesName, []).indexOf('') === -1) {// If one of the parents is empty key -don't save
                this.property.childPropUpdated(property);
                this.dataTypeService.checkForCustomBehavior(this.property);
                this.emitter.emit();
            }
        }
    }

    deleteListOrMapItem = (item: DerivedFEProperty) => {
        if (this.property instanceof PropertyFEModel) {
            const childMapKey = item.mapKey;
            this.removeValueFromParent(item);
            this.property.flattenedChildren.splice(this.property.getIndexOfChild(item.propertiesName), this.property.getCountOfChildren(item.propertiesName));
            this.expandChildById(item.propertiesName);
            if (this.property.type == PROPERTY_TYPES.LIST && this.property.schemaType == PROPERTY_TYPES.MAP && childMapKey != null) {
                const valueObject = JSON.parse(this.property.value);
                const innerObject = valueObject[item.parentMapKey];
                delete innerObject[childMapKey];
                this.property.valueObj = valueObject;
                this.property.value = JSON.stringify(valueObject);
                this.property.flattenedChildren[0].valueObj = valueObject;
                this.property.flattenedChildren[0].value = JSON.stringify(valueObject);
                this.property.flattenedChildren[0].valueObjIsChanged = true;
            }
        }
    }

    removeValueFromParent = (item: DerivedFEProperty) => {
        if (this.property instanceof PropertyFEModel) {
            const itemParent = (item.parentName == this.property.name)
                ? this.property : this.property.flattenedChildren.find((prop) => prop.propertiesName == item.parentName);
            if (!itemParent) {
                return;
            }
            const oldKey = item.getActualMapKey();
            if (this.property.subPropertyToscaFunctions !== null) {
                const deletedIndex = item.toscaPath.length > 0 ? Number(item.toscaPath[item.toscaPath.length - 1]) : null;
                const parentIndex = item.toscaPath.length > 1 ? item.toscaPath.splice(0, item.toscaPath.length - 2) : null;
                const tempSubToscaFunction: SubPropertyToscaFunction[] = [];
                const toscaPathMap = new Map();
                this.property.subPropertyToscaFunctions.forEach((subToscaItem: SubPropertyToscaFunction) => {
                    if ((subToscaItem.subPropertyPath.toString()).indexOf(item.toscaPath.toString()) == -1) {
                        tempSubToscaFunction.push(subToscaItem);
                    } else {
                        if (item.derivedDataType == DerivedPropertyType.LIST ) {
                            if (parentIndex != null) {
                                if ((subToscaItem.subPropertyPath.toString()).indexOf(parentIndex.toString()) != -1
                                    && subToscaItem.subPropertyPath.length > parentIndex.length) {
                                    const nextIndex = Number(subToscaItem.subPropertyPath[parentIndex.length]);
                                    if (!isNaN(nextIndex) && !isNaN(deletedIndex) && nextIndex > deletedIndex) {
                                        const revisedPAth = subToscaItem.subPropertyPath;
                                        revisedPAth[parentIndex.length] = (nextIndex - 1).toString();
                                        toscaPathMap.set(subToscaItem.subPropertyPath.toString(), revisedPAth.toString());
                                    }
                                }
                            } else {
                                if (subToscaItem.subPropertyPath.length == 1 && !isNaN(deletedIndex)) {
                                    const nextElementIndex = Number(subToscaItem.subPropertyPath[0]);
                                    if (!isNaN(nextElementIndex) && nextElementIndex > deletedIndex) {
                                        subToscaItem.subPropertyPath[0] = (nextElementIndex - 1).toString();
                                    }
                                }
                            }
                        }
                    }
                });
                this.property.subPropertyToscaFunctions = tempSubToscaFunction;
                if (item.derivedDataType == DerivedPropertyType.LIST && parentIndex != null && toscaPathMap.size > 0) {
                    this.property.flattenedChildren.forEach((childProperties: DerivedFEProperty) => {
                        if (toscaPathMap.has(childProperties.toscaPath.toString())) {
                            childProperties.toscaPath = toscaPathMap.get(childProperties.toscaPath.toString());
                        }
                    });
                }
            }
            if (item.derivedDataType == DerivedPropertyType.MAP && !item.mapInlist) {
                delete itemParent.valueObj[oldKey];
                if (itemParent instanceof PropertyFEModel) {
                    delete itemParent.valueObjValidation[oldKey];
                    itemParent.valueObjIsValid = itemParent.calculateValueObjIsValid();
                }
                this.property.childPropMapKeyUpdated(item, null);  // remove map key
            } else {
                const itemIndex: number = this.property.flattenedChildren.filter((prop) => prop.parentName == item.parentName).map((prop) => prop.propertiesName).indexOf(item.propertiesName);
                itemParent.valueObj.splice(itemIndex, 1);
                if (itemParent instanceof PropertyFEModel) {
                    itemParent.valueObjValidation.splice(itemIndex, 1);
                    itemParent.valueObjIsValid = itemParent.calculateValueObjIsValid();
                }
            }
            if (itemParent instanceof PropertyFEModel) { // direct child
                this.emitter.emit();
            } else { // nested child - need to update parent prop by getting flattened name (recurse through parents and replace map/list keys, etc)
                this.childValueChanged(itemParent);
            }
        }
    }

    updateChildKeyInParent(childProp: DerivedFEProperty, newMapKey: string) {
        if (this.property instanceof PropertyFEModel) {
            const oldKey = childProp.getActualMapKey();
            const oldToscaPath = childProp.toscaPath;
            this.property.childPropMapKeyUpdated(childProp, newMapKey);
            this.updateChildMapKey(this.property.flattenedChildren, childProp.propertiesName, newMapKey);
            if (this.property.subPropertyToscaFunctions != null) {
                this.property.subPropertyToscaFunctions.forEach((item: SubPropertyToscaFunction) => {
                    if (item.subPropertyPath === oldToscaPath) {
                        item.subPropertyPath = childProp.toscaPath;
                    }
                });
            }
            this.emitter.emit();
        }
    }

    updateChildMapKey(childProps: DerivedFEProperty[], parentName: string, newMapKey: string) {
        childProps.forEach((tempDervObj) => {
            if (parentName === tempDervObj.parentName) {
                tempDervObj.mapKey = newMapKey;
                tempDervObj.toscaPath[tempDervObj.toscaPath.length - 2] = newMapKey;
            }
        });
    }

    preventInsertItem = (property: DerivedFEProperty): boolean => {
        if (property.type == PROPERTY_TYPES.MAP && property.valueObj != null && Object.keys(property.valueObj).indexOf('') > -1 ) {
            return true;
        }
        return false;
    }

}
