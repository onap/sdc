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
import {ValidationUtils, PROPERTY_TYPES} from "app/utils";
import {DataTypesService} from "app/services";
import {SchemaProperty} from "app/models";

export interface ITypeMapScope extends ng.IScope {
    parentFormObj:ng.IFormController;
    schemaProperty:SchemaProperty;
    isMapKeysUnique:boolean;
    isSchemaTypeDataType:boolean;
    valueObjRef:any;
    mapKeys:Array<string>;//array of map keys
    mapKeysStatic:Array<string>;
    MapKeyValidationPattern:RegExp;
    fieldsPrefixName:string;
    readOnly:boolean;
    mapDefaultValue:any;
    maxLength:number;
    constraints:string[];
    showAddBtn: boolean;

    getValidationPattern(type:string):RegExp;
    validateIntRange(value:string):boolean;
    changeKeyOfMap(newKey:string, index:number, fieldName:string):void;
    deleteMapItem(index:number):void;
    addMapItemFields():void;
    parseToCorrectType(objectOfValues:any, locationInObj:string, type:string):void;
    getNumber(num:number):Array<any>;
}


export class TypeMapDirective implements ng.IDirective {

    constructor(private DataTypesService:DataTypesService,
                private MapKeyValidationPattern:RegExp,
                private ValidationUtils:ValidationUtils,
                private $timeout:ng.ITimeoutService) {
    }

    scope = {
        valueObjRef: '=',//ref to map object in the parent value object
        schemaProperty: '=',//get the schema.property object
        parentFormObj: '=',//ref to parent form (get angular form object)
        fieldsPrefixName: '=',//prefix for form fields names
        readOnly: '=',//is form read only
        defaultValue: '@',//this map default value
        maxLength: '=',
        constraints: '=',
        showAddBtn: '=?'
    };

    restrict = 'E';
    replace = true;
    template = (): string => {
        return require('./type-map-directive.html');
    };

    link = (scope:ITypeMapScope, element:any, $attr:any) => {
        scope.showAddBtn = angular.isDefined(scope.showAddBtn) ? scope.showAddBtn : true;
        scope.MapKeyValidationPattern = this.MapKeyValidationPattern;
        scope.isMapKeysUnique = true;

        //reset valueObjRef and mapKeys when schema type is changed
        scope.$watchCollection('schemaProperty.type', (newData:any):void => {
            scope.isSchemaTypeDataType = this.DataTypesService.isDataTypeForSchemaType(scope.schemaProperty);
            if (scope.valueObjRef) {
                scope.mapKeys = Object.keys(scope.valueObjRef);
                //keeping another copy of the keys, as the mapKeys gets overridden sometimes
                scope.mapKeysStatic = Object.keys(scope.valueObjRef);
            }
        });

        scope.$watchCollection('valueObjRef', (newData: any): void => {
            scope.mapKeys = Object.keys(scope.valueObjRef);
            scope.mapKeysStatic = Object.keys(scope.valueObjRef);
        });

        //when user brows between properties in "edit property form"
        scope.$watchCollection('fieldsPrefixName', (newData:any):void => {
            if (!scope.valueObjRef) {
                scope.valueObjRef = {};
            }
            scope.mapKeys = Object.keys(scope.valueObjRef);
            //keeping another copy of the keys, as the mapKeys gets overridden sometimes
            scope.mapKeysStatic = Object.keys(scope.valueObjRef);

            if ($attr.defaultValue) {
                scope.mapDefaultValue = JSON.parse($attr.defaultValue);
            }
        });

        //return dummy array in order to prevent rendering map-keys ng-repeat again when a map key is changed
        scope.getNumber = (num:number):Array<any> => {
            return new Array(num);
        };

        scope.getValidationPattern = (type:string):RegExp => {
            return this.ValidationUtils.getValidationPattern(type);
        };

        scope.validateIntRange = (value:string):boolean => {
            return !value || this.ValidationUtils.validateIntRange(value);
        };

        scope.changeKeyOfMap = (newKey:string, index:number, fieldName:string):void => {
            const currentKeySet = Object.keys(scope.valueObjRef);
            const currentKey = currentKeySet[index];
            const existingKeyIndex = currentKeySet.indexOf(newKey);
            if (existingKeyIndex > -1 && existingKeyIndex != index) {
                scope.parentFormObj[fieldName].$setValidity('keyExist', false);
                scope.isMapKeysUnique = false;
                return;
            }

            scope.parentFormObj[fieldName].$setValidity('keyExist', true);
            scope.isMapKeysUnique = true;
            if (!scope.parentFormObj[fieldName].$invalid) {
                //To preserve the order of the keys, delete each one and recreate
                let newObj = {};
                angular.copy(scope.valueObjRef, newObj);
                angular.forEach(newObj, function (value: any, key: string) {
                    delete scope.valueObjRef[key];
                    if (key == currentKey) {
                        scope.valueObjRef[newKey] = value;
                    } else {
                        scope.valueObjRef[key] = value;
                    }
                });
            }
        };

        scope.deleteMapItem = (index:number):void=> {
            delete scope.valueObjRef[scope.mapKeys[index]];
            scope.mapKeys.splice(index, 1);
            if (!scope.mapKeys.length) {//only when user removes all pairs of key-value fields - put the default
                if (scope.mapDefaultValue) {
                    angular.copy(scope.mapDefaultValue, scope.valueObjRef);
                    scope.mapKeys = Object.keys(scope.valueObjRef);
                }
            }
        };

        scope.addMapItemFields = ():void => {
            scope.valueObjRef[''] = null;
            scope.mapKeys = Object.keys(scope.valueObjRef);
        };

        scope.parseToCorrectType = (objectOfValues:any, locationInObj:string, type:string):void => {
            if (objectOfValues[locationInObj] && type != PROPERTY_TYPES.STRING) {
                objectOfValues[locationInObj] = JSON.parse(objectOfValues[locationInObj]);
            }
        }
    };

    public static factory = (DataTypesService:DataTypesService,
                             MapKeyValidationPattern:RegExp,
                             ValidationUtils:ValidationUtils,
                             $timeout:ng.ITimeoutService)=> {
        return new TypeMapDirective(DataTypesService, MapKeyValidationPattern, ValidationUtils, $timeout);
    };
}

TypeMapDirective.factory.$inject = ['Sdc.Services.DataTypesService', 'MapKeyValidationPattern', 'ValidationUtils', '$timeout'];
