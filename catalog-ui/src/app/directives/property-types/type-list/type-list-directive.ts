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
 * Created by rcohen on 9/15/2016.
 */
'use strict';
import {SchemaProperty, PropertyModel, DataTypesMap} from "app/models";
import {ValidationUtils, PROPERTY_TYPES, PROPERTY_DATA} from "app/utils";
import {DataTypesService} from "app/services";
import {InstanceFeDetails} from "app/models/instance-fe-details";
import {ToscaGetFunction} from "app/models/tosca-get-function";
import {SubPropertyToscaFunction} from "app/models/sub-property-tosca-function";

export interface ITypeListScope extends ng.IScope {
    parentFormObj:ng.IFormController;
    schemaProperty:SchemaProperty;
    parentProperty:PropertyModel;
    componentInstanceMap: Map<string, InstanceFeDetails>;
    isSchemaTypeDataType:boolean;
    valueObjRef:any;
    propertyNameValidationPattern:RegExp;
    fieldsPrefixName:string;
    readOnly:boolean;
    listDefaultValue:any;
    listNewItem:any;
    maxLength:number;
    stringSchema: SchemaProperty;
    showToscaFunction: Array<boolean>;
    constraints:string[];
    types:DataTypesMap;
    isService:boolean;

    getValidationPattern(type:string):RegExp;
    validateIntRange(value:string):boolean;
    addListItem():void;
    addValueToList(value:string,index:number);
    deleteListItem(listItemIndex:number):void;
    getStringSchemaProperty():SchemaProperty;
    getNumber(num:number):Array<any>;
    onEnableTosca(toscaFlag:boolean,index:number);
    onGetToscaFunction(toscaGetFunction: ToscaGetFunction, index:number);
}


export class TypeListDirective implements ng.IDirective {

    private readonly stringSchema: SchemaProperty;

    constructor(private DataTypesService:DataTypesService,
                private PropertyNameValidationPattern:RegExp,
                private ValidationUtils:ValidationUtils) {
        this.stringSchema = new SchemaProperty();
        this.stringSchema.type = PROPERTY_TYPES.STRING;
        this.stringSchema.isSimpleType = true;
        this.stringSchema.isDataType = false;
    }

    scope = {
        valueObjRef: '=',//ref to list object in the parent value object
        schemaProperty: '=',//get the schema.property object
        componentInstanceMap: '=',
        parentProperty: '=',
        parentFormObj: '=',//ref to parent form (get angular form object)
        fieldsPrefixName: '=',//prefix for form fields names
        readOnly: '=',//is form read only
        defaultValue: '@',//this list default value
        maxLength: '=',
        constraints: '=',
        types: '=',
        isService: '='
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./type-list-directive.html');
    };
    
    private isDataTypeForSchemaType = (property:SchemaProperty, types:DataTypesMap):boolean=> {
        property.simpleType = "";
        if (property.type && PROPERTY_DATA.TYPES.indexOf(property.type) > -1) {
            return false;
        }
        let simpleType = this.getTypeForDataTypeDerivedFromSimple(property.type, types);
        if (simpleType) {
            property.simpleType = simpleType;
            return false;
        }
        return true;
    };
    
    private getTypeForDataTypeDerivedFromSimple = (dataTypeName:string, types:DataTypesMap):string => {
        if (!types[dataTypeName]) {
            return 'string';
        }
        if (types[dataTypeName].derivedFromName == "tosca.datatypes.Root" || types[dataTypeName].properties) {
            return null;
        }
        if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(types[dataTypeName].derivedFromName) > -1) {
            return types[dataTypeName].derivedFromName
        }
        return this.getTypeForDataTypeDerivedFromSimple(types[dataTypeName].derivedFromName, types);
    };

    link = (scope:ITypeListScope, element:any, $attr:any) => {
        scope.propertyNameValidationPattern = this.PropertyNameValidationPattern;
        scope.stringSchema = this.stringSchema;
        if (scope.valueObjRef.length == 0) {
            scope.valueObjRef.push("");
        }
        scope.showToscaFunction = new Array(scope.valueObjRef.length);
        scope.valueObjRef.forEach((value, index) => {
            scope.showToscaFunction[index] = false;
            let key : string = index.toString();
            if (scope.parentProperty.subPropertyToscaFunctions != null) {
                scope.parentProperty.subPropertyToscaFunctions.forEach(SubPropertyToscaFunction => {
                    if (SubPropertyToscaFunction.subPropertyPath.indexOf(key) != -1) {
                        scope.showToscaFunction[index] = true;
                    }
                });
            }
        });
        //reset valueObjRef when schema type is changed
        scope.$watchCollection('schemaProperty.type', (newData:any):void => {
            scope.isSchemaTypeDataType = this.isDataTypeForSchemaType(scope.schemaProperty, scope.types);
        });

        //when user brows between properties in "edit property form"
        scope.$watchCollection('fieldsPrefixName', (newData:any):void => {
            scope.listNewItem = {value: ''};

            if ($attr.defaultValue) {
                scope.listDefaultValue = JSON.parse($attr.defaultValue);
            }
        });

        scope.getValidationPattern = (type:string):RegExp => {
            return this.ValidationUtils.getValidationPattern(type);
        };

        scope.validateIntRange = (value:string):boolean => {
            return !value || this.ValidationUtils.validateIntRange(value);
        };

        scope.addListItem = ():void => {
            scope.valueObjRef = scope.valueObjRef || [];
            let newVal;
            if (scope.schemaProperty.type === PROPERTY_TYPES.MAP) {
                newVal = {"": ""};
            } else if ((scope.schemaProperty.simpleType || scope.schemaProperty.type) == PROPERTY_TYPES.STRING) {
                newVal = scope.listNewItem.value;
            } else {
                if (scope.listNewItem.value != "") {
                    newVal = JSON.parse(scope.listNewItem.value);
                }
            }
            scope.valueObjRef.push(newVal);
            scope.showToscaFunction.push(false);
            scope.listNewItem.value = "";
        };

        //return dummy array in order to prevent rendering map-keys ng-repeat again when a map key is changed
        scope.getNumber = (num:number):Array<any> => {
            return new Array(num);
        };

        scope.addValueToList = (value:string,index:number):void => {
            console.log("value : "+value+" , index : "+index);
            scope.valueObjRef[index] = value;
            scope.parentProperty.value = scope.valueObjRef;
        }

        scope.deleteListItem = (listItemIndex: number): void => {
            scope.valueObjRef.splice(listItemIndex, 1);
            let key : string = listItemIndex.toString();
            if (scope.parentProperty.subPropertyToscaFunctions != null) {
                let subToscaFunctionList : Array<SubPropertyToscaFunction> = [];
                scope.parentProperty.subPropertyToscaFunctions.forEach((SubPropertyToscaFunction, index) => {
                    if (SubPropertyToscaFunction.subPropertyPath.indexOf(key) == -1) {
                        subToscaFunctionList.push(SubPropertyToscaFunction);
                    }
                });
                scope.parentProperty.subPropertyToscaFunctions = subToscaFunctionList;
            }
            if (!scope.valueObjRef.length && scope.listDefaultValue) {
                angular.copy(scope.listDefaultValue, scope.valueObjRef);
            }
        };

        scope.onEnableTosca = (toscaFlag:boolean,flagIndex:number):void => {
            scope.showToscaFunction[flagIndex] = toscaFlag;
            scope.valueObjRef[flagIndex] = "";
            let key:string = flagIndex.toString();
            if (!toscaFlag) {
                if (scope.parentProperty.subPropertyToscaFunctions != null) {
                    let subToscaFunctionList : Array<SubPropertyToscaFunction> = [];
                    scope.parentProperty.subPropertyToscaFunctions.forEach((SubPropertyToscaFunction, index) => {
                        if (SubPropertyToscaFunction.subPropertyPath.indexOf(key) == -1) {
                            subToscaFunctionList.push(SubPropertyToscaFunction);
                        }
                    });
                    scope.parentProperty.subPropertyToscaFunctions = subToscaFunctionList;
                }
            }
        };

        scope.onGetToscaFunction = (toscaGetFunction: ToscaGetFunction, index:number): void => {
            let key:string = index.toString();
            if (scope.parentProperty.subPropertyToscaFunctions != null) {
                scope.parentProperty.subPropertyToscaFunctions.forEach(SubPropertyToscaFunction => {
                    if (SubPropertyToscaFunction.subPropertyPath.indexOf(key) != -1) {
                        SubPropertyToscaFunction.toscaFunction = toscaGetFunction;
                        return;
                    }
                });

            }
            if (scope.parentProperty.subPropertyToscaFunctions == null){
                scope.parentProperty.subPropertyToscaFunctions = [];
            }
            let subPropertyToscaFunction = new SubPropertyToscaFunction();
            subPropertyToscaFunction.toscaFunction = toscaGetFunction;
            subPropertyToscaFunction.subPropertyPath = [key];
            scope.parentProperty.subPropertyToscaFunctions.push(subPropertyToscaFunction);
        }

    };

    public static factory = (DataTypesService:DataTypesService,
                             PropertyNameValidationPattern:RegExp,
                             ValidationUtils:ValidationUtils)=> {
        return new TypeListDirective(DataTypesService, PropertyNameValidationPattern, ValidationUtils);
    };
}

TypeListDirective.factory.$inject = ['Sdc.Services.DataTypesService', 'PropertyNameValidationPattern', 'ValidationUtils'];

