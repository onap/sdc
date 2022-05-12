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
import {SchemaProperty} from "app/models";
import {ValidationUtils, PROPERTY_TYPES} from "app/utils";
import {DataTypesService} from "app/services";

export interface ITypeListScope extends ng.IScope {
    parentFormObj:ng.IFormController;
    schemaProperty:SchemaProperty;
    isSchemaTypeDataType:boolean;
    valueObjRef:any;
    propertyNameValidationPattern:RegExp;
    fieldsPrefixName:string;
    readOnly:boolean;
    listDefaultValue:any;
    listNewItem:any;
    maxLength:number;
    stringSchema: SchemaProperty;

    constraints:string[];

    getValidationPattern(type:string):RegExp;
    validateIntRange(value:string):boolean;
    addListItem():void;
    deleteListItem(listItemIndex:number):void;
    getStringSchemaProperty():SchemaProperty;
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
        parentFormObj: '=',//ref to parent form (get angular form object)
        fieldsPrefixName: '=',//prefix for form fields names
        readOnly: '=',//is form read only
        defaultValue: '@',//this list default value
        maxLength: '=',
        constraints: '='
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./type-list-directive.html');
    };

    link = (scope:ITypeListScope, element:any, $attr:any) => {
        scope.propertyNameValidationPattern = this.PropertyNameValidationPattern;
        scope.stringSchema = this.stringSchema;

        //reset valueObjRef when schema type is changed
        scope.$watchCollection('schemaProperty.type', (newData:any):void => {
            scope.isSchemaTypeDataType = this.DataTypesService.isDataTypeForSchemaType(scope.schemaProperty);
            //insert 1 empty item dt by default
            if (scope.isSchemaTypeDataType && (!scope.valueObjRef || !scope.valueObjRef.length)) {
                scope.valueObjRef = scope.valueObjRef || [];
                scope.valueObjRef.push({});
            }
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
                newVal = JSON.parse(scope.listNewItem.value);
            }
            scope.valueObjRef.push(newVal);
            scope.listNewItem.value = "";
        };

        scope.deleteListItem = (listItemIndex: number): void => {
            scope.valueObjRef.splice(listItemIndex, 1);
            if (!scope.valueObjRef.length && scope.listDefaultValue) {
                angular.copy(scope.listDefaultValue, scope.valueObjRef);
            }
        };
    };

    public static factory = (DataTypesService:DataTypesService,
                             PropertyNameValidationPattern:RegExp,
                             ValidationUtils:ValidationUtils)=> {
        return new TypeListDirective(DataTypesService, PropertyNameValidationPattern, ValidationUtils);
    };
}

TypeListDirective.factory.$inject = ['Sdc.Services.DataTypesService', 'PropertyNameValidationPattern', 'ValidationUtils'];

