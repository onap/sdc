/*!
 * Copyright © 2016-2018 European Support Limited
 * Modification Copyright (C) 2022 Nordix Foundation.
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
import {Component, Input, OnInit} from '@angular/core';
import {InputBEModel, PropertyBEModel, PropertyFEModel, PropertyModel} from 'app/models';
import {SourceType} from 'app/ng2/components/logic/service-dependencies/service-dependencies.component';
import {DropdownValue} from 'app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component';
import {ServiceServiceNg2} from 'app/ng2/services/component-services/service.service';
import {PROPERTY_DATA} from 'app/utils';
import {PropertiesUtils} from '../properties-assignment/services/properties.utils';
import {ToscaFunctionValidationEvent} from "../properties-assignment/tosca-function/tosca-function.component";
import {InstanceFeDetails} from "../../../models/instance-fe-details";
import {CompositionService} from "../composition/composition.service";
import {ToscaGetFunction} from "../../../models/tosca-get-function";
import {PropertyFilterConstraintUi} from "../../../models/ui-models/property-filter-constraint-ui";
import {ConstraintOperatorType, FilterConstraintHelper} from "../../../utils/filter-constraint-helper";
import {ToscaFunctionHelper} from "../../../utils/tosca-function-helper";

@Component({
  selector: 'service-dependencies-editor',
  templateUrl: './service-dependencies-editor.component.html',
  styleUrls: ['./service-dependencies-editor.component.less'],
  providers: [ServiceServiceNg2]
})
export class ServiceDependenciesEditorComponent implements OnInit {

  @Input() serviceRuleIndex: number;
  @Input() serviceRules: PropertyFilterConstraintUi[];
  @Input() compositeServiceName: string;
  @Input() currentServiceName: string;
  @Input() parentServiceInputs: InputBEModel[];
  @Input() parentServiceProperties: PropertyBEModel[];
  @Input() selectedInstanceProperties: PropertyBEModel[];
  @Input() allowedOperators: ConstraintOperatorType[] = [
      ConstraintOperatorType.GREATER_THAN,
      ConstraintOperatorType.LESS_THAN,
      ConstraintOperatorType.EQUAL,
      ConstraintOperatorType.GREATER_OR_EQUAL,
      ConstraintOperatorType.LESS_OR_EQUAL
  ];
  @Input() capabilityNameAndPropertiesMap: Map<string, PropertyModel[]>;
  @Input() filterType: FilterType;
  @Input() filterConstraint: PropertyFilterConstraintUi;
  //output
  currentRule: PropertyFilterConstraintUi;

  FILTER_TYPE_CAPABILITY: FilterType = FilterType.CAPABILITY

  operatorTypes: DropdownValue[] = [
    {label: FilterConstraintHelper.convertToSymbol(ConstraintOperatorType.GREATER_THAN), value: ConstraintOperatorType.GREATER_THAN},
    {label: FilterConstraintHelper.convertToSymbol(ConstraintOperatorType.LESS_THAN), value: ConstraintOperatorType.LESS_THAN},
    {label: FilterConstraintHelper.convertToSymbol(ConstraintOperatorType.EQUAL), value: ConstraintOperatorType.EQUAL},
    {label: FilterConstraintHelper.convertToSymbol(ConstraintOperatorType.GREATER_OR_EQUAL), value: ConstraintOperatorType.GREATER_OR_EQUAL},
    {label: FilterConstraintHelper.convertToSymbol(ConstraintOperatorType.LESS_OR_EQUAL), value: ConstraintOperatorType.LESS_OR_EQUAL}
  ];

  servicePropertyDropdownList: DropdownValue[];
  isLoading: false;
  selectedProperty: PropertyFEModel;
  selectedSourceType: string;
  componentInstanceMap: Map<string, InstanceFeDetails> = new Map<string, InstanceFeDetails>();
  capabilityDropdownList: DropdownValue[] = [];

  SOURCE_TYPES = {
    STATIC: {label: 'Static', value: SourceType.STATIC},
    TOSCA_FUNCTION: {label: 'Tosca Function', value: SourceType.TOSCA_FUNCTION}
  };

  constructor(private propertiesUtils: PropertiesUtils, private compositionService: CompositionService) {}

  ngOnInit(): void {
    if (this.compositionService.componentInstances) {
      this.compositionService.componentInstances.forEach(value => {
        this.componentInstanceMap.set(value.uniqueId, <InstanceFeDetails>{
          name: value.name
        });
      });
    }
    this.initCapabilityDropdown();
    this.initCurrentRule();
    this.initConstraintOperatorOptions();
    this.initSelectedSourceType();
    this.initPropertyDropdown();
    this.syncRuleData();
  }


  private initCapabilityDropdown(): void {
    if (this.filterType == FilterType.CAPABILITY) {
      this.capabilityDropdownList = [
        new DropdownValue(undefined, 'Select'),
        ...Array.from(this.capabilityNameAndPropertiesMap.keys()).map(capabilityName => new DropdownValue(capabilityName, capabilityName))
      ];
    }
  }

  private initPropertyDropdown(): void {
    let propertyList: PropertyBEModel[] = [];
    if (this.filterType == FilterType.CAPABILITY) {
      if (this.currentRule.capabilityName) {
        propertyList = this.capabilityNameAndPropertiesMap.get(this.currentRule.capabilityName);
      }
    } else {
      propertyList = this.selectedInstanceProperties;
    }
    let selectLabel;
    if (this.filterType == FilterType.CAPABILITY) {
      selectLabel = this.currentRule.capabilityName ? 'Select' : 'Select a Capability';
    } else {
      selectLabel = 'Select';
    }
    this.servicePropertyDropdownList = [new DropdownValue(undefined, selectLabel), ...propertyList.map(prop => new DropdownValue(prop.name, prop.name))];
  }

  private initConstraintOperatorOptions(): void {
    if (!this.selectedProperty) {
      this.operatorTypes = [new DropdownValue(undefined, 'Select a Property')];
      return;
    }

    if (PROPERTY_DATA.SIMPLE_TYPES_COMPARABLE.indexOf(this.selectedProperty.type) === -1) {
      if (this.currentRule.constraintOperator !== ConstraintOperatorType.EQUAL) {
        this.currentRule.constraintOperator = ConstraintOperatorType.EQUAL;
      }
      this.operatorTypes = [new DropdownValue(ConstraintOperatorType.EQUAL, FilterConstraintHelper.convertToSymbol(ConstraintOperatorType.EQUAL))];
    } else {
      const operatorList: DropdownValue[] = [];
      this.allowedOperators.forEach(constraintOperatorType =>
        operatorList.push(new DropdownValue(constraintOperatorType, FilterConstraintHelper.convertToSymbol(constraintOperatorType)))
      );
      this.operatorTypes = operatorList;
    }
  }

  private initSelectedSourceType(): void {
    if (!this.currentRule.sourceType || this.currentRule.sourceType === SourceType.STATIC) {
      this.selectedSourceType = SourceType.STATIC;
    } else {
      this.selectedSourceType = SourceType.TOSCA_FUNCTION;
    }
  }

  private initCurrentRule(): void {
    if (this.filterConstraint) {
      this.currentRule = new PropertyFilterConstraintUi(this.filterConstraint);
    } else {
      this.currentRule = new PropertyFilterConstraintUi({
        sourceName: SourceType.STATIC,
        sourceType: SourceType.STATIC,
        constraintOperator: ConstraintOperatorType.EQUAL,
        value: undefined
      });
    }
  }

  onCapabilityChange(): void {
    this.initPropertyDropdown();
    this.resetSelectedProperty();
  }

  onPropertyChange(): void {
    this.currentRule.sourceName = undefined;
    this.currentRule.value = undefined;
    this.onValueChange(false);
    this.updateSelectedProperty();
    this.initConstraintOperatorOptions();
  }

  syncRuleData(): void {
    if (!this.currentRule.sourceName || this.currentRule.sourceType === SourceType.STATIC) {
      this.currentRule.sourceName = SourceType.STATIC;
      this.currentRule.sourceType = SourceType.STATIC;
    }
    this.initSelectedProperty();
    this.initConstraintOperatorOptions();
  }

  onValueChange(isValidValue:any): void {
    this.currentRule.updateValidity(isValidValue);
  }

  checkFormValidForSubmit(): boolean {
    return this.currentRule.isValidRule();
  }

  initSelectedProperty(): void {
    if (!this.currentRule.servicePropertyName) {
      this.selectedProperty = undefined;
      return;
    }
    let newProperty;
    if (this.filterType === FilterType.CAPABILITY) {
      const currentProperty = this.capabilityNameAndPropertiesMap.get(this.currentRule.capabilityName)
        .find(property => property.name === this.currentRule.servicePropertyName);
      newProperty = new PropertyFEModel(currentProperty);
    } else {
      newProperty = new PropertyFEModel(this.selectedInstanceProperties.find(property => property.name === this.currentRule.servicePropertyName));
    }
    newProperty.value = undefined;
    newProperty.toscaFunction = undefined;
    if (typeof this.currentRule.value === 'string') {
      newProperty.value = this.currentRule.value;
      this.propertiesUtils.initValueObjectRef(newProperty);
    } else if (ToscaFunctionHelper.isValueToscaFunction(this.currentRule.value)) {
      newProperty.toscaFunction = ToscaFunctionHelper.convertObjectToToscaFunction(this.currentRule.value);
      newProperty.value = newProperty.toscaFunction.buildValueString();
    } else {
      newProperty.value = JSON.stringify(this.currentRule.value);
      this.propertiesUtils.initValueObjectRef(newProperty);
    }

    this.selectedProperty = newProperty;
  }

  updateSelectedProperty(): void {
    this.selectedProperty = undefined;
    if (!this.currentRule.servicePropertyName) {
      return;
    }

    let newProperty;
    if (this.filterType === FilterType.CAPABILITY) {
      const currentProperty = this.capabilityNameAndPropertiesMap.get(this.currentRule.capabilityName)
        .find(property => property.name === this.currentRule.servicePropertyName);
      newProperty = new PropertyFEModel(currentProperty);
    } else {
      newProperty = new PropertyFEModel(this.selectedInstanceProperties.find(property => property.name === this.currentRule.servicePropertyName));
    }
    newProperty.value = undefined;
    newProperty.toscaFunction = undefined;

    this.propertiesUtils.initValueObjectRef(newProperty);
    this.selectedProperty = newProperty;
  }

  isStaticSource(): boolean {
    return this.selectedSourceType === SourceType.STATIC
  }

  isToscaFunctionSource(): boolean {
    return this.selectedSourceType === SourceType.TOSCA_FUNCTION
  }

  isComplexListMapType(): boolean {
    return this.selectedProperty && this.selectedProperty.derivedDataType > 0;
  }

  updateComplexListMapTypeRuleValue(): void {
    this.currentRule.value = PropertyFEModel.cleanValueObj(this.selectedProperty.valueObj);
    this.onValueChange(this.selectedProperty.valueObjIsValid);
  }

  onToscaFunctionValidityChange(validationEvent: ToscaFunctionValidationEvent): void {
    if (validationEvent.isValid && validationEvent.toscaFunction) {
      this.currentRule.value = validationEvent.toscaFunction;
      this.currentRule.sourceType = validationEvent.toscaFunction.type
      if (validationEvent.toscaFunction instanceof ToscaGetFunction) {
        this.currentRule.sourceName = validationEvent.toscaFunction.sourceName;
      }
    } else {
      this.currentRule.updateValidity(false);
      this.currentRule.value = undefined;
      this.currentRule.sourceType = undefined;
      this.currentRule.sourceName = undefined;
    }
  }

  onSourceTypeChange(): void {
    this.currentRule.value = undefined;
    this.currentRule.sourceType = this.selectedSourceType;
    if (this.isStaticSource()) {
      this.currentRule.sourceName = SourceType.STATIC;
    }
    this.updateSelectedProperty();
  }

  private resetSelectedProperty(): void {
    this.currentRule.servicePropertyName = undefined;
    this.selectedProperty = undefined;
    this.onPropertyChange();
  }

}

export enum FilterType {
  CAPABILITY,
  PROPERTY
}