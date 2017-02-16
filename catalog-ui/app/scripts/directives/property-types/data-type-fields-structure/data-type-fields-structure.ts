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
/// <reference path="../../../references"/>
module Sdc.Directives {
    'use strict';

    export interface IDataTypeFieldsStructureScope extends ng.IScope {
        parentFormObj:ng.IFormController;
        dataTypeProperties:Array<Models.DataTypePropertyModel>;
        typeName:string;
        valueObjRef:any;
        propertyNameValidationPattern: RegExp;
        fieldsPrefixName:string;
        readOnly:boolean;
        currentTypeDefaultValue:any;
        types:Models.DataTypesMap;
        expandByDefault:boolean;
        expand:boolean;
        expanded:boolean;
        dataTypesService:Sdc.Services.DataTypesService;

        expandAndCollapse():void;
        getValidationPattern(type:string):RegExp;
        validateIntRange(value:string):boolean;
        onValueChange(propertyName:string, type:string):void
    }


    export class DataTypeFieldsStructureDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService,
                    private DataTypesService:Sdc.Services.DataTypesService,
                    private PropertyNameValidationPattern: RegExp,
                    private ValidationUtils:Sdc.Utils.ValidationUtils) {
        }

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
        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/property-types/data-type-fields-structure/data-type-fields-structure.html');
        };
        public types=Utils.Constants.PROPERTY_DATA.TYPES;

        //get data type properties array and return object with the properties and their default value
        //(for example: get: [{name:"prop1",defaultValue:1 ...},{name:"prop2", defaultValue:"bla bla" ...}]
        //              return: {prop1: 1, prop2: "bla bla"}
        private getDefaultValue = (dataTypeProperties:Array<Models.DataTypePropertyModel>):any => {
            let defaultValue = {};
            for(let i=0; i < dataTypeProperties.length; i++){
                if(dataTypeProperties[i].type!='string'){
                    if(dataTypeProperties[i].defaultValue){
                        defaultValue[dataTypeProperties[i].name] = JSON.parse(dataTypeProperties[i].defaultValue);
                    }
                }else{
                    defaultValue[dataTypeProperties[i].name] = dataTypeProperties[i].defaultValue;
                }
            }
            return defaultValue;
        };

        private initDataOnScope = (scope:any, $attr:any):void =>{
            scope.dataTypesService = this.DataTypesService;
            scope.dataTypeProperties = this.DataTypesService.getFirsLevelOfDataTypeProperties(scope.typeName,scope.types);
            if($attr.defaultValue){
                scope.currentTypeDefaultValue = JSON.parse($attr.defaultValue);
            }else{
                scope.currentTypeDefaultValue = this.getDefaultValue(scope.dataTypeProperties);
            }

            if(!scope.valueObjRef) {
                scope.valueObjRef = {};
            }

            _.forEach(scope.currentTypeDefaultValue, (value, key)=> {
                if(!scope.valueObjRef[key]){
                    if(typeof scope.currentTypeDefaultValue[key] == 'object'){
                        angular.copy(scope.currentTypeDefaultValue[key], scope.valueObjRef[key]);
                    }else{
                        scope.valueObjRef[key] = scope.currentTypeDefaultValue[key];
                    }
                }
            });
        };

        private rerender = (scope:any):void =>{
            scope.expanded = false;
            scope.expand = false;
            if(scope.expandByDefault){
                scope.expandAndCollapse();
            }
        };

        link = (scope:IDataTypeFieldsStructureScope, element:any, $attr:any) => {
            scope.propertyNameValidationPattern = this.PropertyNameValidationPattern;

            scope.$watchCollection('[typeName,fieldsPrefixName]', (newData:any):void => {
                this.rerender(scope);
            });


            scope.expandAndCollapse = ():void => {
                if(!scope.expanded){
                    this.initDataOnScope(scope,$attr);
                    scope.expanded=true;
                }
                scope.expand=!scope.expand;
            };

            scope.getValidationPattern = (type:string):RegExp => {
                return this.ValidationUtils.getValidationPattern(type);
            };

            scope.validateIntRange = (value:string):boolean => {
                return !value || this.ValidationUtils.validateIntRange(value);
            };

            scope.onValueChange = (propertyName:string, type:string):void => {
                scope.valueObjRef[propertyName] = !angular.isUndefined(scope.valueObjRef[propertyName]) ? scope.valueObjRef[propertyName] : scope.currentTypeDefaultValue[propertyName];
                if(scope.valueObjRef[propertyName] && type != 'string'){
                    scope.valueObjRef[propertyName] = JSON.parse(scope.valueObjRef[propertyName]);
                }
            };
        };

        public static factory = ($templateCache:ng.ITemplateCacheService,
                                 DataTypesService:Sdc.Services.DataTypesService,
                                 PropertyNameValidationPattern:RegExp,
                                 ValidationUtils:Sdc.Utils.ValidationUtils)=> {
            return new DataTypeFieldsStructureDirective($templateCache,DataTypesService,PropertyNameValidationPattern,ValidationUtils);
        };
    }

    DataTypeFieldsStructureDirective.factory.$inject = ['$templateCache','Sdc.Services.DataTypesService','PropertyNameValidationPattern','ValidationUtils'];
}
