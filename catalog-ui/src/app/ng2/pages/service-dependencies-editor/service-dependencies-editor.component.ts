/*!
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import {Component} from '@angular/core';
import {InputBEModel, PropertyBEModel, PropertyFEModel} from 'app/models';
import {
  ConstraintObjectUI,
  OPERATOR_TYPES
} from 'app/ng2/components/logic/service-dependencies/service-dependencies.component';
import {DropdownValue} from 'app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component';
import {ServiceServiceNg2} from 'app/ng2/services/component-services/service.service';
import {PROPERTY_DATA} from 'app/utils';
import {ServiceInstanceObject} from '../../../models/service-instance-properties-and-interfaces';
import { PropertiesUtils } from '../properties-assignment/services/properties.utils';

export class UIDropDownSourceTypesElement extends DropdownValue {
  options: any[];
  assignedLabel: string;
  type: string;

  constructor(input?: any) {
    super(input ? input.value || '' : "", input ? input.label || '' : "");
    if (input) {
      this.options = input.options;
      this.assignedLabel = input.assignedLabel;
      this.type = input.type;
    }
  }
}

// tslint:disable-next-line:max-classes-per-file
@Component({
  selector: 'service-dependencies-editor',
  templateUrl: './service-dependencies-editor.component.html',
  styleUrls: ['./service-dependencies-editor.component.less'],
  providers: [ServiceServiceNg2]
})

export class ServiceDependenciesEditorComponent {

  input: {
    serviceRuleIndex: number,
    serviceRules: ConstraintObjectUI[],
    compositeServiceName: string,
    currentServiceName: string,
    parentServiceInputs: InputBEModel[],
    parentServiceProperties: PropertyBEModel[];
    selectedInstanceProperties: PropertyBEModel[],
    operatorTypes: DropdownValue[],
    selectedInstanceSiblings: ServiceInstanceObject[]
  };
  currentServiceName: string;
  selectedServiceProperties: PropertyBEModel[];
  selectedPropertyObj: PropertyFEModel;
  ddValueSelectedServicePropertiesNames: DropdownValue[];
  operatorTypes: DropdownValue[];
  functionTypes: DropdownValue[];
  sourceTypes: UIDropDownSourceTypesElement[] = [];
  currentRule: ConstraintObjectUI;
  currentIndex: number;
  listOfValuesToAssign: DropdownValue[];
  listOfSourceOptions: PropertyBEModel[];
  assignedValueLabel: string;
  serviceRulesList: ConstraintObjectUI[];

  SOURCE_TYPES = {
    STATIC: {label: 'Static', value: 'static'},
    SERVICE_PROPERTY: {label: 'Service Property', value: 'property'},
    SERVICE_INPUT: {label: 'Service Input', value: 'service_input'}
  };

  constructor(private propertiesUtils: PropertiesUtils) {}

  ngOnInit() {
    this.currentIndex = this.input.serviceRuleIndex;
    this.serviceRulesList = this.input.serviceRules;
    this.initFunctionTypes();
    this.initCurrentRule();
    this.currentServiceName = this.input.currentServiceName;
    this.operatorTypes = this.input.operatorTypes;
    this.selectedServiceProperties = this.input.selectedInstanceProperties;
    this.ddValueSelectedServicePropertiesNames = _.map(this.input.selectedInstanceProperties, (prop) => new DropdownValue(prop.name, prop.name));
    if (this.SOURCE_TYPES.STATIC.value !== this.currentRule.sourceType) {
      this.loadSourceTypesData();
    }
    this.syncRuleData();
  }

  private initCurrentRule() {
    this.currentRule = this.serviceRulesList && this.input.serviceRuleIndex >= 0 ?
        this.serviceRulesList[this.input.serviceRuleIndex] :
        new ConstraintObjectUI({
          sourceName: this.SOURCE_TYPES.STATIC.value,
          sourceType: this.SOURCE_TYPES.STATIC.value,
          value: '',
          constraintOperator: OPERATOR_TYPES.EQUAL
        });
    if (this.currentRule && this.currentRule.sourceType === this.SOURCE_TYPES.STATIC.value){
      this.sourceTypes.push({
        label: this.SOURCE_TYPES.STATIC.label,
        value: this.SOURCE_TYPES.STATIC.value,
        assignedLabel: this.SOURCE_TYPES.STATIC.value,
        type: this.SOURCE_TYPES.STATIC.value,
        options: []});
    }
  }

  private initFunctionTypes() {
    this.functionTypes = [
      {label: this.SOURCE_TYPES.STATIC.label, value: this.SOURCE_TYPES.STATIC.value},
      {label: this.SOURCE_TYPES.SERVICE_PROPERTY.label, value: this.SOURCE_TYPES.SERVICE_PROPERTY.value},
      {label: this.SOURCE_TYPES.SERVICE_INPUT.label, value: this.SOURCE_TYPES.SERVICE_INPUT.value}];
  }

  onServicePropertyChanged() {
    if(this.SOURCE_TYPES.SERVICE_INPUT.value === this.currentRule.sourceType || this.SOURCE_TYPES.SERVICE_PROPERTY.value === this.currentRule.sourceType){
      this.currentRule.sourceName = "SELF";
    } else {
      this.currentRule.sourceName = "";
    }
    this.updateSelectedPropertyObj();
    this.updateOperatorTypesList();
    this.updateSourceTypesRelatedValues();
    this.currentRule.value = "";
  }

  onSelectFunctionType(value: any) {
    this.currentRule.sourceName = "";
    this.listOfValuesToAssign = [];
    this.currentRule.sourceType = value;
    this.loadSourceTypesData();
    this.updateSourceTypesRelatedValues();
  }

  private loadSourceTypesData() {
    const SELF = "SELF";
    if (this.SOURCE_TYPES.SERVICE_INPUT.value === this.currentRule.sourceType || this.SOURCE_TYPES.SERVICE_PROPERTY.value === this.currentRule.sourceType) {
      this.currentRule.sourceName = SELF;
    }
    this.sourceTypes = [];
    this.sourceTypes.push({
      label: SELF,
      value: SELF,
      assignedLabel: this.currentRule.sourceType == this.SOURCE_TYPES.SERVICE_PROPERTY.value
          ? this.SOURCE_TYPES.SERVICE_PROPERTY.label : this.SOURCE_TYPES.SERVICE_INPUT.label,
      type: this.currentRule.sourceType == this.SOURCE_TYPES.SERVICE_PROPERTY.value
          ? this.SOURCE_TYPES.SERVICE_PROPERTY.value : this.SOURCE_TYPES.SERVICE_INPUT.value,
      options: this.loadSourceTypeBySelectedFunction().get(this.currentRule.sourceType)
    });

    if (this.currentRule.sourceType !== this.SOURCE_TYPES.SERVICE_INPUT.value) {
      if (this.input.selectedInstanceSiblings && this.isPropertyFunctionSelected) {
        _.forEach(this.input.selectedInstanceSiblings, (sib) =>
            this.sourceTypes.push({
              label: sib.name,
              value: sib.name,
              options: sib.properties || [],
              assignedLabel: this.SOURCE_TYPES.SERVICE_PROPERTY.label,
              type: this.SOURCE_TYPES.SERVICE_PROPERTY.value
            })
        );
      }
    }
  }

  loadSourceTypeBySelectedFunction = (): any => {
    let parentDataMap = new Map();
    parentDataMap.set(this.SOURCE_TYPES.SERVICE_PROPERTY.value, this.input.parentServiceProperties);
    parentDataMap.set(this.SOURCE_TYPES.SERVICE_INPUT.value , this.input.parentServiceInputs);
    return parentDataMap;
  }

  syncRuleData() {
    if (!this.currentRule.sourceName || this.currentRule.sourceType === this.SOURCE_TYPES.STATIC.value) {
      this.currentRule.sourceName = this.SOURCE_TYPES.STATIC.value;
      this.currentRule.sourceType = this.SOURCE_TYPES.STATIC.value;
    }
    this.updateSelectedPropertyObj();
    this.updateOperatorTypesList();
    this.updateSourceTypesRelatedValues();
  }

  updateOperatorTypesList() {
    if (this.selectedPropertyObj && PROPERTY_DATA.SIMPLE_TYPES_COMPARABLE.indexOf(this.selectedPropertyObj.type) === -1) {
      this.operatorTypes = [{label: '=', value: OPERATOR_TYPES.EQUAL}];
      this.currentRule.constraintOperator = OPERATOR_TYPES.EQUAL;
    } else {
      this.operatorTypes = this.input.operatorTypes;
    }
  }

  updateSourceTypesRelatedValues() {
    if (this.currentRule.sourceName) {
      const selectedSourceType: UIDropDownSourceTypesElement = this.sourceTypes.find(
          (t) => t.value === this.currentRule.sourceName && t.type === this.currentRule.sourceType
      );
      if (selectedSourceType) {
        this.listOfSourceOptions = [];
        this.listOfSourceOptions = selectedSourceType.options || [];
        this.assignedValueLabel = selectedSourceType.assignedLabel || this.SOURCE_TYPES.STATIC.label;
        this.filterOptionsByType();
      }
    }
  }

  onChangePage(newIndex:any) {
    if (newIndex >= 0 && newIndex < this.input.serviceRules.length) {
      this.currentIndex = newIndex;
      this.currentRule = this.serviceRulesList[newIndex];
      this.syncRuleData();
    }
  }

  filterOptionsByType() {
    if (!this.selectedPropertyObj) {
      this.listOfValuesToAssign = [];
      return;
    }
    this.listOfValuesToAssign = this.listOfSourceOptions.reduce((result, op: PropertyBEModel) => {
      if (op.type === this.selectedPropertyObj.type && (!op.schemaType || op.schemaType === this.selectedPropertyObj.schemaType)) {
        result.push(new DropdownValue(op.name, op.name));
      }
      return result;
    }, []);
  }

  onValueChange(isValidValue:any) {
    this.currentRule.updateValidity(isValidValue);
  }

  checkFormValidForSubmit() {
    if (!this.serviceRulesList) { // for create modal
      const isStatic = this.currentRule.sourceName === this.SOURCE_TYPES.STATIC.value;
      return this.currentRule.isValidRule(isStatic);
    }

    // for update all rules
    return this.serviceRulesList.every((rule) => rule.isValidRule(rule.sourceName === this.SOURCE_TYPES.STATIC.value));
  }

  updateSelectedPropertyObj(): void {
    this.selectedPropertyObj = null;
    if (this.currentRule.servicePropertyName) {
      let newProp = new PropertyFEModel(_.find(this.selectedServiceProperties, (prop) => prop.name === this.currentRule.servicePropertyName));
      newProp.value = JSON.stringify(this.currentRule.value);
      this.propertiesUtils.initValueObjectRef(newProp);
      console.log("TEST" + newProp.value);
      setTimeout(() => {
        this.selectedPropertyObj = newProp})
      this.selectedPropertyObj = newProp;
    }
  }

  isStaticSource(): boolean {
    return this.currentRule.sourceType === this.SOURCE_TYPES.STATIC.value
  }

  isPropertyFunctionSelected(): boolean {
    return this.currentRule.sourceType === this.SOURCE_TYPES.SERVICE_PROPERTY.value;
  }

  isComplexListMapType(): boolean {
    return this.selectedPropertyObj && this.selectedPropertyObj.derivedDataType > 0;
  }

  updateComplexListMapTypeRuleValue(): void {
    let value = PropertyFEModel.cleanValueObj(this.selectedPropertyObj.valueObj);
    this.currentRule.value = JSON.stringify(value);
    this.onValueChange(this.selectedPropertyObj.valueObjIsValid);
  }

  getValue(event: Event): string | number {
      return (event.target as HTMLInputElement).value;
  }

  isObjTypeOf(obj: any, type: string): boolean {
      return typeof(obj) === type;
  }

}
