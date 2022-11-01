/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {DataTypeModel} from '../../../../../../../models/data-types';
import {SchemaPropertyGroupModel} from '../../../../../../../models/schema-property';
import {DerivedPropertyType, PropertyBEModel} from '../../../../../../../models/properties-inputs/property-be-model';
import {PROPERTY_DATA, PROPERTY_TYPES} from '../../../../../../../utils/constants';
import {ToscaFunction} from '../../../../../../../models/tosca-function';
import {ToscaFunctionValidationEvent} from "../../../../../../../ng2/pages/properties-assignment/tosca-function/tosca-function.component";
import {InstanceFeDetails} from "../../../../../../../models/instance-fe-details";

@Component({
  selector: 'app-input-list-item',
  templateUrl: './input-list-item.component.html',
  styleUrls: ['./input-list-item.component.less']
})
export class InputListItemComponent implements OnInit {

  @Input() valueObjRef: any;
  @Input() name: string;
  @Input() dataTypeMap: Map<string, DataTypeModel>;
  @Input() type: DataTypeModel;
  @Input() schema: SchemaPropertyGroupModel;
  @Input() nestingLevel: number;
  @Input() isListChild: boolean = false;
  @Input() isMapChild: boolean = false;
  @Input() showToscaFunctionOption: boolean = false;
  @Input() listIndex: number;
  @Input() isViewOnly: boolean;
  @Input() allowDeletion: boolean = false;
  @Input() toscaFunction: ToscaFunction;
  @Input() componentInstanceMap: Map<string, InstanceFeDetails> = new Map();
  @Output('onValueChange') onValueChangeEvent: EventEmitter<any> = new EventEmitter<any>();
  @Output('onDelete') onDeleteEvent: EventEmitter<string> = new EventEmitter<string>();
  @Output('onChildListItemDelete') onChildListItemDeleteEvent: EventEmitter<number> = new EventEmitter<number>();

  isExpanded: boolean = false;
  mapEntryName: string;
  isToscaFunction: boolean = false;
  property: PropertyBEModel;

  ngOnInit() {
    if (!this.nestingLevel) {
      this.nestingLevel = 0;
    }
    if (this.type.properties) {
      this.type.properties.forEach(property => {
        this.initEmptyPropertyInValueObjRef(property);
      });
    }

    this.property = new PropertyBEModel();
    this.property.type = this.type.name;
    if (this.schema) {
      this.property.schema = this.schema;
      this.property.schemaType = this.schema.property.type;
    }
    if (this.toscaFunction) {
      this.property.toscaFunction = this.toscaFunction;
      this.valueObjRef = this.toscaFunction.value;
      this.isToscaFunction = true;
    }
  }

  private initEmptyPropertyInValueObjRef(property: PropertyBEModel) {
    if (this.valueObjRef[property.name] == undefined) {
      if (this.isTypeComplex(property.type) || this.isTypeMap(property.type)) {
        this.valueObjRef[property.name] = {};
      } else if (this.isTypeList(property.type) || this.isTypeRange(property.type)) {
        this.valueObjRef[property.name] = [];
      } else {
        this.valueObjRef[property.name] = null;
      }
    }
  }

  getType(typeName: string): DerivedPropertyType {
    if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(typeName) > -1) {
      return DerivedPropertyType.SIMPLE;
    } else if (typeName === PROPERTY_TYPES.LIST) {
      return DerivedPropertyType.LIST;
    } else if (typeName === PROPERTY_TYPES.MAP) {
      return DerivedPropertyType.MAP;
    } else if (typeName === PROPERTY_TYPES.RANGE) {
      return DerivedPropertyType.RANGE;
    } else {
      return DerivedPropertyType.COMPLEX;
    }
  }

  isTypeWithoutProperties(typeName: string): boolean {
      if (this.dataTypeMap.get(typeName) === undefined) {
          return true;
      }
      return this.dataTypeMap.get(typeName).properties === undefined ||
          this.dataTypeMap.get(typeName).properties.length == 0;
  }

  isTypeDerivedFromSimple(typeName: string): boolean {
      if (this.dataTypeMap.get(typeName) === undefined) {
          return true;
      }
      if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.dataTypeMap.get(typeName).derivedFromName) > -1) {
          return true;
      } else {
          while (this.dataTypeMap.get(typeName).derivedFromName != PROPERTY_DATA.ROOT_DATA_TYPE) {
              if (this.isTypeDerivedFromSimple(this.dataTypeMap.get(typeName).derivedFromName)) {
                  return true;
              }
          }
      }
  }

  isTypeSimple(typeName: string): boolean {
    if (typeName === undefined) {
        return false;
    }
    if (this.isTypeDerivedFromSimple(typeName) && (this.isTypeWithoutProperties(typeName))) {
        return true;
    }
    return this.getType(typeName) == DerivedPropertyType.SIMPLE;
  }

  isTypeRange(typeName: string): boolean {
      return !this.isTypeSimple(typeName) && this.getType(typeName) == DerivedPropertyType.RANGE;
  }

  isTypeList(typeName: string): boolean {
    return this.getType(typeName) == DerivedPropertyType.LIST;
  }

  isTypeMap(typeName: string): boolean {
    return this.getType(typeName) == DerivedPropertyType.MAP;
  }

  isTypeComplex(typeName: string): boolean {
    return !this.isTypeSimple(typeName) && !this.isTypeList(typeName) && !this.isTypeMap(typeName) && !this.isTypeRange(typeName);
  }

  expandAndCollapse() {
    this.isExpanded = !this.isExpanded;
  }

  getDataType(type: string) {
    return this.dataTypeMap.get(type);
  }

  onValueTypeChange () {
    if ( !this.isToscaFunction ) {
      this.onValueChange(this.valueObjRef);
    }
  }

  onToscaFunctionValidityChange(validationEvent: ToscaFunctionValidationEvent):void {
    if (validationEvent.isValid) {
      this.emitValueChangeEvent(validationEvent.toscaFunction, true);
      return;
    }
    this.emitValueChangeEvent(undefined, true);
  }

  onValueChange(value: any): void {
    if (this.isNumber(this.type.name)) {
      this.emitValueChangeEvent(this.parseNumber(value));
      return;
    }
    if (this.type.name == PROPERTY_TYPES.BOOLEAN) {
      this.emitValueChangeEvent(this.parseBoolean(value));
      return;
    }
    this.emitValueChangeEvent(value);
  }

  onListValueChange(): void {
    this.emitValueChangeEvent(this.valueObjRef);
  }

  onPropertyValueChange($event: any) {
    this.valueObjRef[$event.name] = $event.value;
    this.emitValueChangeEvent(this.valueObjRef);
  }

  private emitValueChangeEvent(value: any, isToscaFunction=false) {
    let emitValue = {
      name: this.name,
      value: value,
      isToscaFunction:isToscaFunction
    };
    this.onValueChangeEvent.emit(emitValue);
  }

  isRoot(): boolean {
    return this.nestingLevel === 0;
  }

  showListItemDelete(): boolean {
    return !this.isViewOnly && (this.isListChild && this.nestingLevel > 0);
  }

  showInputDelete(): boolean {
    return this.allowDeletion && !this.isViewOnly && (this.isRoot() || this.isMapChild);
  }

  resolveType(): string {
    if (this.isTypeList(this.type.name)) {
      return `list of value type ${this.schema.property.type}`
    }
    if (this.isTypeMap(this.type.name)) {
      return `map of 'string' keys and '${this.schema.property.type}' values`
    }
    return this.type.name;
  }

  onInputDelete() {
    this.onDeleteEvent.emit(this.name);
  }

  onListItemDelete(index: number): void {
    this.valueObjRef.splice(index, 1);
    this.emitValueChangeEvent(this.valueObjRef);
  }

  addListElement() {
    if (!this.valueObjRef) {
      this.valueObjRef = [];
    }
    if (this.isTypeSimple(this.schema.property.type)) {
      this.valueObjRef.push('');
    } else if (this.isTypeComplex(this.schema.property.type) || this.isTypeMap(this.schema.property.type)) {
      this.valueObjRef.push({});
    } else if (this.isTypeList(this.schema.property.type)) {
      this.valueObjRef.push([]);
    }
  }

  trackByIndex(index: number, value: string): number {
    return index;
  }

  onChildListItemDelete() {
    this.onChildListItemDeleteEvent.emit(this.listIndex);
  }

  getObjectEntries(valueObjRef: object) {
    return Object.keys(valueObjRef);
  }

  onMapValueChange() {
    this.emitValueChangeEvent(this.valueObjRef);
  }

  onMapKeyDelete(key: string) {
    delete this.valueObjRef[key]
    this.emitValueChangeEvent(this.valueObjRef);
  }

  addMapEntry() {
    let newKey;
    if (this.mapEntryName) {
      newKey = this.mapEntryName.trim();
    }
    if (!newKey) {
      return;
    }
    if (Object.keys(this.valueObjRef).indexOf(newKey) !== -1) {
      return;
    }
    this.mapEntryName = '';
    if (this.isTypeSimple(this.schema.property.type)) {
      this.valueObjRef[newKey] = '';
    } else if (this.isTypeComplex(this.schema.property.type) || this.isTypeMap(this.schema.property.type)) {
      this.valueObjRef[newKey] = {};
    } else if (this.isTypeList(this.schema.property.type)) {
      this.valueObjRef[newKey] = [];
    }
    this.emitValueChangeEvent(this.valueObjRef);
  }

  getSimpleValueInputType() {
    if (this.isNumber(this.type.name)){
      return 'number';
    }
    return 'text';
  }

  isNumber(type: string): boolean {
    return type === PROPERTY_TYPES.INTEGER || type === PROPERTY_TYPES.FLOAT;
  }

  private parseBoolean(value: any) {
    if (value === 'true') {
      return true;
    }
    if (value === 'false') {
      return false;
    }
    return null;
  }

  private parseNumber(value: any) {
    const number = parseInt(value);
    return isNaN(number) ? null : number;
  }

}
