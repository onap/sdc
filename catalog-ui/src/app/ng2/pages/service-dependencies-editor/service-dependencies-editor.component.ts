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
import {PROPERTY_DATA, PROPERTY_TYPES} from 'app/utils';
import {PropertiesUtils} from '../properties-assignment/services/properties.utils';
import {ToscaFunctionValidationEvent} from "../properties-assignment/tosca-function/tosca-function.component";
import {InstanceFeDetails} from "../../../models/instance-fe-details";
import {CompositionService} from "../composition/composition.service";
import {ToscaGetFunction} from "../../../models/tosca-get-function";
import {PropertyFilterConstraintUi} from "../../../models/ui-models/property-filter-constraint-ui";
import {ConstraintOperatorType, FilterConstraintHelper} from "../../../utils/filter-constraint-helper";
import {ToscaFunctionHelper} from "../../../utils/tosca-function-helper";
import {TopologyTemplateService} from "app/ng2/services/component-services/topology-template.service";
import {CustomToscaFunction} from "../../../models/default-custom-functions";
import {ToscaFunction} from "../../../models/tosca-function";

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
      ConstraintOperatorType.LESS_OR_EQUAL,
      ConstraintOperatorType.IN_RANGE,
      ConstraintOperatorType.VALID_VALUES,
      ConstraintOperatorType.LENGTH,
      ConstraintOperatorType.MIN_LENGTH,
      ConstraintOperatorType.MAX_LENGTH,
      ConstraintOperatorType.PATTERN
  ];
    @Input() comparableAllowedOperators: ConstraintOperatorType[] = [
        ConstraintOperatorType.GREATER_THAN,
        ConstraintOperatorType.LESS_THAN,
        ConstraintOperatorType.EQUAL,
        ConstraintOperatorType.GREATER_OR_EQUAL,
        ConstraintOperatorType.LESS_OR_EQUAL,
        ConstraintOperatorType.IN_RANGE,
        ConstraintOperatorType.VALID_VALUES,
    ];
  @Input() capabilityNameAndPropertiesMap: Map<string, PropertyModel[]>;
  @Input() filterType: FilterType;
  @Input() filterConstraint: PropertyFilterConstraintUi;
  //output
  currentRule: PropertyFilterConstraintUi;

  FILTER_TYPE_CAPABILITY: FilterType = FilterType.CAPABILITY

  listAllowedOperators: ConstraintOperatorType[] = [
        ConstraintOperatorType.EQUAL,
        ConstraintOperatorType.LENGTH,
        ConstraintOperatorType.MIN_LENGTH,
        ConstraintOperatorType.MAX_LENGTH
    ];

  operatorTypes: DropdownValue[] = [
    {label: FilterConstraintHelper.convertToSymbol(ConstraintOperatorType.GREATER_THAN), value: ConstraintOperatorType.GREATER_THAN},
    {label: FilterConstraintHelper.convertToSymbol(ConstraintOperatorType.LESS_THAN), value: ConstraintOperatorType.LESS_THAN},
    {label: FilterConstraintHelper.convertToSymbol(ConstraintOperatorType.EQUAL), value: ConstraintOperatorType.EQUAL},
    {label: FilterConstraintHelper.convertToSymbol(ConstraintOperatorType.GREATER_OR_EQUAL), value: ConstraintOperatorType.GREATER_OR_EQUAL},
    {label: FilterConstraintHelper.convertToSymbol(ConstraintOperatorType.LESS_OR_EQUAL), value: ConstraintOperatorType.LESS_OR_EQUAL}
  ];
  lengthArray: string[] = [ConstraintOperatorType.LENGTH,
      ConstraintOperatorType.MIN_LENGTH,
      ConstraintOperatorType.MAX_LENGTH];

  servicePropertyDropdownList: DropdownValue[];
  isLoading: false;
  selectedProperty: PropertyFEModel;
  selectedSourceType: string;
  componentInstanceMap: Map<string, InstanceFeDetails> = new Map<string, InstanceFeDetails>();
  customToscaFunctions: Array<CustomToscaFunction>;
  capabilityDropdownList: DropdownValue[] = [];
  validValuesToscaFunctionList: ToscaFunction[];
  rangeToscaFunctionList: ToscaFunction[];
  overridingType = PROPERTY_TYPES.INTEGER;

  SOURCE_TYPES = {
    STATIC: {label: 'Static', value: SourceType.STATIC},
    TOSCA_FUNCTION: {label: 'Tosca Function', value: SourceType.TOSCA_FUNCTION},
    TOSCA_FUNCTION_LIST: {label: 'Tosca Function List', value: SourceType.TOSCA_FUNCTION_LIST}
  };

  constructor(private propertiesUtils: PropertiesUtils, private compositionService: CompositionService, private topologyTemplateService: TopologyTemplateService) {}

  ngOnInit(): void {
    if (this.compositionService.componentInstances) {
      this.compositionService.componentInstances.forEach(value => {
        this.componentInstanceMap.set(value.uniqueId, <InstanceFeDetails>{
          name: value.name
        });
      });
    }
    this.initCustomToscaFunctions();
    this.initCapabilityDropdown();
    this.initCurrentRule();
    this.initConstraintOperatorOptions();
    this.initSelectedSourceType();
    this.initPropertyDropdown();
    this.syncRuleData();
    this.generateRangeToscaFunctionList();
  }

  private initCustomToscaFunctions() {
    this.customToscaFunctions = [];
    this.topologyTemplateService.getDefaultCustomFunction().toPromise().then((data) => {
        for (let customFunction of data) {
            this.customToscaFunctions.push(new CustomToscaFunction(customFunction));
        }
    });
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
    this.servicePropertyDropdownList = [new DropdownValue(undefined, selectLabel), ...propertyList.map(prop => new DropdownValue(prop.name, prop.name)).sort((prop1, prop2) => prop1.value.localeCompare(prop2.value))];
  }

    private initConstraintOperatorOptions(): void {
        if (!this.selectedProperty) {
            this.operatorTypes = [this.setOperatorDropdownValue(undefined)];
            return;
        }
        const operatorList: DropdownValue[] = [];
        switch (true) {
            case this.selectedProperty.type === PROPERTY_TYPES.RANGE:
                if (this.currentRule.constraintOperator !== ConstraintOperatorType.IN_RANGE) {
                    this.currentRule.constraintOperator = ConstraintOperatorType.IN_RANGE;
                }
                this.operatorTypes = [this.setOperatorDropdownValue(ConstraintOperatorType.IN_RANGE)];
                break;
            case this.selectedProperty.type === PROPERTY_TYPES.STRING:
                this.allowedOperators.forEach(constraintOperatorType =>
                    operatorList.push(this.setOperatorDropdownValue(constraintOperatorType))
                );
                this.operatorTypes = operatorList;
                break;
            case  this.selectedProperty.type != PROPERTY_TYPES.STRING &&
            ((PROPERTY_DATA.SIMPLE_TYPES_COMPARABLE.indexOf(this.selectedProperty.type) > -1) ||
            (PROPERTY_DATA.COMPARABLE_TYPES.indexOf(this.selectedProperty.type) > -1)):
                this.comparableAllowedOperators.forEach(constraintOperatorType =>
                    operatorList.push(this.setOperatorDropdownValue(constraintOperatorType))
                );
                this.operatorTypes = operatorList;
                break;
            case this.selectedProperty.type === PROPERTY_TYPES.LIST:
                this.listAllowedOperators.forEach(constraintOperatorType =>
                    operatorList.push(this.setOperatorDropdownValue(constraintOperatorType))
                );
                this.operatorTypes = operatorList;
                break;
            default:
                if (this.currentRule.constraintOperator !== ConstraintOperatorType.EQUAL) {
                    this.currentRule.constraintOperator = ConstraintOperatorType.EQUAL;
                }
                this.operatorTypes = [this.setOperatorDropdownValue(ConstraintOperatorType.EQUAL)];
                break;
        }
    }

    private setOperatorDropdownValue(constraintOperatorType: ConstraintOperatorType) {
        if (constraintOperatorType === undefined) {
            return new DropdownValue(undefined, 'Select a Property');
        }
        return new DropdownValue(constraintOperatorType, FilterConstraintHelper.convertToSymbol(constraintOperatorType));
    }

    private initSelectedSourceType(): void {
    if (!this.currentRule.sourceType || this.currentRule.sourceType === SourceType.STATIC) {
      this.selectedSourceType = SourceType.STATIC;
    } else {
        if (!this.isValidValuesOperator() && !this.isRangeType() && !this.isInRangeOperator()){
          this.selectedSourceType = SourceType.TOSCA_FUNCTION;
        }
        else {
          this.selectedSourceType = SourceType.TOSCA_FUNCTION_LIST;
        }
    }
  }

  private initCurrentRule(): void {
      let propertyList: PropertyBEModel[] = [];
      if (this.filterType == FilterType.CAPABILITY) {
          if (this.currentRule.capabilityName) {
              propertyList = this.capabilityNameAndPropertiesMap.get(this.currentRule.capabilityName);
          }
      } else {
          propertyList = this.selectedInstanceProperties;
      }
    if (this.filterConstraint) {
        this.filterConstraint.originalType = propertyList.find(prop=>prop.name==this.filterConstraint.servicePropertyName).type;
      this.currentRule = new PropertyFilterConstraintUi(this.filterConstraint);
    } else {
      this.currentRule = new PropertyFilterConstraintUi({
        sourceName: SourceType.STATIC,
        sourceType: SourceType.STATIC,
        constraintOperator: ConstraintOperatorType.EQUAL,
        value: undefined,
        originalType: undefined
      });
    }
  }

  onCapabilityChange(): void {
    this.initPropertyDropdown();
    this.resetSelectedProperty();
  }

  onPropertyChange(): void {
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
    } else if (Array.isArray(this.currentRule.value) &&
        typeof this.currentRule.value[0] === "object" &&
        this.currentRule.value[0]['propertySource'] != undefined) {
            this.validValuesToscaFunctionList = this.currentRule.value;
            this.rangeToscaFunctionList = this.currentRule.value;
            newProperty.toscaFunction = this.currentRule.value;
    } else {
      newProperty.value = JSON.stringify(this.currentRule.value);
      this.propertiesUtils.initValueObjectRef(newProperty);
    }

    this.selectedProperty = newProperty;
      this.currentRule.originalType = this.selectedProperty.type;
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
    this.currentRule.originalType = this.selectedProperty.type;
  }

  isStaticSource(): boolean {
    return this.selectedSourceType === SourceType.STATIC
  }

  isToscaFunctionSource(): boolean {
    return this.selectedSourceType === SourceType.TOSCA_FUNCTION
  }

  isToscaFunctionListSource(): boolean {
    return this.selectedSourceType === SourceType.TOSCA_FUNCTION_LIST
  }

  isComplexListMapType(): boolean {
    return this.selectedProperty && this.selectedProperty.derivedDataType > 0;
  }

  isRangeType(): boolean {
    return this.selectedProperty && this.selectedProperty.derivedDataType == 4;
  }

  isLengthOperator(): boolean {
      return this.lengthArray.indexOf(this.currentRule.constraintOperator) > -1;
  }

  isInRangeOperator(): boolean {
    return this.currentRule.constraintOperator && this.currentRule.constraintOperator === ConstraintOperatorType.IN_RANGE;
  }

  isValidValuesOperator(): boolean {
    return this.currentRule.constraintOperator && this.currentRule.constraintOperator === ConstraintOperatorType.VALID_VALUES;
  }

  updateComplexListMapTypeRuleValue(): void {
    this.currentRule.value = PropertyFEModel.cleanValueObj(this.selectedProperty.valueObj);
    this.onValueChange(this.selectedProperty.valueObjIsValid);
  }

  onToscaFunctionValidityChange(validationEvent: ToscaFunctionValidationEvent): void {
    if (validationEvent.isValid && validationEvent.toscaFunction) {
        if (this.isValidValuesOperator()) {
            this.currentRule.value = this.validValuesToscaFunctionList;
            this.currentRule.sourceType = SourceType.TOSCA_FUNCTION_LIST;
            if (validationEvent.toscaFunction instanceof ToscaGetFunction) {
                this.currentRule.sourceName = SourceType.TOSCA_FUNCTION_LIST;
            }
        }
        else {
            if (this.isLengthOperator()) {
                this.overridingType = PROPERTY_TYPES.INTEGER;
            }
            this.currentRule.value = validationEvent.toscaFunction;
            this.currentRule.sourceType = validationEvent.toscaFunction.type
            if (validationEvent.toscaFunction instanceof ToscaGetFunction) {
                this.currentRule.sourceName = validationEvent.toscaFunction.sourceName;
            }
        }
    } else {
      this.currentRule.updateValidity(false);
      this.currentRule.value = undefined;
      this.currentRule.sourceType = undefined;
      this.currentRule.sourceName = undefined;
    }
  }

    onToscaFunctionListValidityChange(validationEvent: ToscaFunctionValidationEvent, valueIndex: number): void {
        if (validationEvent.isValid && validationEvent.toscaFunction) {
            this.validValuesToscaFunctionList.splice(this.validValuesToscaFunctionList.length -1, 1, validationEvent.toscaFunction);
            this.currentRule.value = this.validValuesToscaFunctionList;
            this.currentRule.sourceType = 'SEVERAL';
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

    onToscaRangeFunctionListValidityChange(validationEvent: ToscaFunctionValidationEvent, valueIndex: number): void {
        if (validationEvent.isValid && validationEvent.toscaFunction) {
            this.rangeToscaFunctionList.splice(valueIndex, 1, validationEvent.toscaFunction);
            this.currentRule.value = this.rangeToscaFunctionList;
            this.currentRule.sourceType = 'SEVERAL';
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
    if (!this.isStaticSource() && (this.isValidValuesOperator() || this.isRangeType() || this.isInRangeOperator())) {
        this.selectedSourceType = SourceType.TOSCA_FUNCTION_LIST;
    }
    this.currentRule.sourceType = this.selectedSourceType;
    if (this.isStaticSource()) {
      this.currentRule.sourceName = SourceType.STATIC;
    }
    if (this.isToscaFunctionListSource()) {
      this.currentRule.sourceName = SourceType.TOSCA_FUNCTION_LIST;
    }
    this.updateSelectedProperty();
  }

  private resetSelectedProperty(): void {
    this.currentRule.servicePropertyName = undefined;
    this.selectedProperty = undefined;
    this.onPropertyChange();
  }

  addToList(){
      if (!this.validValuesToscaFunctionList) {
          this.validValuesToscaFunctionList = new Array();
      }
      this.validValuesToscaFunctionList.push(ToscaFunctionHelper.convertObjectToToscaFunction(undefined));
  }

  generateRangeToscaFunctionList() {
      if (!this.rangeToscaFunctionList) {
          this.rangeToscaFunctionList = new Array();
          this.rangeToscaFunctionList.push(ToscaFunctionHelper.convertObjectToToscaFunction(undefined));
          this.rangeToscaFunctionList.push(ToscaFunctionHelper.convertObjectToToscaFunction(undefined));
      }
  }

  trackByFn(index) {
    return index;
  }

  removeFromList(valueIndex: number){
    this.validValuesToscaFunctionList.splice(valueIndex, 1);
      this.currentRule.updateValidity(!this.doesArrayContainsEmptyValues(this.validValuesToscaFunctionList) && !(this.validValuesToscaFunctionList.length === 0));
      if (this.doesArrayContainsEmptyValues(this.validValuesToscaFunctionList) || (this.validValuesToscaFunctionList.length === 0)) {
          this.currentRule.value = undefined;
          this.currentRule.sourceType = undefined;
          this.currentRule.sourceName = undefined;
      }
  }

  private doesArrayContainsEmptyValues(arr) {
    for(const element of arr) {
      if(element === undefined) return true;
    }
      return false;
  }
}

export enum FilterType {
  CAPABILITY,
  PROPERTY
}