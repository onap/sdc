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
/// <reference path="../references"/>
module Sdc.Services {

    'use strict';

    export interface IDataTypesService {
        //declare methods
        getAllDataTypes():ng.IPromise<Models.PropertyModel>;
        getFirsLevelOfDataTypeProperties(dataTypeName:string, dataTypes:Models.DataTypesMap):Array<Models.DataTypePropertyModel>;
        isDataTypeForSchemaType(property:Models.SchemaProperty, types:Models.DataTypesMap):boolean;
        isDataTypeForPropertyType(property:Models.PropertyModel, types:Models.DataTypesMap):boolean;
        isDataTypeForDataTypePropertyType(property:Models.DataTypePropertyModel, types:Models.DataTypesMap):boolean;
    }

    export class DataTypesService implements IDataTypesService {

        static '$inject' = [
            'sdcConfig',
            '$q',
            '$http'
        ];

        constructor(private sdcConfig:Models.IAppConfigurtaion,
                    private $q:ng.IQService,
                    private $http:ng.IHttpService) {
        }

        //if the dt derived from simple- return the first parent type, else- return null
        private getTypeForDataTypeDerivedFromSimple = (dataTypeName:string, dataTypes:Models.DataTypesMap):string => {
            /////////temporary hack for tosca primitives///////////////////////
            if(!dataTypes[dataTypeName]){
                return 'string';
            }
            ///////////////////////////////////////////////////////////////////
            if(dataTypes[dataTypeName].derivedFromName == "tosca.datatypes.Root" || dataTypes[dataTypeName].properties){
                return null;
            }
            if(Utils.Constants.PROPERTY_DATA.SIMPLE_TYPES.indexOf(dataTypes[dataTypeName].derivedFromName) > -1 ){
                return dataTypes[dataTypeName].derivedFromName
            }
            return this.getTypeForDataTypeDerivedFromSimple(dataTypes[dataTypeName].derivedFromName,dataTypes);
        };

        public getAllDataTypes = ():ng.IPromise<Models.PropertyModel> => {
            let deferred = this.$q.defer();
            this.$http({
                url: this.sdcConfig.api.root + this.sdcConfig.api.component_api_root + "dataTypes",
                method: "get"
            })
            .success((response:any) => {
                deferred.resolve(response);
            })
            .error((err) => {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        //return list of data type properties and all its parents properties
        //(not include the properties of its properties, in case this data type has not primitive properties)
        public getFirsLevelOfDataTypeProperties = (dataTypeName:string, dataTypes:Models.DataTypesMap):Array<Models.DataTypePropertyModel> => {
            let properties = dataTypes[dataTypeName].properties || [];
            if(dataTypes[dataTypeName].derivedFromName != "tosca.datatypes.Root" ){
                properties = this.getFirsLevelOfDataTypeProperties(dataTypes[dataTypeName].derivedFromName,dataTypes).concat(properties);
            }
            return properties;
        };

        //return false when type= data type (=not simple type) that not derived from simple type
        public isDataTypeForSchemaType = (property:Models.SchemaProperty, types:Models.DataTypesMap):boolean=>{
            property.simpleType="";
            if(property.type && Utils.Constants.PROPERTY_DATA.TYPES.indexOf(property.type) > -1){
                return false;
            }
            let simpleType = this.getTypeForDataTypeDerivedFromSimple(property.type, types);
            if(simpleType){
                property.simpleType=simpleType;
                return false;
            }
            return true;
        };

        public isDataTypeForPropertyType = (property:Models.PropertyModel, types:Models.DataTypesMap):boolean=>{
            property.simpleType="";
            if(property.type && Utils.Constants.PROPERTY_DATA.TYPES.indexOf(property.type) > -1){
                return false;
            }
            let simpleType = this.getTypeForDataTypeDerivedFromSimple(property.type, types);
            if(simpleType){
                property.simpleType=simpleType;
                return false;
            }
            return true;
        };

        public isDataTypeForDataTypePropertyType = (property:Models.DataTypePropertyModel, types:Models.DataTypesMap):boolean=>{
            property.simpleType="";
            if(property.type && Utils.Constants.PROPERTY_DATA.TYPES.indexOf(property.type) > -1){
                return false;
            }
            let simpleType = this.getTypeForDataTypeDerivedFromSimple(property.type, types);
            if(simpleType){
                property.simpleType=simpleType;
                return false;
            }
            return true;
        };

    }
}
