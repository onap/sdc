/**
 * Created by rcohen on 9/15/2016.
 */
'use strict';
import {SchemaProperty} from "app/models";
import {DataTypesService} from "app/services";
import {ValidationUtils, PROPERTY_TYPES} from "app/utils";

export interface ISelectTypeMapScope extends ng.IScope {
    parentFormObj:ng.IFormController;
    schemaProperty:SchemaProperty;
    isSchemaTypeDataType:boolean;
    valueObjRef:any;
    mapKeys:Array<string>;//array of map keys
    MapKeyValidationPattern:RegExp;
    fieldsPrefixName:string;
    readOnly:boolean;
    mapDefaultValue:any;
    maxLength:number;
    dataTypesService:DataTypesService;

    getValidationPattern(type:string):RegExp;
    validateIntRange(value:string):boolean;
    changeKeyOfMap(newKey:string, index:number, fieldName:string):void;
    deleteMapItem(index:number):void;
    addMapItemFields():void;
    parseToCorrectType(objectOfValues:any, locationInObj:string, type:string):void;
    getNumber(num:number):Array<any>;
}


export class SelectTypeMapDirective implements ng.IDirective {

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
        path: '@'
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./select-type-map-directive.html');
    };

    link = (scope:ISelectTypeMapScope, element:any, $attr:any) => {

        scope.dataTypesService = this.DataTypesService;
        scope.MapKeyValidationPattern = this.MapKeyValidationPattern;

        //reset valueObjRef and mapKeys when schema type is changed
        scope.$watchCollection('schemaProperty.type', (newData:any):void => {
            scope.isSchemaTypeDataType = this.DataTypesService.isDataTypeForSchemaType(scope.schemaProperty);
            if (scope.valueObjRef) {
                scope.mapKeys = Object.keys(scope.valueObjRef);
            }
        });

        //when user brows between properties in "edit property form"
        scope.$watchCollection('fieldsPrefixName', (newData:any):void => {
            if (!scope.valueObjRef) {
                scope.valueObjRef = {};
            }
            scope.mapKeys = Object.keys(scope.valueObjRef);

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
            let oldKey = Object.keys(scope.valueObjRef)[index];
            let existsKeyIndex = Object.keys(scope.valueObjRef).indexOf(newKey);
            if (existsKeyIndex > -1 && existsKeyIndex != index) {
                scope.parentFormObj[fieldName].$setValidity('keyExist', false);
            } else {
                scope.parentFormObj[fieldName].$setValidity('keyExist', true);
                if (!scope.parentFormObj[fieldName].$invalid) {
                    angular.copy(JSON.parse(JSON.stringify(scope.valueObjRef).replace('"' + oldKey + '":', '"' + newKey + '":')), scope.valueObjRef);//update key
                }
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
        return new SelectTypeMapDirective(DataTypesService, MapKeyValidationPattern, ValidationUtils, $timeout);
    };
}

SelectTypeMapDirective.factory.$inject = ['Sdc.Services.DataTypesService', 'MapKeyValidationPattern', 'ValidationUtils', '$timeout'];
