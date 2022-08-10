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
import {ConstraintObjectUI, OPERATOR_TYPES, SourceType} from 'app/ng2/components/logic/service-dependencies/service-dependencies.component';
import {DropdownValue} from 'app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component';
import {ServiceServiceNg2} from 'app/ng2/services/component-services/service.service';
import {PROPERTY_DATA} from 'app/utils';
import {ServiceInstanceObject} from '../../../models/service-instance-properties-and-interfaces';
import {PropertiesUtils} from '../properties-assignment/services/properties.utils';
import {ToscaFunctionValidationEvent} from "../properties-assignment/tosca-function/tosca-function.component";
import {InstanceFeDetails} from "../../../models/instance-fe-details";
import {CompositionService} from "../composition/composition.service";
import {ToscaGetFunction} from "../../../models/tosca-get-function";

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
  //output
  currentRule: ConstraintObjectUI;

  currentServiceName: string;
  selectedServiceProperties: PropertyBEModel[] = [];
  ddValueSelectedServicePropertiesNames: DropdownValue[];
  operatorTypes: DropdownValue[];
  currentIndex: number;
  serviceRulesList: ConstraintObjectUI[];
  isLoading: false;
  selectedProperty: PropertyFEModel;
  selectedSourceType: string;
  componentInstanceMap: Map<string, InstanceFeDetails> = new Map<string, InstanceFeDetails>();

  SOURCE_TYPES = {
    STATIC: {label: 'Static', value: SourceType.STATIC},
    TOSCA_FUNCTION: {label: 'Tosca Function', value: SourceType.TOSCA_FUNCTION}
  };

  constructor(private propertiesUtils: PropertiesUtils, private compositionService: CompositionService) {}

  ngOnInit() {
    this.currentIndex = this.input.serviceRuleIndex;
    this.serviceRulesList = this.input.serviceRules;
    if (this.input.selectedInstanceProperties) {
      this.selectedServiceProperties = this.input.selectedInstanceProperties;
    }
    this.currentServiceName = this.input.currentServiceName;
    this.operatorTypes = this.input.operatorTypes;
    if (this.compositionService.componentInstances) {
      this.compositionService.componentInstances.forEach(value => {
        this.componentInstanceMap.set(value.uniqueId, <InstanceFeDetails>{
          name: value.name
        });
      });
    }
    this.initCurrentRule();
    this.initSelectedSourceType();
    this.selectedProperty = new PropertyFEModel(this.selectedServiceProperties.find(property => property.name === this.currentRule.servicePropertyName));
    this.selectedProperty.toscaFunction = undefined;
    this.selectedProperty.value = undefined;
    this.ddValueSelectedServicePropertiesNames = _.map(this.input.selectedInstanceProperties, (prop) => new DropdownValue(prop.name, prop.name));
    this.syncRuleData();
  }

  private initSelectedSourceType() {
    if (!this.currentRule.sourceType || this.currentRule.sourceType === SourceType.STATIC) {
      this.selectedSourceType = SourceType.STATIC;
    } else {
      this.selectedSourceType = SourceType.TOSCA_FUNCTION;
    }
  }

  private initCurrentRule() {
    if (this.serviceRulesList && this.input.serviceRuleIndex >= 0) {
      this.currentRule = new ConstraintObjectUI(this.serviceRulesList[this.input.serviceRuleIndex]);
    } else {
      this.currentRule = new ConstraintObjectUI({
        sourceName: SourceType.STATIC,
        sourceType: SourceType.STATIC,
        value: '',
        constraintOperator: OPERATOR_TYPES.EQUAL
      });
    }
  }

  onServicePropertyChanged() {
    this.currentRule.sourceName = undefined;
    this.updateSelectedProperty();
    this.updateOperatorTypesList();
    this.currentRule.value = "";
  }

  syncRuleData() {
    if (!this.currentRule.sourceName || this.currentRule.sourceType === SourceType.STATIC) {
      this.currentRule.sourceName = SourceType.STATIC;
      this.currentRule.sourceType = SourceType.STATIC;
    }
    this.updateSelectedProperty();
    this.updateOperatorTypesList();
  }

  updateOperatorTypesList() {
    if (this.selectedProperty && PROPERTY_DATA.SIMPLE_TYPES_COMPARABLE.indexOf(this.selectedProperty.type) === -1) {
      this.operatorTypes = [{label: '=', value: OPERATOR_TYPES.EQUAL}];
      this.currentRule.constraintOperator = OPERATOR_TYPES.EQUAL;
    } else {
      this.operatorTypes = this.input.operatorTypes;
    }
  }

  onValueChange(isValidValue:any) {
    this.currentRule.updateValidity(isValidValue);
  }

  checkFormValidForSubmit() {
    if (!this.serviceRulesList) { // for create modal
      return this.currentRule.isValidRule();
    }

    // for update all rules
    return this.serviceRulesList.every((rule) => rule.isValidRule());
  }

  updateSelectedProperty(): void {
    if (!this.currentRule.servicePropertyName) {
      this.selectedProperty = undefined;
      return;
    }

    const newProperty = new PropertyFEModel(this.selectedServiceProperties.find(property => property.name === this.currentRule.servicePropertyName));
    newProperty.toscaFunction = undefined;
    if (typeof this.currentRule.value === 'string') {
      newProperty.value = this.currentRule.value;
    } else {
      newProperty.value = JSON.stringify(this.currentRule.value);
    }
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
    const value = PropertyFEModel.cleanValueObj(this.selectedProperty.valueObj);
    this.currentRule.value = JSON.stringify(value);
    this.onValueChange(this.selectedProperty.valueObjIsValid);
  }

  onToscaFunctionValidityChange(validationEvent: ToscaFunctionValidationEvent) {
    if (validationEvent.isValid && validationEvent.toscaFunction) {
      this.currentRule.value = validationEvent.toscaFunction.buildValueString();
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

  onSourceTypeChange() {
    this.currentRule.value = undefined;
    this.currentRule.sourceType = this.selectedSourceType;
    if (this.isStaticSource()) {
      this.currentRule.sourceName = SourceType.STATIC;
    }
    this.updateSelectedProperty();
  }

}
