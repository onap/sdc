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

'use strict';
import { DataTypePropertyModel } from "../models/data-type-properties";
import {
    ComponentInstance,
    InputModel,
    DataTypesMap,
    PropertyModel,
    InputPropertyBase,
    IAppConfigurtaion,
    SchemaProperty, DataTypeModel
} from "../models";
import {PROPERTY_DATA} from "../utils/constants";

export interface IDataTypesService {

    dataTypes:DataTypesMap; //Data type map
    selectedPropertiesName:string;
    selectedInput:PropertyModel;
    alreadySelectedProperties:Array<InputPropertyBase>;
    selectedInstance:ComponentInstance;
    selectedComponentInputs:Array<InputModel>;
    //declare methods
    loadDataTypesCache(modelName:string):void;
    findAllDataTypesByModel(modelName: string): void;
    getAllDataTypes():DataTypesMap;
    getFirsLevelOfDataTypeProperties(dataTypeName:string):Array<DataTypePropertyModel>;
    isDataTypeForSchemaType(property:SchemaProperty):boolean;
    isDataTypeForPropertyType(property:PropertyModel):boolean;
    isDataTypeForDataTypePropertyType(property:DataTypePropertyModel):boolean;
}

export class DataTypesService implements IDataTypesService {

    static '$inject' = [
        'sdcConfig',
        '$q',
        '$http'
    ];

    constructor(private sdcConfig:IAppConfigurtaion,
                private $q:ng.IQService,
                private $http:ng.IHttpService) {
    }

    private baseUrl = this.sdcConfig.api.root + this.sdcConfig.api.component_api_root;

    dataTypes:DataTypesMap; //Data type map
    selectedPropertiesName:string;
    selectedInput:PropertyModel;
    alreadySelectedProperties:Array<InputPropertyBase>;
    selectedInstance:ComponentInstance;
    selectedComponentInputs:Array<InputModel>;

    public loadDataTypesCache = async (modelName: string): Promise<void> => {
        let model;
        if (modelName) {
            model = {'model': modelName}
        }
        await this.$http.get(this.baseUrl + "dataTypes", {params: model})
        .then((response: any) => {
            this.dataTypes = response.data;
            delete this.dataTypes['tosca.datatypes.Root'];
        });
    };

    public fetchDataTypesByModel = (modelName: string): angular.IHttpPromise<any> => {
        let model;
        if (modelName) {
            model = {'model': modelName}
        }
        return this.$http.get(this.baseUrl + "dataTypes", {params: model});
    };

    public getAllDataTypesFromModel = (modelName: string): DataTypesMap => {
        this.loadDataTypesCache(modelName);
        return this.dataTypes;
    }

    public findAllDataTypesByModel = (modelName: string): Promise<Map<string, DataTypeModel>> => {
        return new Promise<Map<string, DataTypeModel>>((resolve, reject) => {
            this.fetchDataTypesByModel(modelName).then(response => {
                const dataTypes = response.data;
                delete dataTypes[PROPERTY_DATA.ROOT_DATA_TYPE];
                const dataTypeMap = new Map<string, DataTypeModel>();
                for(const dataTypeKey of Object.keys(dataTypes)) {
                    dataTypeMap.set(dataTypeKey, new DataTypeModel(dataTypes[dataTypeKey]))
                }
                resolve(dataTypeMap);
            }).catch(reason => {
                reject(reason);
            });
        });
    }

    public getAllDataTypes = ():DataTypesMap => {
        return this.dataTypes;
    };

    //if the dt derived from simple- return the first parent type, else- return null
    private getTypeForDataTypeDerivedFromSimple = (dataTypeName:string):string => {
        /////////temporary hack for tosca primitives///////////////////////
        if (!this.dataTypes[dataTypeName]) {
            return 'string';
        }
        ///////////////////////////////////////////////////////////////////
        if (this.dataTypes[dataTypeName].derivedFromName == "tosca.datatypes.Root" || this.dataTypes[dataTypeName].properties) {
            return null;
        }
        if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.dataTypes[dataTypeName].derivedFromName) > -1) {
            return this.dataTypes[dataTypeName].derivedFromName
        }
        return this.getTypeForDataTypeDerivedFromSimple(this.dataTypes[dataTypeName].derivedFromName);
    };


    //return list of data type properties and all its parents properties
    //(not include the properties of its properties, in case this data type has not primitive properties)
    public getFirsLevelOfDataTypeProperties = (dataTypeName:string):Array<DataTypePropertyModel> => {
        let properties = this.dataTypes[dataTypeName].properties || [];
        if (this.dataTypes[dataTypeName].derivedFromName != "tosca.datatypes.Root") {
            properties = this.getFirsLevelOfDataTypeProperties(this.dataTypes[dataTypeName].derivedFromName).concat(properties);
        }
        return properties;
    };

    //return false when type= data type (=not simple type) that not derived from simple type
    public isDataTypeForSchemaType = (property:SchemaProperty):boolean=> {
        property.simpleType = "";
        if (property.type && PROPERTY_DATA.TYPES.indexOf(property.type) > -1) {
            return false;
        }
        let simpleType = this.getTypeForDataTypeDerivedFromSimple(property.type);
        if (simpleType) {
            property.simpleType = simpleType;
            return false;
        }
        return true;
    };

    public isDataTypeForPropertyType = (property:PropertyModel):boolean=> {
        property.simpleType = "";
        if (property.type && PROPERTY_DATA.TYPES.indexOf(property.type) > -1) {
            return false;
        }
        let simpleType = this.getTypeForDataTypeDerivedFromSimple(property.type);
        if (simpleType) {
            property.simpleType = simpleType;
            return false;
        }
        return true;
    };


    public isDataTypeForDataTypePropertyType = (property:DataTypePropertyModel):boolean=> {
        property.simpleType = "";
        let isScalarForNFoD:boolean = property.type === 'scalar-unit.size';
        if (property.type && PROPERTY_DATA.TYPES.indexOf(property.type) > -1 || isScalarForNFoD) {
            return false;
        }
        let simpleType = this.getTypeForDataTypeDerivedFromSimple(property.type);
        if (simpleType) {
            property.simpleType = simpleType;
            return false;
        }
        return true;
    };
}
