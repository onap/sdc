/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
*  Copyright (C) 2020 Nordix Foundation. All rights reserved.
*  ================================================================================
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*  SPDX-License-Identifier: Apache-2.0
*  ============LICENSE_END=========================================================
*/

import {Component} from '@angular/core';
import {InputBEModel, PropertyBEModel, PropertyModel} from 'app/models';
import {DropdownValue} from 'app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component';
import {ServiceServiceNg2} from 'app/ng2/services/component-services/service.service';
import {PROPERTY_DATA} from 'app/utils';
import {ServiceInstanceObject} from '../../../../models/service-instance-properties-and-interfaces';
import {CapabilityFilterConstraintUI} from "../../../../models/capability-filter-constraint";
import {OPERATOR_TYPES} from "../../../../utils/filter-constraint-helper";

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

@Component({
  selector: 'app-capabilities-filter-properties-editor',
  templateUrl: './capabilities-filter-properties-editor.component.html',
  styleUrls: ['./capabilities-filter-properties-editor.component.less'],
  providers: [ServiceServiceNg2]
})
export class CapabilitiesFilterPropertiesEditorComponent {

  input: {
    serviceRuleIndex: number,
    serviceRules: CapabilityFilterConstraintUI[],
    compositeServiceName: string,
    currentServiceName: string,
    parentServiceInputs: InputBEModel[],
    selectedInstanceProperties: PropertyBEModel[],
    operatorTypes: DropdownValue[],
    selectedInstanceSiblings: ServiceInstanceObject[],
    componentInstanceCapabilitiesMap: Map<string, PropertyModel[]>,
  };
  currentServiceName: string;
  selectedServiceProperties: PropertyBEModel[];
  operatorTypes: DropdownValue[];
  sourceTypes: UIDropDownSourceTypesElement[] = [];
  currentRule: CapabilityFilterConstraintUI;
  currentIndex: number;
  listOfValuesToAssign: DropdownValue[];
  listOfSourceOptions: PropertyBEModel[];
  assignedValueLabel: string;
  serviceRulesList: CapabilityFilterConstraintUI[];

  capabilitiesNames: string[];
  selectedPropertiesByCapabilityName: Array<PropertyModel>;
  selectedCapabilityName: string;
  capabilityProperties: DropdownValue[];

  selectedCapabilitiesPropertyObject: PropertyBEModel;

  SOURCE_TYPES = {
    STATIC: {label: 'Static', value: 'static'},
    SERVICE_PROPERTY: {label: 'Service Property', value: 'property'},
    CAPABILITY_NAME: {label: 'Name', value: 'name'}
  };

  ngOnInit() {
    this.capabilitiesNames = Array.from(this.input.componentInstanceCapabilitiesMap.keys());
    this.currentIndex = this.input.serviceRuleIndex;
    this.serviceRulesList = this.input.serviceRules;
    this.currentRule = this.serviceRulesList && this.input.serviceRuleIndex >= 0 ?
        this.serviceRulesList[this.input.serviceRuleIndex] :
        new CapabilityFilterConstraintUI({
          capabilityName: this.SOURCE_TYPES.CAPABILITY_NAME.value,
          sourceName: this.SOURCE_TYPES.STATIC.value,
          sourceType: this.SOURCE_TYPES.STATIC.value, value: '',
          constraintOperator: OPERATOR_TYPES.EQUAL
        });
    this.currentServiceName = this.input.currentServiceName;
    this.operatorTypes = this.input.operatorTypes;

    this.initSourceTypes();
    this.syncRuleData();
    this.updateSourceTypesRelatedValues();
    this.onCapabilityNameChanged(this.currentRule.capabilityName)
  }

  initSourceTypes() {
    this.sourceTypes.push({
      label: this.SOURCE_TYPES.STATIC.label,
      value: this.SOURCE_TYPES.STATIC.value,
      options: [],
      assignedLabel: this.SOURCE_TYPES.STATIC.label,
      type: this.SOURCE_TYPES.STATIC.value
    });
    this.sourceTypes.push({
      label: this.input.compositeServiceName,
      value: this.input.compositeServiceName,
      assignedLabel: this.SOURCE_TYPES.SERVICE_PROPERTY.label,
      type: this.SOURCE_TYPES.SERVICE_PROPERTY.value,
      options: this.input.parentServiceInputs
    });
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

  syncRuleData() {
    if (!this.currentRule.sourceName && this.currentRule.sourceType === this.SOURCE_TYPES.STATIC.value) {
      this.currentRule.sourceName = this.SOURCE_TYPES.STATIC.value;
    }
    if (this.input.componentInstanceCapabilitiesMap) {
      this.selectedCapabilitiesPropertyObject = Array.from(this.input.componentInstanceCapabilitiesMap
      .get(this.currentRule.capabilityName))
      .find(property => property.name == this.currentRule.servicePropertyName);
    }
    this.updateOperatorTypesList();
    this.updateSourceTypesRelatedValues();
  }

  updateOperatorTypesList() {
    if (this.selectedCapabilitiesPropertyObject && PROPERTY_DATA.SIMPLE_TYPES_COMPARABLE.indexOf(this.selectedCapabilitiesPropertyObject.type) === -1) {
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
        this.listOfSourceOptions = selectedSourceType.options || [];
        this.assignedValueLabel = selectedSourceType.assignedLabel || this.SOURCE_TYPES.STATIC.label;
        this.filterOptionsByType();
      }
    }
  }

  onCapabilityNameChanged = (value: any): void => {
    this.selectedPropertiesByCapabilityName = this.input.componentInstanceCapabilitiesMap.get(value);
    this.capabilityProperties = _.map(this.selectedPropertiesByCapabilityName, (prop) => new DropdownValue(prop.name, prop.name));
    this.selectedCapabilityName = value;
    this.updateOperatorTypesList();
    this.filterOptionsByType();
  }

  onServicePropertyChanged() { 
    this.syncRuleData();
    this.filterOptionsByType();
    this.currentRule.value = '';
  }

  onSelectSourceType() {
    this.currentRule.sourceType = this.currentRule.sourceName === this.SOURCE_TYPES.STATIC.value ?
        this.SOURCE_TYPES.STATIC.value :
        this.SOURCE_TYPES.SERVICE_PROPERTY.value;
    this.updateSourceTypesRelatedValues();
    this.currentRule.value = '';
  }

  filterOptionsByType() {
    if (!this.selectedCapabilitiesPropertyObject) {
      this.listOfValuesToAssign = [];
      return;
    }
    this.listOfValuesToAssign = this.listOfSourceOptions.reduce((result, op: PropertyModel) => {
      if (op.type === this.selectedCapabilitiesPropertyObject.type && (!op.schemaType || op.schemaType === this.selectedCapabilitiesPropertyObject.schemaType)) {
        result.push(new DropdownValue(op.name, op.name));
      }
      return result;
    }, []);
  }

  onValueChange(isValidValue) {
    this.currentRule.updateValidity(isValidValue);
  }

  checkFormValidForSubmit() {
    if (!this.serviceRulesList) {
      const isStatic = this.currentRule.sourceName === this.SOURCE_TYPES.STATIC.value;
      return this.currentRule.isValidRule(isStatic);
    }
    return this.serviceRulesList.every((rule) => rule.isValidRule(rule.sourceName === this.SOURCE_TYPES.STATIC.value));
  }

}
