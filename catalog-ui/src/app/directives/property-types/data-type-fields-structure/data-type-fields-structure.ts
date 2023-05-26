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

/**
 * Created by obarda on 1/27/2016.
 */
'use strict';
import { DataTypesMap } from 'app/models';
import { DataTypePropertyModel } from 'app/models/data-type-properties';
import { DataTypesService } from 'app/services';
import { ValidationUtils } from 'app/utils';
import * as _ from 'lodash';

export interface IDataTypeFieldsStructureScope extends ng.IScope {
    parentFormObj: ng.IFormController;
    dataTypeProperties: DataTypePropertyModel[];
    typeName: string;
    valueObjRef: any;
    propertyNameValidationPattern: RegExp;
    fieldsPrefixName: string;
    readOnly: boolean;
    currentTypeDefaultValue: any;
    types: DataTypesMap;
    expandByDefault: boolean;
    expand: boolean;
    expanded: boolean;
    dataTypesService: DataTypesService;
    constraints: string[];

    expandAndCollapse(): void;
    getValidationPattern(type: string): RegExp;
    validateIntRange(value: string): boolean;
    onValueChange(propertyName: string, type: string): void;
    inputOnValueChange(property: any): void;
}

export class DataTypeFieldsStructureDirective implements ng.IDirective {

    constraints: string[];

    scope = {
        valueObjRef: '=',
        typeName: '=',
        parentFormObj: '=',
        fieldsPrefixName: '=',
        readOnly: '=',
        defaultValue: '@',
        types: '=',
        expandByDefault: '='
    };

    restrict = 'E';
    replace = true;

    constructor(private DataTypesService: DataTypesService,
                private PropertyNameValidationPattern: RegExp,
                private ValidationUtils: ValidationUtils) {
    }

    public static factory = (DataTypesService: DataTypesService,
                             PropertyNameValidationPattern: RegExp,
                             ValidationUtils: ValidationUtils) => {
        return new DataTypeFieldsStructureDirective(DataTypesService, PropertyNameValidationPattern, ValidationUtils);
    }
    template = (): string => {
        return require('./data-type-fields-structure.html');
    }

    link = (scope: IDataTypeFieldsStructureScope, element: any, $attr: any) => {
        scope.propertyNameValidationPattern = this.PropertyNameValidationPattern;

        scope.$watchCollection('[typeName,fieldsPrefixName]', (newData: any): void => {
            this.rerender(scope);
        });

        scope.expandAndCollapse = (): void => {
            if (!scope.expanded) {
                this.initDataOnScope(scope, $attr);
                scope.expanded = true;
            }
            scope.expand = !scope.expand;
        };

        scope.getValidationPattern = (type: string): RegExp => {
            return this.ValidationUtils.getValidationPattern(type);
        };

        scope.validateIntRange = (value: string): boolean => {
            return !value || this.ValidationUtils.validateIntRange(value);
        };

        scope.onValueChange = (propertyName: string, type: string, ): void => {
            scope.valueObjRef[propertyName] = !angular.isUndefined(scope.valueObjRef[propertyName]) ? scope.valueObjRef[propertyName] : scope.currentTypeDefaultValue[propertyName];
            if (scope.valueObjRef[propertyName] && type != 'string') {
                scope.valueObjRef[propertyName] = JSON.parse(scope.valueObjRef[propertyName]);
            }
        };

        scope.inputOnValueChange = (property: any) => {
            if (property.constraints) {
                // this.constraints = property.constraints[0].validValues;
            }

            const value = !scope.parentFormObj[scope.fieldsPrefixName + property.name].$error.pattern
                && ('integer' == property.type && scope.parentFormObj[scope.fieldsPrefixName + property.name].$setValidity('pattern', scope.validateIntRange(scope.valueObjRef[property.name]))
                || scope.onValueChange(property.name, (property.simpleType || property.type)));
            return value;
        };
    }
    // public types=Utils.Constants.PROPERTY_DATA.TYPES;

    // get data type properties array and return object with the properties and their default value
    // (for example: get: [{name:"prop1",defaultValue:1 ...},{name:"prop2", defaultValue:"bla bla" ...}]
    //              return: {prop1: 1, prop2: "bla bla"}
    private getDefaultValue = (dataTypeProperties: DataTypePropertyModel[]): any => {
        const defaultValue = {};
        for (const element of dataTypeProperties) {
            if (element.type != 'string') {
                if (!angular.isUndefined(element.defaultValue)) {
                    defaultValue[element.name] = JSON.parse(element.defaultValue);
                }
            } else {
                defaultValue[element.name] = element.defaultValue;
            }
        }
        return defaultValue;
    }

    private initDataOnScope = (scope: any, $attr: any): void => {
        scope.dataTypesService = this.DataTypesService;
        scope.dataTypeProperties = this.getDataTypeProperties(scope.typeName, scope.types);
        if ($attr.defaultValue) {
            scope.currentTypeDefaultValue = JSON.parse($attr.defaultValue);
        } else {
            scope.currentTypeDefaultValue = this.getDefaultValue(scope.dataTypeProperties);
        }

        if (!scope.valueObjRef) {
            scope.valueObjRef = {};
        }

        _.forEach(scope.currentTypeDefaultValue, (value, key) => {
            if (angular.isUndefined(scope.valueObjRef[key])) {
                if (typeof scope.currentTypeDefaultValue[key] == 'object') {
                    angular.copy(scope.currentTypeDefaultValue[key], scope.valueObjRef[key]);
                } else {
                    scope.valueObjRef[key] = scope.currentTypeDefaultValue[key];
                }
            }
        });
    }

    private getDataTypeProperties = (dataTypeName: string, typesInModel: DataTypesMap): DataTypePropertyModel[] => {
        let properties = typesInModel[dataTypeName].properties || [];
        if (typesInModel[dataTypeName].derivedFromName != 'tosca.datatypes.Root') {
            properties = this.getDataTypeProperties(typesInModel[dataTypeName].derivedFromName, typesInModel).concat(properties);
        }
        return properties;
    }

    private rerender = (scope: any): void => {
        scope.expanded = false;
        scope.expand = false;
        if (scope.expandByDefault) {
            scope.expandAndCollapse();
        }
    }
}

DataTypeFieldsStructureDirective.factory.$inject = ['Sdc.Services.DataTypesService', 'PropertyNameValidationPattern', 'ValidationUtils'];