/**
 * Created by obarda on 1/27/2016.
 */
'use strict';
import {DataTypesService} from "app/services";
import { ValidationUtils } from "app/utils";
import { DataTypePropertyModel } from "app/models/data-type-properties";
import { DataTypesMap} from "app/models";

export interface IDataTypeFieldsStructureScope extends ng.IScope {
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

    expandAndCollapse():void;
    getValidationPattern(type:string):RegExp;
    validateIntRange(value:string):boolean;
    onValueChange(propertyName:string, type:string):void
    inputOnValueChange(property:any):void;
}


export class DataTypeFieldsStructureDirective implements ng.IDirective {

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
        //     types: '=',
        expandByDefault: '='
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./data-type-fields-structure.html');
    };
    //public types=Utils.Constants.PROPERTY_DATA.TYPES;

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

    private initDataOnScope = (scope:any, $attr:any):void => {
        scope.dataTypesService = this.DataTypesService;
        scope.dataTypeProperties = this.DataTypesService.getFirsLevelOfDataTypeProperties(scope.typeName);
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

    link = (scope:IDataTypeFieldsStructureScope, element:any, $attr:any) => {
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

        scope.onValueChange = (propertyName:string, type:string):void => {
            scope.valueObjRef[propertyName] = !angular.isUndefined(scope.valueObjRef[propertyName]) ? scope.valueObjRef[propertyName] : scope.currentTypeDefaultValue[propertyName];
            if (scope.valueObjRef[propertyName] && type != 'string') {
                scope.valueObjRef[propertyName] = JSON.parse(scope.valueObjRef[propertyName]);
            }
        };

        scope.inputOnValueChange = (property:any) => {

            let value = !scope.parentFormObj[scope.fieldsPrefixName + property.name].$error.pattern
                && ('integer' == property.type && scope.parentFormObj[scope.fieldsPrefixName + property.name].$setValidity('pattern', scope.validateIntRange(scope.valueObjRef[property.name]))
                || scope.onValueChange(property.name, (property.simpleType || property.type)));
            return value;
        }
    };

    public static factory = (DataTypesService:DataTypesService,
                             PropertyNameValidationPattern:RegExp,
                             ValidationUtils:ValidationUtils)=> {
        return new DataTypeFieldsStructureDirective(DataTypesService, PropertyNameValidationPattern, ValidationUtils);
    };
}

DataTypeFieldsStructureDirective.factory.$inject = ['Sdc.Services.DataTypesService', 'PropertyNameValidationPattern', 'ValidationUtils'];
