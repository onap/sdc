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
import {ValidationUtils} from "app/utils";
import { DataTypesService } from "app/services";
import { DataTypePropertyModel } from "app/models/data-type-properties";
import {DataTypesMap, PropertyModel} from "app/models";

export interface ISelectDataTypeFieldsStructureScope extends ng.IScope {
    parentFormObj:ng.IFormController;
    dataTypeProperties:Array<DataTypePropertyModel>;
    typeName:string;
    valueObjRef:any;
    propertyNameValidationPattern:RegExp;
    fieldsPrefixName:string;
    readOnly:boolean;
    currentTypeDefaultValue:any;
    types:DataTypesMap;
    expandByDefault:boolean;
    expand:boolean;
    expanded:boolean;
    dataTypesService:DataTypesService;
    path:string;
    isParentAlreadyInput:boolean;

    expandAndCollapse():void;
    getValidationPattern(type:string):RegExp;
    validateIntRange(value:string):boolean;
    isAlreadyInput(property:PropertyModel):boolean;
    setSelectedType(property:PropertyModel):void;
    onValueChange(propertyName:string, type:string):void;
}


export class SelectDataTypeFieldsStructureDirective implements ng.IDirective {

    constructor(private DataTypesService:DataTypesService,
                private PropertyNameValidationPattern:RegExp,
                private ValidationUtils:ValidationUtils) {
    }

    scope = {
        valueObjRef: '=',
        typeName: '=',
        parentFormObj: '=',
        fieldsPrefixName: '=',
        readOnly: '=',
        defaultValue: '@',
        expandByDefault: '=',
        path: '@',
        isParentAlreadyInput: '='
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./select-data-type-fields-structure.html');
    };
    // public types=Utils.Constants.PROPERTY_DATA.TYPES;

    //get data type properties array and return object with the properties and their default value
    //(for example: get: [{name:"prop1",defaultValue:1 ...},{name:"prop2", defaultValue:"bla bla" ...}]
    //              return: {prop1: 1, prop2: "bla bla"}
    private getDefaultValue = (dataTypeProperties:Array<DataTypePropertyModel>):any => {
        let defaultValue = {};
        for (let i = 0; i < dataTypeProperties.length; i++) {
            if (dataTypeProperties[i].type != 'string') {
                if (!angular.isUndefined(dataTypeProperties[i].defaultValue)) {
                    defaultValue[dataTypeProperties[i].name] = JSON.parse(dataTypeProperties[i].defaultValue);
                }
            } else {
                defaultValue[dataTypeProperties[i].name] = dataTypeProperties[i].defaultValue;
            }
        }
        return defaultValue;
    };

    private initDataOnScope = (scope:ISelectDataTypeFieldsStructureScope, $attr:any):void => {
        scope.dataTypesService = this.DataTypesService;
        scope.dataTypeProperties = angular.copy(this.DataTypesService.getFirsLevelOfDataTypeProperties(scope.typeName));
        if ($attr.defaultValue) {
            scope.currentTypeDefaultValue = JSON.parse($attr.defaultValue);
        } else {
            scope.currentTypeDefaultValue = this.getDefaultValue(scope.dataTypeProperties);
        }

        if (!scope.valueObjRef) {
            scope.valueObjRef = {};
        }

        _.forEach(scope.currentTypeDefaultValue, (value, key)=> {
            if (angular.isUndefined(scope.valueObjRef[key])) {
                if (typeof scope.currentTypeDefaultValue[key] == 'object') {
                    angular.copy(scope.currentTypeDefaultValue[key], scope.valueObjRef[key]);
                } else {
                    scope.valueObjRef[key] = scope.currentTypeDefaultValue[key];
                }
            }
        });
    };

    private rerender = (scope:any):void => {
        scope.expanded = false;
        scope.expand = false;
        if (scope.expandByDefault) {
            scope.expandAndCollapse();
        }
    };

    link = (scope:ISelectDataTypeFieldsStructureScope, element:any, $attr:any) => {
        scope.propertyNameValidationPattern = this.PropertyNameValidationPattern;

        scope.$watchCollection('[typeName,fieldsPrefixName]', (newData:any):void => {
            this.rerender(scope);
        });


        scope.expandAndCollapse = ():void => {
            if (!scope.expanded) {
                this.initDataOnScope(scope, $attr);
                scope.expanded = true;
            }
            scope.expand = !scope.expand;
        };

        scope.getValidationPattern = (type:string):RegExp => {
            return this.ValidationUtils.getValidationPattern(type);
        };

        scope.validateIntRange = (value:string):boolean => {
            return !value || this.ValidationUtils.validateIntRange(value);
        };

        /*
         check if property is alrady declered on the service by meatching the input name & the property name

         */
        scope.isAlreadyInput = (property:PropertyModel):boolean => {
            if (scope.path) {
                if (scope.isParentAlreadyInput) {
                    return true;
                }
                let parentInputName = this.DataTypesService.selectedInstance.normalizedName + '_' + scope.path.replace('#', '_');// set the input parent  as he need to declared as input
                let inputName = parentInputName + '_' + property.name;// set the input name as he need to declared as input
                let selectedProperty = _.find(this.DataTypesService.selectedComponentInputs, (componentInput)=> {
                    if (componentInput.name == parentInputName) { //check if the parent(all the complex) is already declared
                        scope.isParentAlreadyInput = true;
                        return true;
                    } else if (componentInput.name.substring(0, inputName.length) == inputName) { //check if specific property inside the complex
                        return true;
                    }
                    //return componentInput.name == parentInputName || componentInput.name.substring(0,inputName.length) == inputName;//check if the parent(all the complex) is already declared or specific property inside the complex
                });
                if (selectedProperty) {
                    return true;
                }
            }
            return false;
        };

        scope.setSelectedType = (property:PropertyModel):void=> {
            scope.dataTypesService.selectedInput = property;
            scope.dataTypesService.selectedPropertiesName = scope.path + '#' + property.name;
        };

        scope.onValueChange = (propertyName:string, type:string):void => {
            scope.valueObjRef[propertyName] = !angular.isUndefined(scope.valueObjRef[propertyName]) ? scope.valueObjRef[propertyName] : scope.currentTypeDefaultValue[propertyName];
            if (scope.valueObjRef[propertyName] && type != 'string') {
                scope.valueObjRef[propertyName] = JSON.parse(scope.valueObjRef[propertyName]);
            }
        };


    };

    public static factory = (DataTypesService:DataTypesService,
                             PropertyNameValidationPattern:RegExp,
                             ValidationUtils:ValidationUtils)=> {
        return new SelectDataTypeFieldsStructureDirective(DataTypesService, PropertyNameValidationPattern, ValidationUtils);
    };
}

SelectDataTypeFieldsStructureDirective.factory.$inject = ['Sdc.Services.DataTypesService', 'PropertyNameValidationPattern', 'ValidationUtils'];
