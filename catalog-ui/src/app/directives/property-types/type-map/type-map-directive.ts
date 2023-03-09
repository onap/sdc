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
import { DataTypesMap, PropertyModel, SchemaProperty } from 'app/models';
import { InstanceFeDetails } from 'app/models/instance-fe-details';
import { SubPropertyToscaFunction } from 'app/models/sub-property-tosca-function';
import { ToscaGetFunction } from 'app/models/tosca-get-function';
import { DataTypesService } from 'app/services';
import { PROPERTY_DATA, PROPERTY_TYPES, ValidationUtils } from 'app/utils';

export interface ITypeMapScope extends ng.IScope {
    parentFormObj: ng.IFormController;
    schemaProperty: SchemaProperty;
    parentProperty: PropertyModel;
    componentInstanceMap: Map<string, InstanceFeDetails>;
    isMapKeysUnique: boolean;
    isSchemaTypeDataType: boolean;
    valueObjRef: any;
    mapKeys: string[]; // array of map keys
    mapKeysStatic: string[];
    MapKeyValidationPattern: RegExp;
    fieldsPrefixName: string;
    readOnly: boolean;
    mapDefaultValue: any;
    maxLength: number;
    constraints: string[];
    showAddBtn: boolean;
    showToscaFunction: boolean[];
    types: DataTypesMap;
    isService: boolean;

    getValidationPattern(type: string): RegExp;
    validateIntRange(value: string): boolean;
    changeKeyOfMap(newKey: string, index: number, fieldName: string): void;
    deleteMapItem(index: number): void;
    addMapItemFields(): void;
    parseToCorrectType(objectOfValues: any, locationInObj: string, type: string): void;
    getNumber(num: number): any[];
    validateSubToscaFunction(key: string): boolean;
    onEnableTosca(toscaFlag: boolean, index: number);
    onGetToscaFunction(toscaGetFunction: ToscaGetFunction, key: string);
}

export class TypeMapDirective implements ng.IDirective {

    scope = {
        valueObjRef: '=', // ref to map object in the parent value object
        componentInstanceMap: '=',
        schemaProperty: '=', // get the schema.property object
        parentFormObj: '=', // ref to parent form (get angular form object)
        fieldsPrefixName: '=', // prefix for form fields names
        readOnly: '=', // is form read only
        defaultValue: '@', // this map default value
        maxLength: '=',
        constraints: '=',
        showAddBtn: '=?',
        parentProperty: '=',
        types: '=',
        isService: '='
    };

    restrict = 'E';
    replace = true;

    constructor(private DataTypesService: DataTypesService,
                private MapKeyValidationPattern: RegExp,
                private ValidationUtils: ValidationUtils,
                private $timeout: ng.ITimeoutService) {
    }

    public static factory = (DataTypesService: DataTypesService,
                             MapKeyValidationPattern: RegExp,
                             ValidationUtils: ValidationUtils,
                             $timeout: ng.ITimeoutService) => {
        return new TypeMapDirective(DataTypesService, MapKeyValidationPattern, ValidationUtils, $timeout);
    }
    template = (): string => {
        return require('./type-map-directive.html');
    }

    link = (scope: ITypeMapScope, element: any, $attr: any) => {
        scope.showAddBtn = angular.isDefined(scope.showAddBtn) ? scope.showAddBtn : true;
        scope.MapKeyValidationPattern = this.MapKeyValidationPattern;
        scope.isMapKeysUnique = true;

        if (scope.mapKeys === undefined) {
            if (scope.valueObjRef) {
                scope.mapKeys = Object.keys(scope.valueObjRef);
            } else if (scope.defaultValue) {
                const defaultValue = JSON.parse(scope.defaultValue);
                scope.valueObjRef = defaultValue;
                scope.mapKeys = Object.keys(defaultValue);
            } else {
                console.warn('Missing value keys');
            }
        }

        if (scope.mapKeys) {
            scope.showToscaFunction = new Array(scope.mapKeys.length);
            scope.mapKeys.forEach((key, index) => {
                scope.showToscaFunction[index] = false;
                if (scope.parentProperty && scope.parentProperty.subPropertyToscaFunctions != null) {
                    scope.parentProperty.subPropertyToscaFunctions.forEach((SubPropertyToscaFunction) => {
                        if (SubPropertyToscaFunction.subPropertyPath.indexOf(key) != -1) {
                            scope.showToscaFunction[index] = true;
                        }
                    });
                }
            });
        } else {
            console.warn('Missing map keys');
        }

        // reset valueObjRef and mapKeys when schema type is changed
        scope.$watchCollection('schemaProperty.type', (newData: any): void => {
            scope.isSchemaTypeDataType = this.isDataTypeForSchemaType(scope.schemaProperty, scope.types);
            if (scope.valueObjRef) {
                scope.mapKeys = Object.keys(scope.valueObjRef);
                // keeping another copy of the keys, as the mapKeys gets overridden sometimes
                scope.mapKeysStatic = Object.keys(scope.valueObjRef);
            }
        });

        scope.$watchCollection('valueObjRef', (newData: any): void => {
            if (scope.valueObjRef) {
                scope.mapKeys = Object.keys(scope.valueObjRef);
                scope.mapKeysStatic = Object.keys(scope.valueObjRef);
            } else {
                console.warn('valueObjRef missing', scope.valueObjRef);
            }
        });

        // when user brows between properties in "edit property form"
        scope.$watchCollection('fieldsPrefixName', (newData: any): void => {
            if (scope.valueObjRef) {
                scope.mapKeys = Object.keys(scope.valueObjRef);
                // keeping another copy of the keys, as the mapKeys gets overridden sometimes
                scope.mapKeysStatic = Object.keys(scope.valueObjRef);
            }

            if ($attr.defaultValue) {
                scope.mapDefaultValue = JSON.parse($attr.defaultValue);
            }
        });

        // return dummy array in order to prevent rendering map-keys ng-repeat again when a map key is changed
        scope.getNumber = (num: number): any[] => {
            return new Array(num);
        };

        scope.getValidationPattern = (type: string): RegExp => {
            return this.ValidationUtils.getValidationPattern(type);
        };

        scope.validateIntRange = (value: string): boolean => {
            return !value || this.ValidationUtils.validateIntRange(value);
        };

        scope.changeKeyOfMap = (newKey: string, index: number, fieldName: string): void => {
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
                // To preserve the order of the keys, delete each one and recreate
                const newObj = {};
                angular.copy(scope.valueObjRef, newObj);
                angular.forEach(newObj, function(value: any, key: string) {
                    delete scope.valueObjRef[key];
                    if (key == currentKey) {
                        scope.valueObjRef[newKey] = value;
                    } else {
                        scope.valueObjRef[key] = value;
                    }
                });
            }
        };

        scope.deleteMapItem = (index: number): void => {
            const keyToChange = scope.mapKeys[index];
            delete scope.valueObjRef[scope.mapKeys[index]];
            scope.mapKeys.splice(index, 1);
            scope.showToscaFunction.splice(index, 1);
            if (scope.parentProperty && scope.parentProperty.subPropertyToscaFunctions != null) {
                const subToscaFunctionList: SubPropertyToscaFunction[] = [];
                scope.parentProperty.subPropertyToscaFunctions.forEach((SubPropertyToscaFunction, index) => {
                    if (SubPropertyToscaFunction.subPropertyPath.indexOf(keyToChange) == -1) {
                        subToscaFunctionList.push(SubPropertyToscaFunction);
                    }
                });
                scope.parentProperty.subPropertyToscaFunctions = subToscaFunctionList;
            }
            if (!scope.mapKeys.length) {// only when user removes all pairs of key-value fields - put the default
                if (scope.mapDefaultValue) {
                    angular.copy(scope.mapDefaultValue, scope.valueObjRef);
                    scope.mapKeys = Object.keys(scope.valueObjRef);
                }
            }
        };

        scope.onEnableTosca = (toscaFlag: boolean, flagIndex: number): void => {
            scope.showToscaFunction[flagIndex] = toscaFlag;
            scope.valueObjRef[scope.mapKeys[flagIndex]] = null;
            if (!toscaFlag) {
                if (scope.parentProperty.subPropertyToscaFunctions != null) {
                    const subToscaFunctionList: SubPropertyToscaFunction[] = [];
                    scope.parentProperty.subPropertyToscaFunctions.forEach((SubPropertyToscaFunction, index) => {
                        if (SubPropertyToscaFunction.subPropertyPath.indexOf(scope.mapKeys[flagIndex]) == -1) {
                            subToscaFunctionList.push(SubPropertyToscaFunction);
                        }
                    });
                    scope.parentProperty.subPropertyToscaFunctions = subToscaFunctionList;
                }
            }
        };

        scope.onGetToscaFunction = (toscaGetFunction: ToscaGetFunction, key: string): void => {
            if (scope.parentProperty.subPropertyToscaFunctions != null) {
                scope.parentProperty.subPropertyToscaFunctions.forEach((SubPropertyToscaFunction) => {
                    if (SubPropertyToscaFunction.subPropertyPath.indexOf(key) != -1) {
                        SubPropertyToscaFunction.toscaFunction = toscaGetFunction;
                        return;
                    }
                });

            }
            if (scope.parentProperty.subPropertyToscaFunctions == null) {
                scope.parentProperty.subPropertyToscaFunctions = [];
            }
            const subPropertyToscaFunction = new SubPropertyToscaFunction();
            subPropertyToscaFunction.toscaFunction = toscaGetFunction;
            subPropertyToscaFunction.subPropertyPath = [key];
            scope.parentProperty.subPropertyToscaFunctions.push(subPropertyToscaFunction);
        };

        scope.addMapItemFields = (): void => {
            if (!scope.valueObjRef) {
                scope.valueObjRef = {};
                scope.showToscaFunction = [];
            }

            scope.valueObjRef[''] = null;
            scope.mapKeys = Object.keys(scope.valueObjRef);
            scope.showToscaFunction.push(false);
        };

        scope.parseToCorrectType = (objectOfValues: any, locationInObj: string, type: string): void => {
            if (objectOfValues[locationInObj] && type != PROPERTY_TYPES.STRING) {
                objectOfValues[locationInObj] = JSON.parse(objectOfValues[locationInObj]);
            }
        };

        scope.validateSubToscaFunction = (key: string): boolean => {
            if (scope.parentProperty.subPropertyToscaFunctions != null) {
                scope.parentProperty.subPropertyToscaFunctions.forEach((SubPropertyToscaFunction) => {
                    if (SubPropertyToscaFunction.subPropertyPath.indexOf(key) != -1) {
                        return true;
                    }
                });
            }
            return false;
        };
    }

    private isDataTypeForSchemaType = (property: SchemaProperty, types: DataTypesMap): boolean => {
        property.simpleType = '';
        if (property.type && PROPERTY_DATA.TYPES.indexOf(property.type) > -1) {
            return false;
        }
        const simpleType = this.getTypeForDataTypeDerivedFromSimple(property.type, types);
        if (simpleType) {
            property.simpleType = simpleType;
            return false;
        }
        return true;
    }

    private getTypeForDataTypeDerivedFromSimple = (dataTypeName: string, types: DataTypesMap): string => {
        if (!types[dataTypeName]) {
            return 'string';
        }
        if (types[dataTypeName].derivedFromName == 'tosca.datatypes.Root' || types[dataTypeName].properties) {
            return null;
        }
        if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(types[dataTypeName].derivedFromName) > -1) {
            return types[dataTypeName].derivedFromName;
        }
        return this.getTypeForDataTypeDerivedFromSimple(types[dataTypeName].derivedFromName, types);
    }
}

TypeMapDirective.factory.$inject = ['Sdc.Services.DataTypesService', 'MapKeyValidationPattern', 'ValidationUtils', '$timeout'];
