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
import { Component } from '@angular/core';
import {ServiceServiceNg2} from "app/ng2/services/component-services/service.service";
import {ConstraintObjectUI, OPERATOR_TYPES} from 'app/ng2/components/logic/service-dependencies/service-dependencies.component';
import {ServiceInstanceObject, PropertyBEModel} from 'app/models';
import { PROPERTY_DATA } from 'app/utils';
import {DropdownValue} from 'app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component';

export class UIDropDownSourceTypesElement extends DropdownValue{
    options: Array<any>;
    assignedLabel: string;
    type: string;
    constructor(input?: any){
        if(input) {
            let value = input.value || '';
            let label = input.label || '';
            super(value, label);
            this.options = input.options;
            this.assignedLabel = input.assignedLabel;
            this.type = input.type;
        }
    }
}

@Component({
    selector: 'service-dependencies-editor',
    templateUrl: './service-dependencies-editor.component.html',
    styleUrls:['./service-dependencies-editor.component.less'],
    providers: [ServiceServiceNg2]
})

export class ServiceDependenciesEditorComponent {

    input: {
        serviceRuleIndex: number,
        serviceRules: Array<ConstraintObjectUI>,
        compositeServiceName: string,
        currentServiceName: string,
        compositeServiceProperties: Array<PropertyBEModel>,
        selectedInstanceProperties: Array<PropertyBEModel>,
        operatorTypes: Array<DropdownValue>,
        selectedInstanceSiblings: Array<ServiceInstanceObject>
    };
    currentServiceName: string;
    selectedServiceProperties: Array<PropertyBEModel>;
    selectedPropertyObj: PropertyBEModel;
    ddValueSelectedServicePropertiesNames: Array<DropdownValue>;
    operatorTypes: Array<DropdownValue>;
    sourceTypes: Array<UIDropDownSourceTypesElement> = [];
    currentRule: ConstraintObjectUI;
    currentIndex: number;
    listOfValuesToAssign: Array<DropdownValue>;
    listOfSourceOptions: Array<PropertyBEModel>;
    assignedValueLabel: string;
    serviceRulesList: Array<ConstraintObjectUI>;


    SOURCE_TYPES = {
        STATIC: {label: 'Static', value: 'static'},
        SERVICE_PROPERTY: {label: 'Service Property', value: 'property'}
    };


    ngOnInit() {
        this.currentIndex = this.input.serviceRuleIndex;
        this.serviceRulesList = this.input.serviceRules;
        this.currentRule = this.serviceRulesList && this.input.serviceRuleIndex >= 0 ?
            this.serviceRulesList[this.input.serviceRuleIndex]:
            new ConstraintObjectUI({sourceName: this.SOURCE_TYPES.STATIC.value, sourceType: this.SOURCE_TYPES.STATIC.value, value: "", constraintOperator: OPERATOR_TYPES.EQUAL});
        this.currentServiceName = this.input.currentServiceName;
        this.operatorTypes = this.input.operatorTypes;
        this.selectedServiceProperties = this.input.selectedInstanceProperties;
        this.ddValueSelectedServicePropertiesNames = _.map(this.input.selectedInstanceProperties, prop => new DropdownValue(prop.name, prop.name));
        this.initSourceTypes();
        this.syncRuleData();
        this.updateSourceTypesRelatedValues();
    }

    initSourceTypes() {
        this.sourceTypes.push({label: this.SOURCE_TYPES.STATIC.label, value: this.SOURCE_TYPES.STATIC.value,
            options: [], assignedLabel: this.SOURCE_TYPES.STATIC.label, type: this.SOURCE_TYPES.STATIC.value});
        this.sourceTypes.push({
            label: this.input.compositeServiceName,
            value: this.input.compositeServiceName,
            assignedLabel: this.SOURCE_TYPES.SERVICE_PROPERTY.label,
            type: this.SOURCE_TYPES.SERVICE_PROPERTY.value,
            options: this.input.compositeServiceProperties
        });
        _.forEach(this.input.selectedInstanceSiblings, sib =>
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
        if(!this.currentRule.sourceName && this.currentRule.sourceType === this.SOURCE_TYPES.STATIC.value) {
            this.currentRule.sourceName = this.SOURCE_TYPES.STATIC.value;
        }
        this.selectedPropertyObj = _.find(this.selectedServiceProperties, prop => prop.name === this.currentRule.servicePropertyName);
        this.updateOperatorTypesList();
        this.updateSourceTypesRelatedValues();
    }

    updateOperatorTypesList() {
        if (this.selectedPropertyObj && PROPERTY_DATA.SIMPLE_TYPES_COMPARABLE.indexOf(this.selectedPropertyObj.type) === -1) {
            this.operatorTypes = [{label: "=", value: OPERATOR_TYPES.EQUAL}];
            this.currentRule.constraintOperator = OPERATOR_TYPES.EQUAL;
        }
        else {
            this.operatorTypes = this.input.operatorTypes;
        }
    }

    updateSourceTypesRelatedValues() {
        if(this.currentRule.sourceName) {
            let selectedSourceType: UIDropDownSourceTypesElement = this.sourceTypes.find(
                t => t.value === this.currentRule.sourceName && t.type === this.currentRule.sourceType
            );
            this.listOfSourceOptions = selectedSourceType.options || [];
            this.assignedValueLabel = selectedSourceType.assignedLabel || this.SOURCE_TYPES.STATIC.label;
            this.filterOptionsByType();
        }
    }

    onChangePage(newIndex) {
        if (newIndex >= 0 && newIndex < this.input.serviceRules.length) {
            this.currentIndex = newIndex;
            this.currentRule = this.serviceRulesList[newIndex];
            this.syncRuleData();
        }
    }

    onServicePropertyChanged() {
        this.selectedPropertyObj = _.find(this.selectedServiceProperties, prop => prop.name === this.currentRule.servicePropertyName);
        this.updateOperatorTypesList();
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
        if(!this.selectedPropertyObj) {
            this.listOfValuesToAssign = [];
            return;
        }
        this.listOfValuesToAssign =  this.listOfSourceOptions.reduce((result, op:PropertyBEModel) => {
            if (op.type === this.selectedPropertyObj.type && (!op.schemaType || op.schemaType === this.selectedPropertyObj.schemaType)) {
                result.push(new DropdownValue(op.name, op.name));
            }
            return result;
        }, []);
    }

    onValueChange(isValidValue) {
        this.currentRule.updateValidity(isValidValue);
    }

    checkFormValidForSubmit() {
        if(!this.serviceRulesList) { //for create modal
            let isStatic = this.currentRule.sourceName === this.SOURCE_TYPES.STATIC.value;
            return this.currentRule.isValidRule(isStatic);
        }
        //for update all rules
        return this.serviceRulesList.every(rule => rule.isValidRule(rule.sourceName === this.SOURCE_TYPES.STATIC.value));
    }
}